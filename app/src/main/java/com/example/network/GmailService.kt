package com.example.network

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class FetchMessagesResult {
    data class Success(val messages: List<IncomingEmailMessage>) : FetchMessagesResult()
    data class NeedsAuth(val reason: String) : FetchMessagesResult()
    data class Error(val message: String) : FetchMessagesResult()
}

sealed class ConnectionVerificationResult {
    data class Connected(val email: String, val messagesTotal: Int) : ConnectionVerificationResult()
    data class NotConnected(val reason: String) : ConnectionVerificationResult()
}

data class GmailAttachment(
    val filename: String,
    val mimeType: String,
    val size: Int,
    val attachmentId: String? = null,
    val dataBytes: ByteArray? = null
)

data class IncomingEmailMessage(
    val id: String,
    val threadId: String,
    val senderEmail: String,
    val senderName: String,
    val subject: String,
    val snippet: String,
    val timestamp: Long,
    val attachments: List<GmailAttachment>
)

class GmailService(private val context: Context) {

    companion object {
        const val OAUTH_CLIENT_ID = "397514363334-edvgk0karck2m657hfasj62m61teuq9c.apps.googleusercontent.com"
        const val OAUTH_REDIRECT_URI = "https://gen-lang-client-0562581571.firebaseapp.com/__/auth/handler"
        const val OAUTH_SCOPES = "https://www.googleapis.com/auth/gmail.modify https://www.googleapis.com/auth/gmail.send"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("gmail_monitor_prefs", Context.MODE_PRIVATE)

    var oauthToken: String?
        get() = prefs.getString("oauth_access_token", null)
        set(value) = prefs.edit().putString("oauth_access_token", value).apply()

    var userEmailAddress: String
        get() {
            val stored = prefs.getString("monitored_user_email", null)
            return if (stored.isNullOrBlank() || stored == "mccandido.1979@gmail.com") {
                "paprint088@gmail.com"
            } else {
                stored
            }
        }
        set(value) = prefs.edit().putString("monitored_user_email", value).apply()

    var isMonitoringActive: Boolean
        get() = prefs.getBoolean("monitoring_active", true)
        set(value) = prefs.edit().putBoolean("monitoring_active", value).apply()

    var lastCheckedTimestamp: Long
        get() = prefs.getLong("last_checked_time", 0L)
        set(value) = prefs.edit().putLong("last_checked_time", value).apply()

    fun buildOAuthUrl(): String {
        val encodedRedirect = java.net.URLEncoder.encode(OAUTH_REDIRECT_URI, "UTF-8")
        val encodedScope = java.net.URLEncoder.encode(OAUTH_SCOPES, "UTF-8")
        val encodedEmail = java.net.URLEncoder.encode(userEmailAddress, "UTF-8")

        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=$OAUTH_CLIENT_ID" +
                "&redirect_uri=$encodedRedirect" +
                "&response_type=token" +
                "&scope=$encodedScope" +
                "&login_hint=$encodedEmail" +
                "&prompt=consent"
    }

    /**
     * Verifies if current OAuth token is valid and returns user profile.
     */
    suspend fun verifyConnection(): ConnectionVerificationResult = withContext(Dispatchers.IO) {
        val token = oauthToken
        if (token.isNullOrBlank()) {
            return@withContext ConnectionVerificationResult.NotConnected("No OAuth token found. Please connect paprint088@gmail.com.")
        }

        try {
            val request = Request.Builder()
                .url("https://gmail.googleapis.com/gmail/v1/users/me/profile")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 401) {
                oauthToken = null
                return@withContext ConnectionVerificationResult.NotConnected("Token expired or unauthorized (401). Please re-connect.")
            }

            if (!response.isSuccessful) {
                return@withContext ConnectionVerificationResult.NotConnected("HTTP Error ${response.code}: ${response.message}")
            }

            val body = response.body?.string() ?: return@withContext ConnectionVerificationResult.NotConnected("Empty response")
            val json = JSONObject(body)
            val email = json.optString("emailAddress", userEmailAddress)
            val total = json.optInt("messagesTotal", 0)

            ConnectionVerificationResult.Connected(email = email, messagesTotal = total)
        } catch (e: Exception) {
            ConnectionVerificationResult.NotConnected("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Fetches unread messages from Gmail API.
     */
    suspend fun fetchUnreadMessages(): FetchMessagesResult = withContext(Dispatchers.IO) {
        val token = oauthToken
        if (token.isNullOrBlank()) {
            return@withContext FetchMessagesResult.NeedsAuth("Google authorization required to read emails for $userEmailAddress")
        }

        try {
            val listUrl = "https://gmail.googleapis.com/gmail/v1/users/me/messages?q=is:unread"
            val request = Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 401) {
                oauthToken = null
                return@withContext FetchMessagesResult.NeedsAuth("Gmail access token expired. Please re-authenticate.")
            }

            if (!response.isSuccessful) {
                return@withContext FetchMessagesResult.Error("Gmail API error: HTTP ${response.code} ${response.message}")
            }

            val body = response.body?.string() ?: return@withContext FetchMessagesResult.Success(emptyList())
            val json = JSONObject(body)
            val messagesArray = json.optJSONArray("messages") ?: return@withContext FetchMessagesResult.Success(emptyList())

            val messages = mutableListOf<IncomingEmailMessage>()
            for (i in 0 until messagesArray.length()) {
                val msgObj = messagesArray.getJSONObject(i)
                val id = msgObj.getString("id")
                val detailed = fetchMessageDetails(id, token)
                if (detailed != null) {
                    messages.add(detailed)
                }
            }
            lastCheckedTimestamp = System.currentTimeMillis()
            FetchMessagesResult.Success(messages)
        } catch (e: Exception) {
            FetchMessagesResult.Error("Network error checking Gmail: ${e.localizedMessage}")
        }
    }

    private suspend fun fetchMessageDetails(id: String, token: String): IncomingEmailMessage? = withContext(Dispatchers.IO) {
        try {
            val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/$id"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)

            val threadId = json.optString("threadId", id)
            val snippet = json.optString("snippet", "")
            val internalDate = json.optLong("internalDate", System.currentTimeMillis())

            val payload = json.optJSONObject("payload")
            val headers = payload?.optJSONArray("headers")

            var fromHeader = ""
            var subjectHeader = "Print Request"

            if (headers != null) {
                for (h in 0 until headers.length()) {
                    val header = headers.getJSONObject(h)
                    when (header.optString("name").lowercase(Locale.ROOT)) {
                        "from" -> fromHeader = header.optString("value")
                        "subject" -> subjectHeader = header.optString("value")
                    }
                }
            }

            val (senderName, senderEmail) = parseSender(fromHeader)
            val attachments = parseAttachments(id, payload, token)

            IncomingEmailMessage(
                id = id,
                threadId = threadId,
                senderEmail = senderEmail,
                senderName = senderName,
                subject = subjectHeader,
                snippet = snippet,
                timestamp = internalDate,
                attachments = attachments
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun parseAttachments(messageId: String, payload: JSONObject?, token: String): List<GmailAttachment> {
        val attachments = mutableListOf<GmailAttachment>()
        if (payload == null) return attachments

        suspend fun extractFromParts(parts: JSONArray?) {
            if (parts == null) return
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val filename = part.optString("filename", "")
                val mimeType = part.optString("mimeType", "")
                val body = part.optJSONObject("body")
                val attachmentId = if (body != null && body.has("attachmentId")) body.getString("attachmentId") else null
                val size = body?.optInt("size", 0) ?: 0

                var dataBytes: ByteArray? = null
                val inlineData = if (body != null && body.has("data")) body.optString("data") else null

                if (!inlineData.isNullOrBlank()) {
                    try {
                        dataBytes = Base64.decode(inlineData, Base64.URL_SAFE)
                    } catch (_: Exception) {}
                } else if (!attachmentId.isNullOrBlank()) {
                    dataBytes = downloadAttachmentBytes(messageId, attachmentId, token)
                }

                if (filename.isNotBlank()) {
                    attachments.add(
                        GmailAttachment(
                            filename = filename,
                            mimeType = mimeType,
                            size = size,
                            attachmentId = attachmentId,
                            dataBytes = dataBytes
                        )
                    )
                }

                if (part.has("parts")) {
                    extractFromParts(part.optJSONArray("parts"))
                }
            }
        }

        extractFromParts(payload.optJSONArray("parts"))
        return attachments
    }

    private suspend fun downloadAttachmentBytes(messageId: String, attachmentId: String, token: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/$messageId/attachments/$attachmentId"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val dataStr = json.optString("data", "")
            if (dataStr.isNotBlank()) {
                return@withContext Base64.decode(dataStr, Base64.URL_SAFE)
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSender(from: String): Pair<String, String> {
        if (from.isBlank()) return Pair("Customer", "customer@example.com")
        val match = Regex("""^(.*?)\s*<(.+?)>$""").find(from.trim())
        return if (match != null) {
            val name = match.groupValues[1].replace("\"", "").trim()
            val email = match.groupValues[2].trim()
            Pair(name.ifBlank { email }, email)
        } else {
            Pair(from.trim(), from.trim())
        }
    }

    /**
     * Sends an email reply via Gmail REST API.
     */
    suspend fun sendEmailReply(
        to: String,
        subject: String,
        bodyText: String,
        inReplyToMessageId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val token = oauthToken
        if (token.isNullOrBlank()) {
            return@withContext true
        }

        try {
            val replySubject = if (subject.startsWith("Re:", ignoreCase = true)) subject else "Re: $subject"
            val rawEmail = StringBuilder().apply {
                append("To: $to\r\n")
                append("Subject: $replySubject\r\n")
                if (!inReplyToMessageId.isNullOrBlank()) {
                    append("In-Reply-To: <$inReplyToMessageId>\r\n")
                    append("References: <$inReplyToMessageId>\r\n")
                }
                append("Content-Type: text/plain; charset=\"UTF-8\"\r\n\r\n")
                append(bodyText)
            }.toString()

            val base64Email = Base64.encodeToString(rawEmail.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
            val jsonBody = JSONObject().apply {
                put("raw", base64Email)
            }

            val request = Request.Builder()
                .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                .addHeader("Authorization", "Bearer $token")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Marks message as processed (removes UNREAD label).
     */
    suspend fun markAsRead(messageId: String): Boolean = withContext(Dispatchers.IO) {
        val token = oauthToken ?: return@withContext true
        try {
            val jsonBody = JSONObject().apply {
                put("removeLabelIds", JSONArray().apply { put("UNREAD") })
            }
            val request = Request.Builder()
                .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/$messageId/modify")
                .addHeader("Authorization", "Bearer $token")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
