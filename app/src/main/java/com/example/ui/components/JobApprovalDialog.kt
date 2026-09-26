package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CostMatrix
import com.example.data.model.PrintJob
import com.example.data.model.PrinterModel
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintBlue
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApprovalDialog(
    job: PrintJob,
    printers: List<PrinterModel>,
    costMatrix: CostMatrix,
    onDismiss: () -> Unit,
    onUpdateSettings: (printerId: Int, printerName: String, paperSize: String, colorMode: String, copies: Int, isDuplex: Boolean, customCost: Double?) -> Unit,
    onApprove: (PrintJob) -> Unit
) {
    val context = LocalContext.current
    var selectedPrinter by remember(job) {
        mutableStateOf(printers.firstOrNull { it.id == job.assignedPrinterId }
            ?: printers.firstOrNull()
            ?: PrinterModel(id = 1, name = job.assignedPrinterName))
    }
    var printerDropdownExpanded by remember { mutableStateOf(false) }

    var paperSize by remember(job) { mutableStateOf(job.paperSize) }
    var colorMode by remember(job) { mutableStateOf(job.printColorMode) }
    var copies by remember(job) { mutableIntStateOf(job.copies) }
    var isDuplex by remember(job) { mutableStateOf(job.isDuplex) }

    // Dynamic cost calculation based on current settings
    val currentCost = remember(selectedPrinter, paperSize, colorMode, copies, isDuplex, job) {
        val perPageCost = costMatrix.calculatePageCost(
            isColor = colorMode == "COLOR",
            inkCoveragePercent = job.avgInkCoverage,
            paperSize = paperSize,
            isDuplex = isDuplex
        )
        val total = (perPageCost * job.pageCount * copies) + costMatrix.baseServiceFee
        String.format("%.2f", total).toDouble()
    }

    var customCostInput by remember(currentCost) { mutableStateOf(currentCost.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_approval_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = "Print Dispatch",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Print Request Review",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Verify settings, cost & dispatch",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sender & Document Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = PrintCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = job.attachmentName ?: "Document.pdf",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "From: ${job.senderName} <${job.senderEmail}>",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Subject: ${job.emailSubject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "📄 ${job.pageCount} page(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "💧 Avg Ink: ${job.avgInkCoverage}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (job.avgInkCoverage > 25.0) PrintAmber else PrintEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Per-Page Ink Breakdown section
                    Text(
                        text = "Per-Page Ink Consumption",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (job.inkBreakdownJson.isNotBlank()) {
                                job.inkBreakdownJson.lines().take(5).forEach { line ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (job.pageCount > 5) {
                                    Text(
                                        text = "+ ${job.pageCount - 5} more page(s) analyzed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrintCyan
                                    )
                                }
                            } else {
                                Text(
                                    text = "Estimated standard text coverage: ~${job.avgInkCoverage}% per page",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Requirement: "notify user which model of printer is use, modify if needed"
                    Text(
                        text = "Assigned Printer Model",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = printerDropdownExpanded,
                        onExpandedChange = { printerDropdownExpanded = !printerDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedPrinter.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = printerDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("printer_select_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = printerDropdownExpanded,
                            onDismissRequest = { printerDropdownExpanded = false }
                        ) {
                            printers.forEach { printer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(printer.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${if (printer.isColor) "Color & B&W" else "Monochrome only"} • Ink: ${printer.blackInkLevelPercent}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPrinter = printer
                                        printerDropdownExpanded = false
                                        if (!printer.isColor && colorMode == "COLOR") {
                                            colorMode = "MONOCHROME"
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Print Settings Controls
                    Text(
                        text = "Print Settings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Color Mode Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = colorMode == "MONOCHROME",
                            onClick = { colorMode = "MONOCHROME" },
                            label = { Text("Black & White") },
                            leadingIcon = { Icon(Icons.Default.InvertColors, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("mode_mono_chip")
                        )
                        FilterChip(
                            selected = colorMode == "COLOR",
                            onClick = {
                                if (selectedPrinter.isColor) {
                                    colorMode = "COLOR"
                                }
                            },
                            enabled = selectedPrinter.isColor,
                            label = { Text("Color") },
                            leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("mode_color_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Paper Size & Copies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("A4", "Letter", "Legal").forEach { size ->
                                FilterChip(
                                    selected = paperSize.equals(size, ignoreCase = true),
                                    onClick = { paperSize = size },
                                    label = { Text(size) }
                                )
                            }
                        }

                        // Copies counter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (copies > 1) copies-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease copies", modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$copies",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (copies < 99) copies++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase copies", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duplex Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("2-Sided (Duplex)", fontWeight = FontWeight.Medium)
                            Text(
                                "${costMatrix.duplexDiscountPercent.toInt()}% paper discount applied",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDuplex,
                            onCheckedChange = { isDuplex = it },
                            modifier = Modifier.testTag("duplex_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Cost Calculation & Editable Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Cost to Sender",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Based on ink matrix: ${job.pageCount}p × $copies copy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Prominent price display
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${costMatrix.currencySymbol}${"%.2f".format(currentCost)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Requirement:
                    // "then when approved button, reply to sender the cost only, then sends the file to the printer."
                    Button(
                        onClick = {
                            val parsedCost = customCostInput.toDoubleOrNull() ?: currentCost
                            val updatedJob = job.copy(
                                assignedPrinterId = selectedPrinter.id,
                                assignedPrinterName = selectedPrinter.name,
                                paperSize = paperSize,
                                printColorMode = colorMode,
                                copies = copies,
                                isDuplex = isDuplex,
                                finalApprovedCost = parsedCost
                            )
                            onApprove(updatedJob)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("approve_dispatch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Approve: Reply Cost & Print",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Dismiss / Review Later")
                    }
                }
            }
        }
    )
}
