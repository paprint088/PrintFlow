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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CostMatrix
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald

@Composable
fun CostMatrixScreen(
    currentMatrix: CostMatrix,
    onSaveMatrix: (CostMatrix) -> Unit
) {
    var currencySymbol by remember(currentMatrix) { mutableStateOf(currentMatrix.currencySymbol) }

    var monoLow by remember(currentMatrix) { mutableStateOf(currentMatrix.monochromeLowCoveragePrice.toString()) }
    var monoHigh by remember(currentMatrix) { mutableStateOf(currentMatrix.monochromeHighCoveragePrice.toString()) }

    var colorLow by remember(currentMatrix) { mutableStateOf(currentMatrix.colorLowCoveragePrice.toString()) }
    var colorMed by remember(currentMatrix) { mutableStateOf(currentMatrix.colorMediumCoveragePrice.toString()) }
    var colorHigh by remember(currentMatrix) { mutableStateOf(currentMatrix.colorHighCoveragePrice.toString()) }

    var paperA4 by remember(currentMatrix) { mutableStateOf(currentMatrix.paperCostA4.toString()) }
    var paperLetter by remember(currentMatrix) { mutableStateOf(currentMatrix.paperCostLetter.toString()) }
    var paperLegal by remember(currentMatrix) { mutableStateOf(currentMatrix.paperCostLegal.toString()) }

    var duplexDiscount by remember(currentMatrix) { mutableStateOf(currentMatrix.duplexDiscountPercent.toString()) }
    var baseServiceFee by remember(currentMatrix) { mutableStateOf(currentMatrix.baseServiceFee.toString()) }

    // Live preview calculator inputs
    var testPages by remember { mutableStateOf("5") }
    var testInkCoverage by remember { mutableStateOf("12.5") }
    var testIsColor by remember { mutableStateOf(false) }

    val previewCost = remember(
        currencySymbol, monoLow, monoHigh, colorLow, colorMed, colorHigh,
        paperA4, duplexDiscount, testPages, testInkCoverage, testIsColor
    ) {
        val matrix = CostMatrix(
            currencySymbol = currencySymbol,
            monochromeLowCoveragePrice = monoLow.toDoubleOrNull() ?: 0.05,
            monochromeHighCoveragePrice = monoHigh.toDoubleOrNull() ?: 0.10,
            colorLowCoveragePrice = colorLow.toDoubleOrNull() ?: 0.15,
            colorMediumCoveragePrice = colorMed.toDoubleOrNull() ?: 0.25,
            colorHighCoveragePrice = colorHigh.toDoubleOrNull() ?: 0.45,
            paperCostA4 = paperA4.toDoubleOrNull() ?: 0.02,
            duplexDiscountPercent = duplexDiscount.toDoubleOrNull() ?: 15.0
        )
        val pages = testPages.toIntOrNull() ?: 1
        val ink = testInkCoverage.toDoubleOrNull() ?: 10.0
        val costPerPage = matrix.calculatePageCost(testIsColor, ink, "A4", false)
        String.format("%.2f", costPerPage * pages)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cost_matrix_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Customizable Print Cost Matrix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Set price rules per page based on ink consumption %, paper types, and color density.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Currency Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("$", "₱", "€", "£", "¥").forEach { sym ->
                            FilterChip(
                                selected = currencySymbol == sym,
                                onClick = { currencySymbol = sym },
                                label = { Text(sym, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        }

        // B&W Ink Pricing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.InvertColors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monochrome (Black & White) Rates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = monoLow,
                            onValueChange = { monoLow = it },
                            label = { Text("Light Text (<10%)") },
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("mono_low_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = monoHigh,
                            onValueChange = { monoHigh = it },
                            label = { Text("Dense Text (≥10%)") },
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("mono_high_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Color Ink Pricing Tiers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = PrintAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Color Ink Coverage Tiers",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = colorLow,
                        onValueChange = { colorLow = it },
                        label = { Text("Low Color Coverage (<15% logo / highlight)") },
                        prefix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("color_low_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = colorMed,
                        onValueChange = { colorMed = it },
                        label = { Text("Medium Color Coverage (15% - 30% diagrams / charts)") },
                        prefix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("color_med_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = colorHigh,
                        onValueChange = { colorHigh = it },
                        label = { Text("High Color / Photo Coverage (>30%)") },
                        prefix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("color_high_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Paper Costs & Duplex Discount
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = PrintEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paper Substrates & Finishing",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = paperA4,
                            onValueChange = { paperA4 = it },
                            label = { Text("A4 Sheet") },
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = paperLetter,
                            onValueChange = { paperLetter = it },
                            label = { Text("Letter") },
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = paperLegal,
                            onValueChange = { paperLegal = it },
                            label = { Text("Legal") },
                            prefix = { Text(currencySymbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = duplexDiscount,
                        onValueChange = { duplexDiscount = it },
                        label = { Text("2-Sided (Duplex) Discount (%)") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Live Interactive Matrix Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Matrix Cost Test Calculator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testPages,
                            onValueChange = { testPages = it },
                            label = { Text("Pages") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = testInkCoverage,
                            onValueChange = { testInkCoverage = it },
                            label = { Text("Avg Ink %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !testIsColor,
                                onClick = { testIsColor = false },
                                label = { Text("B&W") }
                            )
                            FilterChip(
                                selected = testIsColor,
                                onClick = { testIsColor = true },
                                label = { Text("Color") }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Est: $currencySymbol$previewCost",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = CostMatrix(
                        currencySymbol = currencySymbol,
                        monochromeLowCoveragePrice = monoLow.toDoubleOrNull() ?: 0.05,
                        monochromeHighCoveragePrice = monoHigh.toDoubleOrNull() ?: 0.10,
                        colorLowCoveragePrice = colorLow.toDoubleOrNull() ?: 0.15,
                        colorMediumCoveragePrice = colorMed.toDoubleOrNull() ?: 0.25,
                        colorHighCoveragePrice = colorHigh.toDoubleOrNull() ?: 0.45,
                        paperCostA4 = paperA4.toDoubleOrNull() ?: 0.02,
                        paperCostLetter = paperLetter.toDoubleOrNull() ?: 0.02,
                        paperCostLegal = paperLegal.toDoubleOrNull() ?: 0.04,
                        duplexDiscountPercent = duplexDiscount.toDoubleOrNull() ?: 15.0,
                        baseServiceFee = baseServiceFee.toDoubleOrNull() ?: 0.0
                    )
                    onSaveMatrix(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_cost_matrix_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Matrix Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
