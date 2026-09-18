package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QsoEntry
import com.example.ui.HamLogViewModel
import com.example.ui.theme.RadioAmberPrimary
import com.example.ui.theme.RadioCyanSecondary
import com.example.ui.theme.RadioGreenSignal
import com.example.util.ThaiHamParser

@Composable
fun StatsExportScreen(
    qsos: List<QsoEntry>,
    viewModel: HamLogViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showImportDialog by remember { mutableStateOf(false) }
    var copiedToClipboard by remember { mutableStateOf(false) }

    val totalQsos = qsos.size
    val thaiQsos = qsos.count { ThaiHamParser.parse(it.callsign).isThai }
    val dxQsos = totalQsos - thaiQsos
    val qslSentCount = qsos.count { it.qslSent }
    val qslRcvdCount = qsos.count { it.qslRcvd }

    val bandCounts = qsos.groupingBy { it.band.uppercase() }.eachCount().toList().sortedByDescending { it.second }
    val modeCounts = qsos.groupingBy { it.mode.uppercase() }.eachCount().toList().sortedByDescending { it.second }

    val thaiAreaCounts = qsos
        .mapNotNull { ThaiHamParser.parse(it.callsign).callArea }
        .groupingBy { it }
        .eachCount()

    val adifExportText = remember(qsos) {
        viewModel.exportAdif()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-level Station Statistics
        Text(
            text = "STATION QSO METRICS",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = RadioAmberPrimary,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "TOTAL QSOS",
                value = "$totalQsos",
                accentColor = RadioAmberPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "THAI STATIONS",
                value = "$thaiQsos",
                accentColor = RadioGreenSignal,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "DX STATIONS",
                value = "$dxQsos",
                accentColor = RadioCyanSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "QSL CARDS SENT",
                value = "$qslSentCount",
                accentColor = RadioAmberPrimary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "QSL RECEIVED",
                value = "$qslRcvdCount",
                accentColor = RadioGreenSignal,
                modifier = Modifier.weight(1f)
            )
        }

        // Band & Mode Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "BAND DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadioAmberPrimary
                    )
                )

                if (bandCounts.isEmpty()) {
                    Text(
                        text = "No QSOs logged yet.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else {
                    bandCounts.forEach { (band, count) ->
                        val ratio = count.toFloat() / totalQsos.coerceAtLeast(1)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = band,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$count (${(ratio * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(6.dp)
                                        .background(RadioAmberPrimary, RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MODE DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadioCyanSecondary
                    )
                )

                if (modeCounts.isEmpty()) {
                    Text(
                        text = "No QSOs logged yet.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else {
                    modeCounts.forEach { (mode, count) ->
                        val ratio = count.toFloat() / totalQsos.coerceAtLeast(1)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$count (${(ratio * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(6.dp)
                                        .background(RadioCyanSecondary, RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Standard ADIF Export & Import
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ADIF LOG INTERCHANGE (3.1.4)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadioAmberPrimary,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "Export your logs in Amateur Data Interchange Format (ADIF) to upload to LoTW, eQSL, QRZ, or contest checkers.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ADIF Log", adifExportText)
                            clipboard.setPrimaryClip(clip)
                            copiedToClipboard = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_adif_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadioAmberPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (copiedToClipboard) "Copied ADIF!" else "Copy ADIF",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_adif_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Import ADIF")
                        }
                    }
                }

                // Scrollable ADIF Preview snippet
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = adifExportText.take(600) + if (adifExportText.length > 600) "\n... (${adifExportText.length} bytes total)" else "",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Import Dialog
    if (showImportDialog) {
        ImportAdifDialog(
            onDismiss = { showImportDialog = false },
            onImport = { content ->
                viewModel.importAdifLogs(content) { count ->
                    showImportDialog = false
                }
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
    }
}

@Composable
fun ImportAdifDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var adifText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import ADIF Logs") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Paste standard ADIF (.adi) text below to import QSOs into your offline database:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = adifText,
                    onValueChange = { adifText = it },
                    placeholder = { Text("<CALL:5>HS1AB <BAND:2>2M <MODE:2>FM <FREQ:7>144.900 <EOR>") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (adifText.isNotBlank()) {
                        onImport(adifText)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadioAmberPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
