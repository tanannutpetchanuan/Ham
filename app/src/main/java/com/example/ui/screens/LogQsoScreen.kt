package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HamLogViewModel
import com.example.ui.LoggingFormState
import com.example.ui.theme.RadioAmberPrimary
import com.example.ui.theme.RadioCyanSecondary
import com.example.ui.theme.RadioGreenSignal
import com.example.ui.theme.RadioRedAlert

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogQsoScreen(
    formState: LoggingFormState,
    viewModel: HamLogViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val bands = listOf("2m", "70cm", "40m", "20m", "15m", "10m", "6m")
    val modes = listOf("FM", "DMR", "SSB", "CW", "FT8", "FT4")
    val rstPresets = if (formState.mode == "CW" || formState.mode.startsWith("FT")) {
        listOf("599", "579", "559")
    } else {
        listOf("59", "57", "55")
    }
    val powerPresets = listOf("5", "10", "50", "100")

    var liveCurrentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            liveCurrentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val bkkDateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        }
    }
    val utcTimeFormat = remember {
        SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val liveDate = Date(liveCurrentTime)
    val bkkTimeStr = bkkDateFormat.format(liveDate)
    val utcTimeStr = utcTimeFormat.format(liveDate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Logging Time Header: UTC+7 Bangkok
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "QSO Logging Time",
                        tint = RadioAmberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "QSO Time (UTC+7 Bangkok):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "$bkkTimeStr ICT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioAmberPrimary
                        )
                    )
                }
                Text(
                    text = utcTimeStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
        // Status confirmation message
        AnimatedVisibility(
            visible = formState.statusMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            formState.statusMessage?.let { msg ->
                Surface(
                    color = RadioGreenSignal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadioGreenSignal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RadioGreenSignal
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearStatusMessage() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Callsign & RadioID.net Automatic Lookup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CALLSIGN & RADIOID LOOKUP",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RadioAmberPrimary,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    if (formState.previousQsoCount > 0) {
                        Surface(
                            color = RadioCyanSecondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Worked ${formState.previousQsoCount}x previously",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RadioCyanSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Dual Entry Fields: Callsign and Radio ID (DMR Code)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Callsign Input field
                    OutlinedTextField(
                        value = formState.callsign,
                        onValueChange = { viewModel.onCallsignChanged(it) },
                        label = { Text("Callsign (HS1AB...)") },
                        placeholder = { Text("Callsign") },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("callsign_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        ),
                        trailingIcon = {
                            if (formState.callsign.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onCallsignChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear callsign", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadioAmberPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Radio ID (DMR Code) Input field
                    OutlinedTextField(
                        value = formState.radioId,
                        onValueChange = { viewModel.onRadioIdChanged(it) },
                        label = { Text("Radio ID (DMR)") },
                        placeholder = { Text("e.g. 5201001") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("radioid_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioCyanSecondary
                        ),
                        trailingIcon = {
                            if (formState.radioId.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onRadioIdChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Radio ID", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadioCyanSecondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Connect to RadioID.net Live Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enter Callsign or Radio ID code to look up on RadioID.net",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { viewModel.lookupRadioIdNet() },
                        enabled = !formState.isRadioIdLoading && (formState.radioId.isNotEmpty() || formState.callsign.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadioCyanSecondary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("lookup_radioid_button")
                    ) {
                        if (formState.isRadioIdLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connecting...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RadioID.net",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // RadioID.net Error Message
                formState.radioIdError?.let { err ->
                    Surface(
                        color = RadioRedAlert.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RadioRedAlert),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RadioRedAlert, modifier = Modifier.size(18.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // RadioID.net Success Card
                formState.radioIdResultUser?.let { user ->
                    Surface(
                        color = RadioCyanSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RadioCyanSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = RadioCyanSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "RadioID.net Verified DMR Station",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RadioCyanSecondary
                                        )
                                    )
                                }
                                Surface(
                                    color = RadioCyanSecondary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ID: ${user.radioId}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "${user.callsign} • ${user.fullName}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            val loc = listOf(user.city, user.state, user.country).filter { it.isNotBlank() }.joinToString(", ")
                            if (loc.isNotBlank()) {
                                Text(
                                    text = loc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Live Automatic Callsign Analysis Card (Thai zoning, ITU, CQ)
                formState.analysis?.let { analysis ->
                    Surface(
                        color = if (analysis.isThai) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (analysis.isThai) RadioAmberPrimary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (analysis.isThai) "🇹🇭" else "🌐",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = analysis.country,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (analysis.callArea != null) {
                                        Surface(
                                            color = RadioAmberPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ZONE ${analysis.callArea}",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "ITU ${analysis.ituZone} • CQ ${analysis.cqZone}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            if (analysis.isThai && analysis.provinceHint.isNotEmpty()) {
                                Text(
                                    text = analysis.areaTitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = RadioAmberPrimary
                                    )
                                )
                                Text(
                                    text = "Provinces: ${analysis.provinceHint}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 2
                                )
                            }

                            // Match indicator in Offline Directory
                            if (formState.offlineMatch != null) {
                                Surface(
                                    color = RadioGreenSignal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = RadioGreenSignal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Offline Database Match: ${formState.offlineMatch.operatorName}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = RadioGreenSignal
                                                )
                                            )
                                            if (formState.offlineMatch.clubAffiliation.isNotEmpty()) {
                                                Text(
                                                    text = "Affiliation: ${formState.offlineMatch.clubAffiliation} • ${formState.offlineMatch.licenseClass}",
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
                    }
                }
            }
        }

        // Section 2: Frequency, Band & Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "BAND & OPERATING MODE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadioAmberPrimary,
                        letterSpacing = 1.sp
                    )
                )

                // Band selector chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bands.forEach { b ->
                        FilterChip(
                            selected = formState.band == b,
                            onClick = { viewModel.onBandSelected(b) },
                            label = { Text(b) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RadioAmberPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Frequency input (MHz)
                    OutlinedTextField(
                        value = formState.frequency,
                        onValueChange = { viewModel.onFrequencyChanged(it) },
                        label = { Text("Frequency (MHz)") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("frequency_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    // Power Watts
                    OutlinedTextField(
                        value = formState.powerWatts,
                        onValueChange = { viewModel.onPowerChanged(it) },
                        label = { Text("Power (W)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("power_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Power Presets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Power:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    powerPresets.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (formState.powerWatts == p) RadioAmberPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (formState.powerWatts == p) RadioAmberPrimary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.clickable { viewModel.onPowerChanged(p) }
                        ) {
                            Text(
                                text = "${p}W",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (formState.powerWatts == p) RadioAmberPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Mode selector chips
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    modes.forEach { m ->
                        FilterChip(
                            selected = formState.mode == m,
                            onClick = { viewModel.onModeSelected(m) },
                            label = { Text(m) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RadioCyanSecondary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Signal Report (RST)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SIGNAL REPORT (RST)",
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
                    OutlinedTextField(
                        value = formState.rstSent,
                        onValueChange = { viewModel.onRstSentChanged(it) },
                        label = { Text("RST Sent (TX)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rst_sent_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioAmberPrimary
                        )
                    )

                    OutlinedTextField(
                        value = formState.rstRcvd,
                        onValueChange = { viewModel.onRstRcvdChanged(it) },
                        label = { Text("RST Rcvd (RX)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rst_rcvd_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadioCyanSecondary
                        )
                    )
                }

                // Quick RST buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Reports:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    rstPresets.forEach { rst ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable {
                                viewModel.onRstSentChanged(rst)
                                viewModel.onRstRcvdChanged(rst)
                            }
                        ) {
                            Text(
                                text = rst,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Operator & Location Details (Auto-filled by lookup)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "STATION & OPERATOR DETAILS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadioAmberPrimary,
                        letterSpacing = 1.sp
                    )
                )

                OutlinedTextField(
                    value = formState.operatorName,
                    onValueChange = { viewModel.onOperatorNameChanged(it) },
                    label = { Text("Operator Name") },
                    placeholder = { Text("Name / Club name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("operator_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.qth,
                        onValueChange = { viewModel.onQthChanged(it) },
                        label = { Text("QTH (City/District)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qth_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = formState.province,
                        onValueChange = { viewModel.onProvinceChanged(it) },
                        label = { Text("Province / State") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("province_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.gridLocator,
                        onValueChange = { viewModel.onGridLocatorChanged(it) },
                        label = { Text("Grid Square (e.g. OK03)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("grid_locator_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (formState.radioId.isNotEmpty()) {
                        OutlinedTextField(
                            value = formState.radioId,
                            onValueChange = { viewModel.onRadioIdChanged(it) },
                            label = { Text("Radio ID (DMR)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("operator_radioid_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                color = RadioCyanSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { viewModel.onNotesChanged(it) },
                    label = { Text("Remarks / Notes") },
                    placeholder = { Text("e.g. DMR TG 520, Repeater HS1AB, Net Check-in, 73") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input"),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // QSL Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = formState.qslSent,
                            onCheckedChange = { viewModel.onQslSentToggled(it) },
                            colors = CheckboxDefaults.colors(checkedColor = RadioAmberPrimary)
                        )
                        Text("QSL Sent", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = formState.qslRcvd,
                            onCheckedChange = { viewModel.onQslRcvdToggled(it) },
                            colors = CheckboxDefaults.colors(checkedColor = RadioGreenSignal)
                        )
                        Text("QSL Received", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Primary Action: LOG QSO
        Button(
            onClick = { viewModel.logCurrentQso() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("log_qso_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = RadioAmberPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "LOG QSO (73)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
