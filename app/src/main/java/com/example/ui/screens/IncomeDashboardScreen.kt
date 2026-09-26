package com.example.ui.screens

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomeDashboardScreen(
    jobs: List<PrintJob>,
    costMatrix: CostMatrix
) {
    val approvedJobs = jobs.filter { it.status == JobStatus.APPROVED_PRINTED }
    val totalIncome = approvedJobs.sumOf { it.incomeEarned }
    val totalPages = approvedJobs.sumOf { it.pageCount * it.copies }
    val totalColorJobs = approvedJobs.count { it.printColorMode == "COLOR" }
    val totalMonoJobs = approvedJobs.count { it.printColorMode == "MONOCHROME" }

    val colorIncome = approvedJobs.filter { it.printColorMode == "COLOR" }.sumOf { it.incomeEarned }
    val monoIncome = approvedJobs.filter { it.printColorMode == "MONOCHROME" }.sumOf { it.incomeEarned }

    val avgTicket = if (approvedJobs.isNotEmpty()) totalIncome / approvedJobs.size else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("income_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Income Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrintEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = PrintEmerald, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Total Print Revenue",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${costMatrix.currencySymbol}${"%.2f".format(totalIncome)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Dispatched Jobs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${approvedJobs.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Pages Printed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("$totalPages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Average Ticket", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("${costMatrix.currencySymbol}${"%.2f".format(avgTicket)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Color vs Monochrome Revenue Share
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Revenue by Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Color progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = PrintCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Color Prints ($totalColorJobs)")
                        }
                        Text(
                            text = "${costMatrix.currencySymbol}${"%.2f".format(colorIncome)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val colorRatio = if (totalIncome > 0) (colorIncome / totalIncome).toFloat() else 0.5f
                    LinearProgressIndicator(
                        progress = { colorRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrintCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // B&W progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InvertColors, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("B&W Prints ($totalMonoJobs)")
                        }
                        Text(
                            text = "${costMatrix.currencySymbol}${"%.2f".format(monoIncome)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val monoRatio = if (totalIncome > 0) (monoIncome / totalIncome).toFloat() else 0.5f
                    LinearProgressIndicator(
                        progress = { monoRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Revenue Log Entries
        item {
            Text(
                text = "Revenue Transactions (${approvedJobs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (approvedJobs.isEmpty()) {
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
                        Text(
                            text = "No dispatched revenue recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Approve incoming print requests to record dispatch income.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(approvedJobs, key = { it.id }) { job ->
                val dateFmt = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                val timeStr = dateFmt.format(Date(job.completedAt ?: job.receivedAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = job.attachmentName ?: "Print Job #${job.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${job.senderName} • ${job.pageCount}p on ${job.assignedPrinterName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = "+${costMatrix.currencySymbol}${"%.2f".format(job.incomeEarned)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrintEmerald
                        )
                    }
                }
            }
        }
    }
}
