package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAuthDialog(
    authUrl: String,
    targetEmail: String,
    onDismiss: () -> Unit,
    onTokenReceived: (String) -> Unit
) {
    val context = LocalContext.current
    var manualToken by remember { mutableStateOf("") }
    var useEmbeddedWebView by remember { mutableStateOf(false) }
    var webViewCrashed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_auth_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Connect Gmail Account",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = targetEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Notice Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Authorize PrintFlow to read print requests from $targetEmail and reply with total cost.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!useEmbeddedWebView) {
                        // Method 1: System Browser Sign-In (Safest & most stable across all devices & emulators)
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_open_browser_signin"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Browser to Sign In", fontWeight = FontWeight.Bold)
                        }

                        // Method 2: Copy link
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Google Auth Link", authUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Sign-in link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Sign-In Link")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Method 3: Direct Token Input
                        Text(
                            text = "Or Paste OAuth Access Token:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = manualToken,
                            onValueChange = { manualToken = it },
                            placeholder = { Text("ya29.a0Ac...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (manualToken.isNotBlank()) {
                                    onTokenReceived(manualToken.trim())
                                } else {
                                    Toast.makeText(context, "Please paste an access token first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_apply_token"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply Access Token")
                        }

                        TextButton(
                            onClick = { useEmbeddedWebView = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Embedded Web View", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        // Crash-safe software-rendered WebView
                        if (webViewCrashed) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Embedded renderer is not supported by this emulator. Please use 'Open in Browser' instead.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            // Crucial: Use software rendering to prevent MESA rendernode GPU crash!
                                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true

                                            webViewClient = object : WebViewClient() {
                                                override fun onRenderProcessGone(
                                                    view: WebView?,
                                                    detail: RenderProcessGoneDetail?
                                                ): Boolean {
                                                    webViewCrashed = true
                                                    return true // Prevents app termination!
                                                }

                                                override fun shouldOverrideUrlLoading(
                                                    view: WebView?,
                                                    request: WebResourceRequest?
                                                ): Boolean {
                                                    val url = request?.url?.toString() ?: ""
                                                    if (url.contains("access_token=")) {
                                                        val regex = Regex("access_token=([^&]+)")
                                                        val match = regex.find(url)
                                                        val token = match?.groupValues?.get(1)
                                                        if (!token.isNullOrBlank()) {
                                                            onTokenReceived(token)
                                                            return true
                                                        }
                                                    }
                                                    return false
                                                }
                                            }
                                            loadUrl(authUrl)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(380.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { useEmbeddedWebView = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Switch Back to Browser / Token Mode")
                        }
                    }
                }
            }
        }
    )
}
