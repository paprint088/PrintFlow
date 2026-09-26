package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CostMatrix
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import com.example.data.repository.PrintRepository
import com.example.network.ConnectionVerificationResult
import com.example.network.DocumentAnalysisResult
import com.example.network.DocumentAnalyzer
import com.example.network.FetchMessagesResult
import com.example.network.GmailAttachment
import com.example.network.GmailService
import com.example.network.IncomingEmailMessage
import com.example.service.NotificationHelper
import com.example.service.PrinterDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class PrintViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrintRepository
    val gmailService: GmailService

    val allJobs: StateFlow<List<PrintJob>>
    val pendingJobs: StateFlow<List<PrintJob>>
    val costMatrixState: StateFlow<CostMatrix>
    val printersState: StateFlow<List<PrinterModel>>
    val totalIncomeState: StateFlow<Double?>

    private val _isCheckingGmail = MutableStateFlow(false)
    val isCheckingGmail: StateFlow<Boolean> = _isCheckingGmail.asStateFlow()

    private val _activeJobForModal = MutableStateFlow<PrintJob?>(null)
    val activeJobForModal: StateFlow<PrintJob?> = _activeJobForModal.asStateFlow()

    private val _bannerAlert = MutableStateFlow<String?>(null)
    val bannerAlert: StateFlow<String?> = _bannerAlert.asStateFlow()

    private val _isMonitoring = MutableStateFlow(true)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _monitoredEmail = MutableStateFlow("")
    val monitoredEmail: StateFlow<String> = _monitoredEmail.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionVerificationResult?>(null)
    val connectionStatus: StateFlow<ConnectionVerificationResult?> = _connectionStatus.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = PrintRepository(db)
        gmailService = GmailService(application)
        _isMonitoring.value = gmailService.isMonitoringActive
        _monitoredEmail.value = gmailService.userEmailAddress

        viewModelScope.launch {
            repository.ensureInitialData()
            verifyConnection()
            // Auto-polling loop every 20 seconds
            while (true) {
                delay(20_000)
                if (_isMonitoring.value && !gmailService.oauthToken.isNullOrBlank()) {
                    checkGmailSilently()
                }
            }
        }

        allJobs = repository.allJobs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingJobs = repository.pendingJobs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        costMatrixState = repository.costMatrixFlow
            .map { it ?: CostMatrix() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CostMatrix()
            )

        printersState = repository.allPrinters.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        totalIncomeState = repository.totalIncome.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0.0
        )
    }

    fun toggleMonitoring(enabled: Boolean) {
        _isMonitoring.value = enabled
        gmailService.isMonitoringActive = enabled
        setBanner(if (enabled) "Gmail Monitoring is ACTIVE" else "Gmail Monitoring PAUSED")
    }

    fun setBanner(message: String?) {
        _bannerAlert.value = message
    }

    fun openJobModal(job: PrintJob) {
        _activeJobForModal.value = job
    }

    fun closeJobModal() {
        _activeJobForModal.value = null
    }

    fun updateOAuthToken(token: String) {
        val cleanToken = token.trim()
        gmailService.oauthToken = cleanToken
        setBanner("Gmail authorization token updated. Connecting...")
        viewModelScope.launch {
            verifyConnection()
            checkGmailNow()
        }
    }

    fun verifyConnection() {
        viewModelScope.launch {
            val res = gmailService.verifyConnection()
            _connectionStatus.value = res
            if (res is ConnectionVerificationResult.Connected) {
                _monitoredEmail.value = res.email
                setBanner("Successfully connected to ${res.email} (${res.messagesTotal} total messages)")
            }
        }
    }

    fun updateMonitoredEmail(newEmail: String) {
        val clean = newEmail.trim()
        gmailService.userEmailAddress = clean
        _monitoredEmail.value = clean
        setBanner("Monitored Gmail updated to: $clean")
        viewModelScope.launch {
            checkGmailNow()
        }
    }

    fun checkGmailNow() {
        viewModelScope.launch {
            _isCheckingGmail.value = true
            when (val result = gmailService.fetchUnreadMessages()) {
                is FetchMessagesResult.NeedsAuth -> {
                    _connectionStatus.value = ConnectionVerificationResult.NotConnected(result.reason)
                    setBanner("Sign-In Required: Please connect ${gmailService.userEmailAddress} to detect print request emails.")
                }
                is FetchMessagesResult.Error -> {
                    setBanner(result.message)
                }
                is FetchMessagesResult.Success -> {
                    val messages = result.messages
                    if (messages.isEmpty()) {
                        setBanner("Gmail checked for ${gmailService.userEmailAddress}: No new unread messages.")
                    } else {
                        var newDispatched = 0
                        for (msg in messages) {
                            val existing = repository.getJobByGmailId(msg.id)
                            if (existing == null) {
                                processIncomingMessage(msg)
                                newDispatched++
                            }
                        }
                        if (newDispatched > 0) {
                            setBanner("Detected $newDispatched new print request(s) for ${gmailService.userEmailAddress}!")
                        } else {
                            setBanner("All ${messages.size} unread messages have already been processed.")
                        }
                    }
                }
            }
            _isCheckingGmail.value = false
        }
    }

    private suspend fun checkGmailSilently() {
        when (val result = gmailService.fetchUnreadMessages()) {
            is FetchMessagesResult.Success -> {
                for (msg in result.messages) {
                    val existing = repository.getJobByGmailId(msg.id)
                    if (existing == null) {
                        processIncomingMessage(msg)
                    }
                }
            }
            else -> {}
        }
    }

    /**
     * Core workflow logic conforming strictly to user requirements:
     * 1. determine if there is an attachment.
     * 2. if none, reply sender that there are no files attached, app notify user.
     * 3. if attachment is photograph of document, reply cannot be printed (not standard format), app notify user.
     * 4. otherwise, determine ink consumption per page based on customizable cost matrix.
     * 5. notify user which model of printer is used, print settings and cost, modify if needed.
     */
    fun processIncomingMessage(msg: IncomingEmailMessage) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val matrix = costMatrixState.value

            // 1. Determine if there is an attachment
            if (msg.attachments.isEmpty()) {
                val replyText = "Hello,\n\nWe received your email with subject \"${msg.subject}\", but there are no files attached. Please resend your message with the file or document you wish to print attached.\n\nThank you,\nPrintFlow Auto Service"
                
                // Reply to sender via Gmail
                gmailService.sendEmailReply(msg.senderEmail, msg.subject, replyText, msg.id)
                gmailService.markAsRead(msg.id)

                // Log rejected job
                val job = PrintJob(
                    gmailMessageId = msg.id,
                    senderEmail = msg.senderEmail,
                    senderName = msg.senderName,
                    emailSubject = msg.subject,
                    hasAttachment = false,
                    status = JobStatus.REJECTED_NO_ATTACHMENT,
                    replySent = true,
                    replyMessage = replyText,
                    completedAt = System.currentTimeMillis()
                )
                repository.insertJob(job)

                // App notify user
                NotificationHelper.notifyNoAttachment(context, msg.senderName, msg.subject)
                setBanner("Notice: No attachment from ${msg.senderName}. Automated reply sent.")
                return@launch
            }

            // 2. We have attachment: analyze format and photo of document check
            val firstAttachment = msg.attachments.first()
            val analysis: DocumentAnalysisResult = DocumentAnalyzer.analyzeDocument(
                filename = firstAttachment.filename,
                mimeType = firstAttachment.mimeType,
                attachmentBytes = firstAttachment.dataBytes
            )

            if (analysis.isPhotographOfDocument) {
                // If attachment is a photograph of a document
                val replyText = "Hello,\n\nYour file \"${firstAttachment.filename}\" cannot be printed for it is not a standard format. It appears to be a photograph of a document. Please send a standard digital PDF or export document.\n\nThank you,\nPrintFlow Auto Service"
                
                // Reply to sender via Gmail
                gmailService.sendEmailReply(msg.senderEmail, msg.subject, replyText, msg.id)
                gmailService.markAsRead(msg.id)

                // Log rejected job
                val job = PrintJob(
                    gmailMessageId = msg.id,
                    senderEmail = msg.senderEmail,
                    senderName = msg.senderName,
                    emailSubject = msg.subject,
                    hasAttachment = true,
                    isPhotographOfDocument = true,
                    attachmentName = firstAttachment.filename,
                    attachmentType = firstAttachment.mimeType,
                    status = JobStatus.REJECTED_PHOTO_OF_DOC,
                    replySent = true,
                    replyMessage = replyText,
                    completedAt = System.currentTimeMillis()
                )
                repository.insertJob(job)

                // App notify user
                NotificationHelper.notifyPhotographRejected(context, msg.senderName, firstAttachment.filename)
                setBanner("Format Alert: Photograph of document from ${msg.senderName} rejected.")
                return@launch
            }

            // 3. Valid document: determine ink consumption per page & assign printer
            val printers = printersState.value
            val assignedPrinter = printers.firstOrNull { it.isDefault }
                ?: printers.firstOrNull()
                ?: PrinterModel(name = "Epson EcoTank L3210 (Main Desk)")

            val colorMode = if (analysis.isColorDetected && assignedPrinter.isColor) "COLOR" else "MONOCHROME"
            val paperSize = "A4"
            val isDuplex = false

            // Calculate cost based on per-page ink consumption
            var totalCost = 0.0
            val inkBreakdownLines = mutableListOf<String>()

            for (page in analysis.pages) {
                val pageCost = matrix.calculatePageCost(
                    isColor = colorMode == "COLOR",
                    inkCoveragePercent = page.totalCoveragePercent,
                    paperSize = paperSize,
                    isDuplex = isDuplex
                )
                totalCost += pageCost
                inkBreakdownLines.add("Page ${page.pageNumber}: ${page.totalCoveragePercent}% coverage (Black: ${page.blackInkCoveragePercent}%, Color: ${page.colorInkCoveragePercent}%)")
            }

            totalCost += matrix.baseServiceFee
            val roundedCost = String.format("%.2f", totalCost).toDouble()

            val job = PrintJob(
                gmailMessageId = msg.id,
                senderEmail = msg.senderEmail,
                senderName = msg.senderName,
                emailSubject = msg.subject,
                hasAttachment = true,
                isPhotographOfDocument = false,
                attachmentName = analysis.attachmentName,
                attachmentType = firstAttachment.mimeType,
                pageCount = analysis.pageCount,
                avgInkCoverage = analysis.averageCoverage,
                inkBreakdownJson = inkBreakdownLines.joinToString("\n"),
                assignedPrinterId = assignedPrinter.id,
                assignedPrinterName = assignedPrinter.name,
                paperSize = paperSize,
                printColorMode = colorMode,
                copies = 1,
                isDuplex = isDuplex,
                calculatedCost = roundedCost,
                finalApprovedCost = roundedCost,
                status = JobStatus.PENDING_APPROVAL
            )

            val insertedId = repository.insertJob(job)
            val updatedJob = job.copy(id = insertedId)

            // Notify user which model of printer is use, print settings and cost
            val costStr = "${matrix.currencySymbol}${"%.2f".format(roundedCost)}"
            NotificationHelper.notifyJobApprovalNeeded(
                context,
                msg.senderName,
                assignedPrinter.name,
                analysis.pageCount,
                costStr
            )
            setBanner("New Print Request: ${analysis.attachmentName} from ${msg.senderName} - $costStr. Review and approve.")

            // Automatically open modal for user review
            _activeJobForModal.value = updatedJob
        }
    }

    /**
     * User modifies print settings and re-evaluates cost.
     */
    fun updateJobSettings(
        jobId: Long,
        printerId: Int,
        printerName: String,
        paperSize: String,
        colorMode: String,
        copies: Int,
        isDuplex: Boolean,
        customCost: Double?
    ) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId) ?: return@launch
            val matrix = costMatrixState.value

            val newCost = if (customCost != null) {
                customCost
            } else {
                // Recompute using updated settings
                val perPageAvgCost = matrix.calculatePageCost(
                    isColor = colorMode == "COLOR",
                    inkCoveragePercent = job.avgInkCoverage,
                    paperSize = paperSize,
                    isDuplex = isDuplex
                )
                val total = (perPageAvgCost * job.pageCount * copies) + matrix.baseServiceFee
                String.format("%.2f", total).toDouble()
            }

            val updated = job.copy(
                assignedPrinterId = printerId,
                assignedPrinterName = printerName,
                paperSize = paperSize,
                printColorMode = colorMode,
                copies = copies,
                isDuplex = isDuplex,
                finalApprovedCost = newCost
            )

            repository.updateJob(updated)
            _activeJobForModal.value = updated
            setBanner("Print settings & cost updated: ${matrix.currencySymbol}${"%.2f".format(newCost)}")
        }
    }

    /**
     * Requirement: "then when approved button, reply to sender the cost only, then sends the file to the printer. logs details and income."
     */
    fun approveAndDispatchJob(job: PrintJob, activityContext: Context) {
        viewModelScope.launch {
            val matrix = costMatrixState.value
            val costOnlyText = "${matrix.currencySymbol}${"%.2f".format(job.finalApprovedCost)}"
            
            // 1. "reply to sender the cost only"
            val replyText = "Hello,\n\nThe total cost for your print request is: $costOnlyText\n\nThank you for choosing our print service!"
            gmailService.sendEmailReply(job.senderEmail, job.emailSubject, replyText, job.gmailMessageId)
            if (job.gmailMessageId.isNotBlank()) {
                gmailService.markAsRead(job.gmailMessageId)
            }

            // 2. "sends the file to the printer" via Android PrintManager
            PrinterDispatcher.sendToPrinter(activityContext, job) { success ->
                // Complete callback
            }

            // 3. "logs details and income"
            val completedJob = job.copy(
                status = JobStatus.APPROVED_PRINTED,
                replySent = true,
                replyMessage = "Cost Sent: $costOnlyText",
                completedAt = System.currentTimeMillis(),
                incomeEarned = job.finalApprovedCost
            )
            repository.updateJob(completedJob)
            _activeJobForModal.value = null

            setBanner("Approved! Cost reply sent ($costOnlyText) and dispatched to ${job.assignedPrinterName}. Income logged.")
        }
    }

    fun dismissOrCancelJob(job: PrintJob) {
        viewModelScope.launch {
            val updated = job.copy(status = JobStatus.CANCELLED)
            repository.updateJob(updated)
            _activeJobForModal.value = null
            setBanner("Job #${job.id} cancelled.")
        }
    }

    fun deleteJob(jobId: Long) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
            setBanner("Job record removed.")
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAllJobs()
            setBanner("All job logs cleared.")
        }
    }

    fun saveCostMatrix(matrix: CostMatrix) {
        viewModelScope.launch {
            repository.saveCostMatrix(matrix)
            setBanner("Print cost matrix updated successfully.")
        }
    }

    fun addPrinter(printer: PrinterModel) {
        viewModelScope.launch {
            repository.addPrinter(printer)
            setBanner("Printer '${printer.name}' added.")
        }
    }

    fun updatePrinter(printer: PrinterModel) {
        viewModelScope.launch {
            repository.updatePrinter(printer)
            setBanner("Printer '${printer.name}' updated.")
        }
    }

    fun deletePrinter(printer: PrinterModel) {
        viewModelScope.launch {
            repository.deletePrinter(printer)
            setBanner("Printer deleted.")
        }
    }

    /**
     * Built-in test simulator for testing all 3 user requirements on-device:
     * 1. No attachment -> auto-reply "no files attached"
     * 2. Photograph of document -> auto-reply "file cannot be printed for it is not a standard format"
     * 3. Printable document -> determine ink consumption, notify printer, settings & cost, approve button
     */
    fun simulateTestIncomingEmail(scenarioType: String) {
        viewModelScope.launch {
            val msgId = "sim_${UUID.randomUUID().toString().take(8)}"
            when (scenarioType) {
                "NO_ATTACHMENT" -> {
                    val msg = IncomingEmailMessage(
                        id = msgId,
                        threadId = msgId,
                        senderEmail = "alex.student@university.edu",
                        senderName = "Alex Cruz",
                        subject = "Please print my homework quickly",
                        snippet = "Hi, can you print this for me please?",
                        timestamp = System.currentTimeMillis(),
                        attachments = emptyList() // No attachments!
                    )
                    processIncomingMessage(msg)
                }

                "PHOTO_OF_DOC" -> {
                    val msg = IncomingEmailMessage(
                        id = msgId,
                        threadId = msgId,
                        senderEmail = "maria.garcia@gmail.com",
                        senderName = "Maria Garcia",
                        subject = "Print this contract photo please",
                        snippet = "Attached is the phone camera picture of the contract agreement.",
                        timestamp = System.currentTimeMillis(),
                        attachments = listOf(
                            GmailAttachment(
                                filename = "IMG_20260925_contract_desk_photo.jpg",
                                mimeType = "image/jpeg",
                                size = 384 * 1024
                            )
                        )
                    )
                    processIncomingMessage(msg)
                }

                "VALID_DOCUMENT_COLOR" -> {
                    val msg = IncomingEmailMessage(
                        id = msgId,
                        threadId = msgId,
                        senderEmail = "dr.roberts@cityclinic.org",
                        senderName = "Dr. Katherine Roberts",
                        subject = "Medical Conference Handout 2026",
                        snippet = "Please print 1 copy of the attached medical conference handout slides.",
                        timestamp = System.currentTimeMillis(),
                        attachments = listOf(
                            GmailAttachment(
                                filename = "Conference_Handout_Presentation.pdf",
                                mimeType = "application/pdf",
                                size = 1250 * 1024
                            )
                        )
                    )
                    processIncomingMessage(msg)
                }

                "VALID_DOCUMENT_BW" -> {
                    val msg = IncomingEmailMessage(
                        id = msgId,
                        threadId = msgId,
                        senderEmail = "legal.corp@venturelaw.com",
                        senderName = "Atty. Benjamin Lee",
                        subject = "Non-Disclosure Agreement (Final Draft)",
                        snippet = "Attached is the legal agreement for signing.",
                        timestamp = System.currentTimeMillis(),
                        attachments = listOf(
                            GmailAttachment(
                                filename = "NDA_Draft_Final_Signed.pdf",
                                mimeType = "application/pdf",
                                size = 420 * 1024
                            )
                        )
                    )
                    processIncomingMessage(msg)
                }
            }
        }
    }
}
