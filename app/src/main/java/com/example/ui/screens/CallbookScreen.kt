package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallsignEntry
import com.example.data.remote.RadioIdApiClient
import com.example.ui.HamLogViewModel
import com.example.ui.theme.RadioAmberPrimary
import com.example.ui.theme.RadioCyanSecondary
import com.example.ui.theme.RadioGreenSignal
import com.example.ui.theme.RadioRedAlert
import com.example.util.ThaiHamParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallbookScreen(
    callsigns: List<CallsignEntry>,
    searchQuery: String,
    selectedArea: Int?,
    viewModel: HamLogViewModel,
    onSelectForLog: (CallsignEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showZoneGuide by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<CallsignEntry?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setDirectorySearchQuery(it) },
                placeholder = { Text("Search callsign, DMR ID, name, province, club...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setDirectorySearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("directory_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Thai Call Area Quick Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedArea == null,
                    onClick = { viewModel.setAreaFilter(null) },
                    label = { Text("All (${callsigns.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RadioAmberPrimary,
                        selectedLabelColor = Color.Black
                    )
                )

                (0..9).forEach { zone ->
                    FilterChip(
                        selected = selectedArea == zone,
                        onClick = {
                            viewModel.setAreaFilter(if (selectedArea == zone) null else zone)
                        },
                        label = { Text("Zone $zone") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RadioAmberPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // Expandable Thai Zone Reference Guide Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showZoneGuide = !showZoneGuide },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RadioAmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Thai Amateur Radio Zone Guide (HS/E2)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Icon(
                            imageVector = if (showZoneGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = showZoneGuide) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ThaiHamParser.getAllZones().forEach { (zone, info) ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = info.first,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RadioAmberPrimary
                                            )
                                        )
                                        Text(
                                            text = "Grid: ${info.third}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = RadioCyanSecondary
                                            )
                                        )
                                    }
                                    Text(
                                        text = info.second,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Callsign List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("directory_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(callsigns, key = { it.callsign }) { entry ->
                    CallsignDirectoryCard(
                        entry = entry,
                        onUseForQso = { onSelectForLog(entry) },
                        onDelete = if (entry.isCustomUserAdded) { { entryToDelete = entry } } else null
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Floating Action Button to Add Operator to Offline DB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_callsign_fab"),
            containerColor = RadioAmberPrimary,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Call to Offline Database")
        }
    }

    // Add Callsign Dialog with RadioID.net integration
    if (showAddDialog) {
        AddCallsignDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newEntry ->
                viewModel.addCallsignToDirectory(newEntry)
                showAddDialog = false
            }
        )
    }

    // Delete custom station confirmation
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Remove from Directory") },
            text = { Text("Remove ${entry.callsign} (${entry.operatorName}) from your offline directory?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCallsignFromDirectory(entry)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = RadioRedAlert)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CallsignDirectoryCard(
    entry: CallsignEntry,
    onUseForQso: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUseForQso() }
            .testTag("callsign_card_${entry.callsign}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
                        text = entry.callsign,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Surface(
                        color = RadioAmberPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Zone ${entry.callArea}",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 10.sp
                            )
                        )
                    }

                    if (entry.radioId.isNotEmpty()) {
                        Surface(
                            color = RadioCyanSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DMR: ${entry.radioId}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RadioCyanSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    if (entry.isCustomUserAdded) {
                        Surface(
                            color = RadioGreenSignal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Personal",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RadioGreenSignal,
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
                    if (entry.gridLocator.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = entry.gridLocator,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = RadioCyanSecondary
                                )
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete entry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = entry.operatorName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (entry.province.isNotEmpty() || entry.qth.isNotEmpty()) {
                val loc = listOf(entry.qth, entry.province).filter { it.isNotEmpty() }.joinToString(", ")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = loc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (entry.notes.isNotEmpty()) {
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun AddCallsignDialog(
    onDismiss: () -> Unit,
    onSave: (CallsignEntry) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var callsign by remember { mutableStateOf("") }
    var radioId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var qth by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var grid by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoadingRadioId by remember { mutableStateOf(false) }
    var lookupStatus by remember { mutableStateOf<String?>(null) }

    val analysis = remember(callsign) {
        if (callsign.isNotEmpty()) ThaiHamParser.parse(callsign) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to Offline Directory")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // RadioID.net fetch button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val target = radioId.ifEmpty { callsign }
                            if (target.isNotBlank()) {
                                isLoadingRadioId = true
                                lookupStatus = null
                                coroutineScope.launch {
                                    val result = RadioIdApiClient().lookup(target)
                                    isLoadingRadioId = false
                                    result.fold(
                                        onSuccess = { user ->
                                            if (user != null) {
                                                callsign = user.callsign
                                                radioId = user.radioId
                                                if (name.isEmpty()) name = user.fullName
                                                if (qth.isEmpty()) qth = user.city
                                                if (province.isEmpty()) province = user.state
                                                val parsed = ThaiHamParser.parse(user.callsign)
                                                if (grid.isEmpty()) grid = parsed.defaultGrid
                                                lookupStatus = "Found: ${user.callsign} (${user.fullName})"
                                            } else {
                                                lookupStatus = "No record on RadioID.net for '$target'"
                                            }
                                        },
                                        onFailure = { err ->
                                            lookupStatus = "Error: ${err.localizedMessage}"
                                        }
                                    )
                                }
                            }
                        },
                        enabled = !isLoadingRadioId && (callsign.isNotBlank() || radioId.isNotBlank()),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoadingRadioId) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connecting RadioID.net...")
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-fill from RadioID.net")
                        }
                    }
                }

                lookupStatus?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (status.startsWith("Found")) RadioGreenSignal else RadioAmberPrimary,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = callsign,
                        onValueChange = {
                            val up = it.uppercase()
                            callsign = up
                            if (grid.isEmpty() && up.isNotEmpty()) {
                                val a = ThaiHamParser.parse(up)
                                grid = a.defaultGrid
                                if (province.isEmpty() && a.isThai) {
                                    province = a.areaTitle
                                }
                            }
                        },
                        label = { Text("Callsign") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = radioId,
                        onValueChange = { radioId = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Radio ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Operator / Station Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qth,
                    onValueChange = { qth = it },
                    label = { Text("QTH (City/District)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = province,
                    onValueChange = { province = it },
                    label = { Text("Province") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = grid,
                    onValueChange = { grid = it.uppercase() },
                    label = { Text("Maidenhead Grid (e.g. OK03)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Repeater Info") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (callsign.isNotBlank()) {
                        val area = analysis?.callArea ?: 1
                        onSave(
                            CallsignEntry(
                                callsign = callsign.trim().uppercase(),
                                operatorName = name.trim(),
                                qth = qth.trim(),
                                province = province.trim(),
                                callArea = area,
                                gridLocator = grid.trim().uppercase(),
                                radioId = radioId.trim(),
                                licenseClass = analysis?.licenseClassHint ?: "General",
                                notes = notes.trim(),
                                isCustomUserAdded = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadioAmberPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Save Station")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
