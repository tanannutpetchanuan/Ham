package com.example.data.repository

import com.example.data.local.CallsignDao
import com.example.data.local.DefaultCallsignData
import com.example.data.local.QsoDao
import com.example.data.model.CallsignEntry
import com.example.data.model.QsoEntry
import com.example.data.model.RadioIdUser
import com.example.data.remote.RadioIdApiClient
import com.example.util.ThaiHamParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class HamLogRepository(
    private val qsoDao: QsoDao,
    private val callsignDao: CallsignDao,
    private val radioIdApiClient: RadioIdApiClient = RadioIdApiClient()
) {
    val allQsos: Flow<List<QsoEntry>> = qsoDao.getAllQsos()
    val allCallsigns: Flow<List<CallsignEntry>> = callsignDao.getAllCallsigns()
    val totalQsoCount: Flow<Int> = qsoDao.getTotalQsoCount()

    suspend fun searchQsos(query: String): Flow<List<QsoEntry>> {
        return qsoDao.searchQsos(query)
    }

    suspend fun lookupCallsign(callsign: String): CallsignEntry? = withContext(Dispatchers.IO) {
        callsignDao.getCallsign(callsign.trim().uppercase())
    }

    suspend fun lookupByRadioId(radioId: String): CallsignEntry? = withContext(Dispatchers.IO) {
        callsignDao.getByRadioId(radioId.trim())
    }

    /**
     * Query RadioID.net live API by DMR Radio ID or Callsign.
     */
    suspend fun queryRadioIdNet(query: String): Result<RadioIdUser?> = withContext(Dispatchers.IO) {
        val result = radioIdApiClient.lookup(query)
        // If found, optionally cache/persist into local offline directory
        result.getOrNull()?.let { user ->
            val analysis = ThaiHamParser.parse(user.callsign)
            val existing = callsignDao.getCallsign(user.callsign)
            val entry = CallsignEntry(
                callsign = user.callsign,
                operatorName = user.fullName.ifEmpty { existing?.operatorName ?: "" },
                qth = user.city.ifEmpty { existing?.qth ?: "" },
                province = user.state.ifEmpty { existing?.province ?: "" },
                callArea = analysis.callArea ?: 1,
                gridLocator = existing?.gridLocator ?: analysis.defaultGrid,
                radioId = user.radioId,
                licenseClass = existing?.licenseClass ?: analysis.licenseClassHint,
                clubAffiliation = existing?.clubAffiliation ?: "",
                notes = if (existing != null && existing.notes.isNotEmpty()) existing.notes else "RadioID.net verified (DMR ID: ${user.radioId})"
            )
            callsignDao.insertCallsign(entry)
        }
        result
    }

    suspend fun searchCallsigns(query: String): Flow<List<CallsignEntry>> {
        return callsignDao.searchCallsigns(query)
    }

    fun getCallsignsByArea(area: Int): Flow<List<CallsignEntry>> {
        return callsignDao.getCallsignsByArea(area)
    }

    suspend fun insertQso(qso: QsoEntry): Long = withContext(Dispatchers.IO) {
        qsoDao.insertQso(qso)
    }

    suspend fun updateQso(qso: QsoEntry) = withContext(Dispatchers.IO) {
        qsoDao.updateQso(qso)
    }

    suspend fun deleteQso(qso: QsoEntry) = withContext(Dispatchers.IO) {
        qsoDao.deleteQso(qso)
    }

    suspend fun deleteQsoById(id: Long) = withContext(Dispatchers.IO) {
        qsoDao.deleteQsoById(id)
    }

    suspend fun getPreviousQsosForCallsign(callsign: String): List<QsoEntry> = withContext(Dispatchers.IO) {
        qsoDao.getQsosForCallsign(callsign.trim().uppercase())
    }

    suspend fun insertCallsign(entry: CallsignEntry) = withContext(Dispatchers.IO) {
        callsignDao.insertCallsign(entry)
    }

    suspend fun deleteCallsign(entry: CallsignEntry) = withContext(Dispatchers.IO) {
        callsignDao.deleteCallsign(entry)
    }

    suspend fun ensureDatabaseSeeded() = withContext(Dispatchers.IO) {
        val count = callsignDao.getCount()
        if (count == 0) {
            callsignDao.insertInitialDirectory(DefaultCallsignData.initialEntries)
        }
    }
}
