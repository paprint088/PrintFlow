package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CostMatrix
import com.example.data.model.JobStatus
import com.example.data.model.PrintJob
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald
import com.example.ui.theme.PrintRose
import com.example.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JobsLogScreen(
    jobs: List<PrintJob>,
    costMatrix: CostMatrix,
    onDeleteJob: (Long) -> Unit,
    onClearAllLogs: () -> Unit,
    onReviewPendingJob: (PrintJob) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredJobs = remember(jobs, selectedFilter) {
        when (selectedFilter) {
            JobStatus.APPROVED_PRINTED -> jobs.filter { it.status == JobStatus.APPROVED_PRINTED }
            JobStatus.REJECTED_NO_ATTACHMENT -> jobs.filter { it.status == JobStatus.REJECTED_NO_ATTACHMENT }
            JobStatus.REJECTED_PHOTO_OF_DOC -> jobs.filter { it.status == JobStatus.REJECTED_PHOTO_OF_DOC }
            JobStatus.PENDING_APPROVAL -> jobs.filter { it.status == JobStatus.PENDING_APPROVAL }
            else -> jobs
        }
    }

    val approvedCount = jobs.count { it.status == JobStatus.APPROVED_PRINTED }
    val noAttachCount = jobs.count { it.status == JobStatus.REJECTED_NO_ATTACHMENT }
    val photoCount = jobs.count { it.status == JobStatus.REJECTED_PHOTO_OF_DOC }
    val totalIncome = jobs.filter { it.status == JobStatus.APPROVED_PRINTED }.sumOf { it.incomeEarned }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("jobs_log_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Activity & Income Audit Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${jobs.size} total events logged • $approvedCount dispatched",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    if (jobs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear all logs",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Filter chips horizontal scroll
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${jobs.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == JobStatus.APPROVED_PRINTED,
                        onClick = { selectedFilter = JobStatus.APPROVED_PRINTED },
                        label = { Text("Approved & Printed ($approvedCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == JobStatus.REJECTED_PHOTO_OF_DOC,
                        onClick = { selectedFilter = JobStatus.REJECTED_PHOTO_OF_DOC },
                        label = { Text("Photo Rejections ($photoCount)") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == JobStatus.REJECTED_NO_ATTACHMENT,
                        onClick = { selectedFilter = JobStatus.REJECTED_NO_ATTACHMENT },
                        label = { Text("No Attachment ($noAttachCount)") }
                    )
                }
            }
        }

        if (filteredJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No records matching filter",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredJobs, key = { it.id }) { job ->
                JobLogDetailCard(
                    job = job,
                    currencySymbol = costMatrix.currencySymbol,
                    onDelete = { onDeleteJob(job.id) },
                    onReview = { onReviewPendingJob(job) }
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Logs?") },
            text = { Text("This will permanently delete all logged print jobs, auto-reply history, and income records.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllLogs()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JobLogDetailCard(
    job: PrintJob,
    currencySymbol: String,
    onDelete: () -> Unit,
    onReview: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedTime = remember(job.receivedAt) { dateFormat.format(Date(job.receivedAt)) }

    val (icon, iconColor, statusLabel) = when (job.status) {
        JobStatus.APPROVED_PRINTED -> Triple(Icons.Default.CheckCircle, PrintEmerald, "APPROVED & DISPATCHED")
        JobStatus.REJECTED_NO_ATTACHMENT -> Triple(Icons.Default.HighlightOff, PrintRose, "REJECTED: NO ATTACHMENT")
        JobStatus.REJECTED_PHOTO_OF_DOC -> Triple(Icons.Default.CameraAlt, PrintAmber, "REJECTED: PHOTO OF DOC")
        JobStatus.PENDING_APPROVAL -> Triple(Icons.Default.Warning, PrintCyan, "PENDING APPROVAL")
        else -> Triple(Icons.Default.Description, Slate700, "CANCELLED")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("job_log_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                    Text(
                        text = job.attachmentName ?: job.emailSubject,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (job.status == JobStatus.APPROVED_PRINTED) {
                    Text(
                        text = "+$currencySymbol${"%.2f".format(job.incomeEarned)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrintEmerald
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Details"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Sender: ${job.senderName} (${job.senderEmail})", style = MaterialTheme.typography.bodySmall)
                    Text("Subject: ${job.emailSubject}", style = MaterialTheme.typography.bodySmall)

                    if (job.attachmentName != null) {
                        Text("Attachment: ${job.attachmentName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }

                    if (job.status == JobStatus.APPROVED_PRINTED || job.status == JobStatus.PENDING_APPROVAL) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Printer: ${job.assignedPrinterName}", style = MaterialTheme.typography.bodySmall)
                        Text("Specs: ${job.pageCount} page(s) • ${job.paperSize} • ${job.printColorMode} • Copies: ${job.copies}", style = MaterialTheme.typography.bodySmall)
                        Text("Ink Coverage: ~${job.avgInkCoverage}% per page", style = MaterialTheme.typography.bodySmall)
                        Text("Total Cost: $currencySymbol${"%.2f".format(job.finalApprovedCost)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    if (job.replySent && !job.replyMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Automated Email Reply Sent to Sender:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(job.replyMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (job.status == JobStatus.PENDING_APPROVAL) {
                            OutlinedButton(
                                onClick = onReview,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Review & Approve")
                            }
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
