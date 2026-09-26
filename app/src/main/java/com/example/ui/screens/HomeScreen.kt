package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ConnectionVerificationResult
import com.example.data.model.CostMatrix
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintBlue
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald
import com.example.ui.theme.PrintRose
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    isMonitoring: Boolean,
    isCheckingGmail: Boolean,
    monitoredEmail: String,
    connectionStatus: ConnectionVerificationResult?,
    pendingJobs: List<PrintJob>,
    recentJobs: List<PrintJob>,
    costMatrix: CostMatrix,
    activePrinters: List<PrinterModel>,
    totalIncome: Double,
    onToggleMonitoring: (Boolean) -> Unit,
    onCheckGmailNow: () -> Unit,
    onConnectAccount: () -> Unit,
    onOpenSimulation: () -> Unit,
    onReviewJob: (PrintJob) -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val isConnected = connectionStatus is ConnectionVerificationResult.Connected

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Monitor Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monitor_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMonitoring) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected && isMonitoring) PrintEmerald else PrintAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected && isMonitoring) "MONITORING ACTIVE" else if (!isConnected) "NEEDS AUTHORIZATION" else "MONITORING PAUSED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isConnected && isMonitoring) PrintEmerald else PrintAmber
                            )
                        }

                        Switch(
                            checked = isMonitoring,
                            onCheckedChange = onToggleMonitoring,
                            modifier = Modifier.testTag("monitor_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = monitoredEmail,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isConnected) {
                                    val conn = connectionStatus as ConnectionVerificationResult.Connected
                                    "Connected • ${conn.messagesTotal} messages in inbox • Auto-polls every 20s"
                                } else {
                                    "Google authorization required to read new print requests."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isConnected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onConnectAccount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("connect_account_hero_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect $monitoredEmail (Sign In)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCheckGmailNow,
                            enabled = !isCheckingGmail,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("check_gmail_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isCheckingGmail) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check Gmail")
                            }
                        }

                        FilledTonalButton(
                            onClick = onOpenSimulation,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_simulation_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Scenarios")
                        }
                    }
                }
            }
        }

        // Quick Stats Row (Income & Pending)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = PrintEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Total Income",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${costMatrix.currencySymbol}${"%.2f".format(totalIncome)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrintEmerald
                        )
                    }
                }

                // Pending Approvals count card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                tint = if (pendingJobs.isNotEmpty()) PrintAmber else PrintCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Needs Approval",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${pendingJobs.size} job(s)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (pendingJobs.isNotEmpty()) PrintAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Pending Approval Section (Critical requirement: "notify user which model of printer is use, print settings and cost, modify if needed, then when approved button, reply sender cost only, sends file to printer")
        if (pendingJobs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Awaiting Your Approval (${pendingJobs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(pendingJobs) { job ->
                PendingJobCard(
                    job = job,
                    currencySymbol = costMatrix.currencySymbol,
                    onReview = { onReviewJob(job) }
                )
            }
        }

        // Active Default Printer Status
        item {
            val defaultPrinter = activePrinters.firstOrNull { it.isDefault } ?: activePrinters.firstOrNull()
            if (defaultPrinter != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = defaultPrinter.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ready • ${defaultPrinter.location} • Black Ink: ${defaultPrinter.blackInkLevelPercent}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Activity Stream
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity & Auto-Replies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = onNavigateToLogs,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Full Log", fontSize = 12.sp)
                }
            }
        }

        if (recentJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No print requests received yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap 'Test Scenarios' above to test email workflows.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentJobs.take(5)) { job ->
                RecentJobRow(job = job, currencySymbol = costMatrix.currencySymbol)
            }
        }
    }
}

@Composable
fun PendingJobCard(
    job: PrintJob,
    currencySymbol: String,
    onReview: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.attachmentName ?: "Document.pdf",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From ${job.senderName} (${job.senderEmail})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$currencySymbol${"%.2f".format(job.finalApprovedCost)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🖨️ ${job.assignedPrinterName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "📄 ${job.pageCount}p (${job.paperSize})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "💧 ${job.avgInkCoverage}% ink",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (job.avgInkCoverage > 25.0) PrintAmber else PrintEmerald
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("review_approve_btn_${job.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Settings & Approve Dispatch", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentJobRow(job: PrintJob, currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, iconColor, statusLabel) = when (job.status) {
                JobStatus.APPROVED_PRINTED -> Triple(Icons.Default.CheckCircle, PrintEmerald, "Approved & Dispatched")
                JobStatus.REJECTED_NO_ATTACHMENT -> Triple(Icons.Default.HighlightOff, PrintRose, "No Attachment Auto-Reply")
                JobStatus.REJECTED_PHOTO_OF_DOC -> Triple(Icons.Default.CameraAlt, PrintAmber, "Doc Photo Format Rejected")
                JobStatus.PENDING_APPROVAL -> Triple(Icons.Default.Warning, PrintCyan, "Pending Approval")
                else -> Triple(Icons.Default.Description, Slate700, "Cancelled")
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.attachmentName ?: job.emailSubject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "${job.senderName} • $statusLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (job.status == JobStatus.APPROVED_PRINTED) {
                Text(
                    text = "+$currencySymbol${"%.2f".format(job.incomeEarned)}",
                    fontWeight = FontWeight.Bold,
                    color = PrintEmerald,
                    fontSize = 14.sp
                )
            }
        }
    }
}
