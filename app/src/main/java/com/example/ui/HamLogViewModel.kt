package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallsignEntry
import com.example.data.model.QsoEntry
import com.example.data.model.RadioIdUser
import com.example.data.repository.HamLogRepository
import com.example.util.AdifConverter
import com.example.util.CallsignAnalysis
import com.example.util.ThaiHamParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class LoggingFormState(
    val callsign: String = "",
    val radioId: String = "",
    val isRadioIdLoading: Boolean = false,
    val radioIdResultUser: RadioIdUser? = null,
    val radioIdError: String? = null,
    val analysis: CallsignAnalysis? = null,
    val offlineMatch: CallsignEntry? = null,
    val previousQsoCount: Int = 0,
    val band: String = "2m",
    val frequency: String = "144.900",
    val mode: String = "FM",
    val rstSent: String = "59",
    val rstRcvd: String = "59",
    val operatorName: String = "",
    val qth: String = "",
    val province: String = "",
    val gridLocator: String = "",
    val powerWatts: String = "50",
    val notes: String = "",
    val qslSent: Boolean = false,
    val qslRcvd: Boolean = false,
    val statusMessage: String? = null
)

class HamLogViewModel(
    private val repository: HamLogRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(LoggingFormState())
    val formState: StateFlow<LoggingFormState> = _formState.asStateFlow()

    // Logbook filtering
    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    private val _selectedBandFilter = MutableStateFlow<String?>(null)
    val selectedBandFilter: StateFlow<String?> = _selectedBandFilter.asStateFlow()

    private val _selectedModeFilter = MutableStateFlow<String?>(null)
    val selectedModeFilter: StateFlow<String?> = _selectedModeFilter.asStateFlow()

    val allQsos: StateFlow<List<QsoEntry>> = repository.allQsos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredQsos: StateFlow<List<QsoEntry>> = combine(
        allQsos,
        _logSearchQuery,
        _selectedBandFilter,
        _selectedModeFilter
    ) { qsos, query, bandFilter, modeFilter ->
        qsos.filter { qso ->
            val matchesQuery = query.isEmpty() ||
                qso.callsign.contains(query, ignoreCase = true) ||
                qso.operatorName.contains(query, ignoreCase = true) ||
                qso.qth.contains(query, ignoreCase = true) ||
                qso.province.contains(query, ignoreCase = true) ||
                qso.radioId.contains(query, ignoreCase = true) ||
                qso.notes.contains(query, ignoreCase = true)
            val matchesBand = bandFilter == null || qso.band.equals(bandFilter, ignoreCase = true)
            val matchesMode = modeFilter == null || qso.mode.equals(modeFilter, ignoreCase = true)
            matchesQuery && matchesBand && matchesMode
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Directory filtering
    private val _directorySearchQuery = MutableStateFlow("")
    val directorySearchQuery: StateFlow<String> = _directorySearchQuery.asStateFlow()

    private val _selectedAreaFilter = MutableStateFlow<Int?>(null)
    val selectedAreaFilter: StateFlow<Int?> = _selectedAreaFilter.asStateFlow()

    val allCallsigns: StateFlow<List<CallsignEntry>> = repository.allCallsigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCallsigns: StateFlow<List<CallsignEntry>> = combine(
        allCallsigns,
        _directorySearchQuery,
        _selectedAreaFilter
    ) { entries, query, areaFilter ->
        entries.filter { entry ->
            val matchesQuery = query.isEmpty() ||
                entry.callsign.contains(query, ignoreCase = true) ||
                entry.operatorName.contains(query, ignoreCase = true) ||
                entry.province.contains(query, ignoreCase = true) ||
                entry.qth.contains(query, ignoreCase = true) ||
                entry.radioId.contains(query, ignoreCase = true) ||
                entry.clubAffiliation.contains(query, ignoreCase = true)
            val matchesArea = areaFilter == null || entry.callArea == areaFilter
            matchesQuery && matchesArea
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureDatabaseSeeded()
        }
    }

    fun onCallsignChanged(callsign: String) {
        val upper = callsign.uppercase().filter { it.isLetterOrDigit() || it == '/' }
        val analysis = if (upper.isNotEmpty()) ThaiHamParser.parse(upper) else null

        _formState.value = _formState.value.copy(
            callsign = upper,
            analysis = analysis,
            radioIdError = null
        )

        if (upper.isNotEmpty()) {
            viewModelScope.launch {
                val match = repository.lookupCallsign(upper)
                val prevQsos = repository.getPreviousQsosForCallsign(upper)

                if (match != null) {
                    _formState.value = _formState.value.copy(
                        offlineMatch = match,
                        previousQsoCount = prevQsos.size,
                        operatorName = match.operatorName,
                        qth = match.qth,
                        province = match.province,
                        gridLocator = match.gridLocator,
                        radioId = if (_formState.value.radioId.isEmpty() && match.radioId.isNotEmpty()) match.radioId else _formState.value.radioId
                    )
                } else {
                    _formState.value = _formState.value.copy(
                        offlineMatch = null,
                        previousQsoCount = prevQsos.size,
                        gridLocator = if (_formState.value.gridLocator.isEmpty() && analysis != null) analysis.defaultGrid else _formState.value.gridLocator,
                        province = if (_formState.value.province.isEmpty() && analysis != null && analysis.isThai) analysis.areaTitle else _formState.value.province
                    )
                }
            }
        } else {
            _formState.value = _formState.value.copy(
                offlineMatch = null,
                previousQsoCount = 0
            )
        }
    }

    fun onRadioIdChanged(radioId: String) {
        val clean = radioId.filter { it.isDigit() }
        _formState.value = _formState.value.copy(
            radioId = clean,
            radioIdError = null
        )

        if (clean.length >= 6) {
            viewModelScope.launch {
                val match = repository.lookupByRadioId(clean)
                if (match != null) {
                    onCallsignChanged(match.callsign)
                    _formState.value = _formState.value.copy(
                        radioId = clean,
                        operatorName = match.operatorName,
                        qth = match.qth,
                        province = match.province,
                        gridLocator = match.gridLocator,
                        offlineMatch = match
                    )
                }
            }
        }
    }

    /**
     * Connects to RadioID.net to lookup user by DMR Radio ID or Callsign
     */
    fun lookupRadioIdNet(targetCode: String? = null) {
        val current = _formState.value
        val query = (targetCode ?: current.radioId.ifEmpty { current.callsign }).trim()

        if (query.isEmpty()) {
            _formState.value = current.copy(radioIdError = "Enter a DMR Radio ID or Callsign to search on RadioID.net")
            return
        }

        _formState.value = current.copy(isRadioIdLoading = true, radioIdError = null)

        viewModelScope.launch {
            val result = repository.queryRadioIdNet(query)
            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        val analysis = ThaiHamParser.parse(user.callsign)
                        val prevQsos = repository.getPreviousQsosForCallsign(user.callsign)
                        _formState.value = _formState.value.copy(
                            isRadioIdLoading = false,
                            radioIdResultUser = user,
                            callsign = user.callsign,
                            radioId = user.radioId,
                            operatorName = user.fullName,
                            qth = user.city,
                            province = user.state,
                            analysis = analysis,
                            previousQsoCount = prevQsos.size,
                            gridLocator = if (_formState.value.gridLocator.isEmpty()) analysis.defaultGrid else _formState.value.gridLocator,
                            statusMessage = "✓ RadioID.net: ${user.callsign} (${user.fullName}) - ID ${user.radioId}"
                        )
                    } else {
                        _formState.value = _formState.value.copy(
                            isRadioIdLoading = false,
                            radioIdResultUser = null,
                            radioIdError = "No record found on RadioID.net for '$query'"
                        )
                    }
                },
                onFailure = { error ->
                    _formState.value = _formState.value.copy(
                        isRadioIdLoading = false,
                        radioIdError = "RadioID.net connection error: ${error.localizedMessage ?: "Network failed"}"
                    )
                }
            )
        }
    }

    fun onBandSelected(band: String) {
        val defaultFreq = when (band) {
            "2m" -> "144.900"
            "70cm" -> "435.000"
            "6m" -> "50.150"
            "10m" -> "28.500"
            "15m" -> "21.250"
            "20m" -> "14.205"
            "40m" -> "7.100"
            "80m" -> "3.600"
            else -> _formState.value.frequency
        }
        _formState.value = _formState.value.copy(
            band = band,
            frequency = defaultFreq
        )
    }

    fun onFrequencyChanged(freq: String) {
        _formState.value = _formState.value.copy(frequency = freq)
    }

    fun onModeSelected(mode: String) {
        val defaultRst = when (mode) {
            "CW", "FT8", "FT4", "RTTY" -> "599"
            else -> "59"
        }
        _formState.value = _formState.value.copy(
            mode = mode,
            rstSent = defaultRst,
            rstRcvd = defaultRst
        )
    }

    fun onRstSentChanged(rst: String) {
        _formState.value = _formState.value.copy(rstSent = rst)
    }

    fun onRstRcvdChanged(rst: String) {
        _formState.value = _formState.value.copy(rstRcvd = rst)
    }

    fun onOperatorNameChanged(name: String) {
        _formState.value = _formState.value.copy(operatorName = name)
    }

    fun onQthChanged(qth: String) {
        _formState.value = _formState.value.copy(qth = qth)
    }

    fun onProvinceChanged(province: String) {
        _formState.value = _formState.value.copy(province = province)
    }

    fun onGridLocatorChanged(grid: String) {
        _formState.value = _formState.value.copy(gridLocator = grid.uppercase())
    }

    fun onPowerChanged(power: String) {
        _formState.value = _formState.value.copy(powerWatts = power)
    }

    fun onNotesChanged(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun onQslSentToggled(sent: Boolean) {
        _formState.value = _formState.value.copy(qslSent = sent)
    }

    fun onQslRcvdToggled(rcvd: Boolean) {
        _formState.value = _formState.value.copy(qslRcvd = rcvd)
    }

    fun logCurrentQso() {
        val current = _formState.value
        if (current.callsign.isBlank()) {
            _formState.value = current.copy(statusMessage = "Please enter a callsign or Radio ID")
            return
        }

        val freqNum = current.frequency.toDoubleOrNull() ?: 144.900
        val pwrNum = current.powerWatts.toIntOrNull() ?: 50

        val qso = QsoEntry(
            callsign = current.callsign.trim().uppercase(),
            timestamp = System.currentTimeMillis(),
            band = current.band,
            frequency = freqNum,
            mode = current.mode,
            rstSent = current.rstSent,
            rstRcvd = current.rstRcvd,
            operatorName = current.operatorName.trim(),
            qth = current.qth.trim(),
            province = current.province.trim(),
            gridLocator = current.gridLocator.trim().uppercase(),
            radioId = current.radioId.trim(),
            powerWatts = pwrNum,
            qslSent = current.qslSent,
            qslRcvd = current.qslRcvd,
            notes = current.notes.trim()
        )

        viewModelScope.launch {
            repository.insertQso(qso)

            // If the operator entered a new Thai callsign with name or QTH, also offer / auto-add to offline directory
            if (current.analysis?.isThai == true && current.operatorName.isNotBlank() && current.offlineMatch == null) {
                val newEntry = CallsignEntry(
                    callsign = qso.callsign,
                    operatorName = qso.operatorName,
                    qth = qso.qth,
                    province = qso.province,
                    callArea = current.analysis.callArea ?: 1,
                    gridLocator = qso.gridLocator,
                    radioId = qso.radioId,
                    licenseClass = current.analysis.licenseClassHint,
                    notes = if (qso.radioId.isNotEmpty()) "RadioID: ${qso.radioId}" else "Added from QSO log"
                )
                repository.insertCallsign(newEntry)
            }

            val bkkTimeStr = SimpleDateFormat("HH:mm:ss 'ICT (UTC+7)'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Bangkok")
            }.format(Date(qso.timestamp))

            _formState.value = LoggingFormState(
                band = current.band,
                frequency = current.frequency,
                mode = current.mode,
                powerWatts = current.powerWatts,
                statusMessage = "✓ QSO with ${qso.callsign} logged at $bkkTimeStr! 73"
            )
        }
    }

    fun clearStatusMessage() {
        _formState.value = _formState.value.copy(statusMessage = null)
    }

    fun deleteQso(qso: QsoEntry) {
        viewModelScope.launch {
            repository.deleteQso(qso)
        }
    }

    fun toggleQslSent(qso: QsoEntry) {
        viewModelScope.launch {
            repository.updateQso(qso.copy(qslSent = !qso.qslSent))
        }
    }

    fun toggleQslRcvd(qso: QsoEntry) {
        viewModelScope.launch {
            repository.updateQso(qso.copy(qslRcvd = !qso.qslRcvd))
        }
    }

    // Directory management
    fun addCallsignToDirectory(entry: CallsignEntry) {
        viewModelScope.launch {
            repository.insertCallsign(entry)
        }
    }

    fun deleteCallsignFromDirectory(entry: CallsignEntry) {
        viewModelScope.launch {
            repository.deleteCallsign(entry)
        }
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun setBandFilter(band: String?) {
        _selectedBandFilter.value = band
    }

    fun setModeFilter(mode: String?) {
        _selectedModeFilter.value = mode
    }

    fun setDirectorySearchQuery(query: String) {
        _directorySearchQuery.value = query
    }

    fun setAreaFilter(area: Int?) {
        _selectedAreaFilter.value = area
    }

    fun exportAdif(): String {
        return AdifConverter.exportToAdif(allQsos.value)
    }

    fun importAdifLogs(adifContent: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val qsos = AdifConverter.parseAdif(adifContent)
            qsos.forEach { repository.insertQso(it) }
            onComplete(qsos.size)
        }
    }
}

class HamLogViewModelFactory(
    private val repository: HamLogRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HamLogViewModel::class.java)) {
            return HamLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
