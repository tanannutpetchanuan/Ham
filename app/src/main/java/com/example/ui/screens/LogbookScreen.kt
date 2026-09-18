package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.RadioRedAlert
import com.example.util.ThaiHamParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogbookScreen(
    qsos: List<QsoEntry>,
    searchQuery: String,
    selectedBand: String?,
    selectedMode: String?,
    viewModel: HamLogViewModel,
    modifier: Modifier = Modifier
) {
    var qsoToDelete by remember { mutableStateOf<QsoEntry?>(null) }
    val bands = listOf("2m", "70cm", "40m", "20m", "15m", "10m")
    val modes = listOf("FM", "SSB", "CW", "FT8")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setLogSearchQuery(it) },
            placeholder = { Text("Search callsign, operator, QTH, notes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setLogSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("logbook_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips (Band & Mode)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedBand == null && selectedMode == null,
                onClick = {
                    viewModel.setBandFilter(null)
                    viewModel.setModeFilter(null)
                },
                label = { Text("All (${qsos.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RadioAmberPrimary,
                    selectedLabelColor = Color.Black
                )
            )

            bands.forEach { b ->
                FilterChip(
                    selected = selectedBand == b,
                    onClick = {
                        viewModel.setBandFilter(if (selectedBand == b) null else b)
                    },
                    label = { Text(b) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadioAmberPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            modes.forEach { m ->
                FilterChip(
                    selected = selectedMode == m,
                    onClick = {
                        viewModel.setModeFilter(if (selectedMode == m) null else m)
                    },
                    label = { Text(m) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadioCyanSecondary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // QSO List or Empty State
        if (qsos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No QSOs Found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search keyword or clear filters." else "Log your first contact on the Log QSO tab!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("logbook_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(qsos, key = { it.id }) { qso ->
                    QsoCardItem(
                        qso = qso,
                        onDelete = { qsoToDelete = qso },
                        onToggleQslSent = { viewModel.toggleQslSent(qso) },
                        onToggleQslRcvd = { viewModel.toggleQslRcvd(qso) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    qsoToDelete?.let { qso ->
        AlertDialog(
            onDismissRequest = { qsoToDelete = null },
            title = { Text("Delete QSO Log") },
            text = { Text("Are you sure you want to delete the QSO record with ${qso.callsign}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQso(qso)
                        qsoToDelete = null
                    }
                ) {
                    Text("Delete", color = RadioRedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { qsoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QsoCardItem(
    qso: QsoEntry,
    onDelete: () -> Unit,
    onToggleQslSent: () -> Unit,
    onToggleQslRcvd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bkkFormat = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm 'ICT'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        }
    }
    val dateTimeStr = remember(qso.timestamp) {
        bkkFormat.format(Date(qso.timestamp))
    }
    val analysis = remember(qso.callsign) {
        ThaiHamParser.parse(qso.callsign)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qso_card_${qso.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Callsign, Zone Badge, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = qso.callsign,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    )

                    if (analysis.isThai && analysis.callArea != null) {
                        Surface(
                            color = RadioAmberPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Z${analysis.callArea}",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    } else if (!analysis.isThai) {
                        Surface(
                            color = RadioCyanSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = analysis.country,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = RadioCyanSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    if (qso.radioId.isNotEmpty()) {
                        Surface(
                            color = RadioCyanSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DMR: ${qso.radioId}",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RadioCyanSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dateTimeStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete QSO",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Radio Technical Details: Band, Mode, Frequency, RST, Power
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${qso.band.uppercase()} • ${qso.mode} • ${qso.frequency} MHz",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (qso.powerWatts > 0) {
                        Text(
                            text = "${qso.powerWatts}W",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // RST reports
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TX: ${qso.rstSent}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioAmberPrimary
                        )
                    )
                    Text(
                        text = "RX: ${qso.rstRcvd}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioCyanSecondary
                        )
                    )
                }
            }

            // Operator name and QTH location
            if (qso.operatorName.isNotEmpty() || qso.qth.isNotEmpty() || qso.gridLocator.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (qso.operatorName.isNotEmpty()) {
                        Text(
                            text = qso.operatorName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    if (qso.qth.isNotEmpty() || qso.province.isNotEmpty()) {
                        val loc = listOf(qso.qth, qso.province).filter { it.isNotEmpty() }.joinToString(", ")
                        Text(
                            text = "• $loc",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (qso.gridLocator.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = qso.gridLocator,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = RadioCyanSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Notes
            if (qso.notes.isNotEmpty()) {
                Text(
                    text = qso.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            // QSL Status Pills (Tap to toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (qso.qslSent) RadioAmberPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        if (qso.qslSent) RadioAmberPrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .clickable { onToggleQslSent() }
                        .padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (qso.qslSent) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = RadioAmberPrimary, modifier = Modifier.size(12.dp))
                        }
                        Text(
                            text = if (qso.qslSent) "QSL Sent" else "QSL Unsent",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (qso.qslSent) RadioAmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    color = if (qso.qslRcvd) RadioGreenSignal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        if (qso.qslRcvd) RadioGreenSignal else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.clickable { onToggleQslRcvd() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (qso.qslRcvd) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = RadioGreenSignal, modifier = Modifier.size(12.dp))
                        }
                        Text(
                            text = if (qso.qslRcvd) "QSL Rcvd" else "QSL Pending",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (qso.qslRcvd) RadioGreenSignal else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
