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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.data.model.PrinterModel
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald

@Composable
fun PrintersScreen(
    printers: List<PrinterModel>,
    onAddPrinter: (PrinterModel) -> Unit,
    onUpdatePrinter: (PrinterModel) -> Unit,
    onDeletePrinter: (PrinterModel) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("printers_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Printer Models",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Manage print destinations and monitor tank levels.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_printer_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }

        items(printers, key = { it.id }) { printer ->
            PrinterCard(
                printer = printer,
                onSetDefault = {
                    onUpdatePrinter(printer.copy(isDefault = true))
                },
                onDelete = {
                    onDeletePrinter(printer)
                }
            )
        }
    }

    if (showAddDialog) {
        AddPrinterDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newPrinter ->
                onAddPrinter(newPrinter)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PrinterCard(
    printer: PrinterModel,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("printer_card_${printer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = printer.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (printer.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrintEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DEFAULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrintEmerald
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${printer.location} • ${if (printer.isColor) "Color & Monochrome" else "Monochrome only"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!printer.isDefault) {
                    IconButton(onClick = onSetDefault) {
                        Icon(
                            Icons.Default.StarOutline,
                            contentDescription = "Set Default",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ink Level indicators
            Text(
                text = "Ink Cartridge Levels",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Black ink level
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Black Ink", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(70.dp))
                LinearProgressIndicator(
                    progress = { printer.blackInkLevelPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.Black,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${printer.blackInkLevelPercent}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            if (printer.isColor) {
                Spacer(modifier = Modifier.height(6.dp))
                // Color ink level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color Ink", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(70.dp))
                    LinearProgressIndicator(
                        progress = { printer.colorInkLevelPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrintCyan,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${printer.colorInkLevelPercent}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duplex 2-Sided: ${if (printer.isDuplexSupported) "Supported" else "Manual"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!printer.isDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove Printer",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPrinterDialog(
    onDismiss: () -> Unit,
    onAdd: (PrinterModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Counter 1") }
    var isColor by remember { mutableStateOf(true) }
    var isDuplex by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Printer Model") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Printer Name (e.g. Epson L3210)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supports Color")
                    Switch(checked = isColor, onCheckedChange = { isColor = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Automatic Duplex")
                    Switch(checked = isDuplex, onCheckedChange = { isDuplex = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            PrinterModel(
                                name = name.trim(),
                                location = location.trim(),
                                isColor = isColor,
                                isDuplexSupported = isDuplex
                            )
                        )
                    }
                }
            ) {
                Text("Add Printer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
