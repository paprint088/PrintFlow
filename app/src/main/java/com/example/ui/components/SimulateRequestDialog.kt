package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.theme.PrintAmber
import com.example.ui.theme.PrintCyan
import com.example.ui.theme.PrintEmerald
import com.example.ui.theme.PrintRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateRequestDialog(
    onDismiss: () -> Unit,
    onSelectScenario: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_request_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Simulate Incoming Gmail",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verify automated handlers & rules",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scenario 1: No attachment
                    ScenarioCard(
                        title = "1. Email With No Attachment",
                        description = "Triggers automated reply: 'no files attached' and notifies user.",
                        icon = Icons.Default.HighlightOff,
                        iconColor = PrintRose,
                        testTag = "sim_no_attachment_btn",
                        onClick = {
                            onSelectScenario("NO_ATTACHMENT")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scenario 2: Photograph of document
                    ScenarioCard(
                        title = "2. Phone Photo of Document",
                        description = "Triggers automated reply: 'cannot be printed, not standard format' and notifies user.",
                        icon = Icons.Default.CameraAlt,
                        iconColor = PrintAmber,
                        testTag = "sim_photo_doc_btn",
                        onClick = {
                            onSelectScenario("PHOTO_OF_DOC")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scenario 3: Valid Printable Document (Color)
                    ScenarioCard(
                        title = "3. Valid Color PDF Document",
                        description = "Calculates per-page ink consumption, assigns printer, notifies user with cost & settings for approval.",
                        icon = Icons.Default.PictureAsPdf,
                        iconColor = PrintEmerald,
                        testTag = "sim_valid_color_btn",
                        onClick = {
                            onSelectScenario("VALID_DOCUMENT_COLOR")
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scenario 4: Valid Printable Document (B&W)
                    ScenarioCard(
                        title = "4. Valid B&W Document",
                        description = "Monochrome legal draft, page ink analysis, printer model assignment and approval dispatch.",
                        icon = Icons.Default.Description,
                        iconColor = PrintCyan,
                        testTag = "sim_valid_bw_btn",
                        onClick = {
                            onSelectScenario("VALID_DOCUMENT_BW")
                            onDismiss()
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun ScenarioCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
