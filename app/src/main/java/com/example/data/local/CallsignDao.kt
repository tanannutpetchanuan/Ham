package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallsignEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CallsignDao {
    @Query("SELECT * FROM callsign_directory ORDER BY callsign ASC")
    fun getAllCallsigns(): Flow<List<CallsignEntry>>

    @Query("SELECT * FROM callsign_directory WHERE callsign = :callsign LIMIT 1")
    suspend fun getCallsign(callsign: String): CallsignEntry?

    @Query("SELECT * FROM callsign_directory WHERE radioId = :radioId LIMIT 1")
    suspend fun getByRadioId(radioId: String): CallsignEntry?

    @Query("SELECT * FROM callsign_directory WHERE callsign LIKE '%' || :query || '%' OR operatorName LIKE '%' || :query || '%' OR province LIKE '%' || :query || '%' OR qth LIKE '%' || :query || '%' OR radioId LIKE '%' || :query || '%' ORDER BY callsign ASC")
    fun searchCallsigns(query: String): Flow<List<CallsignEntry>>

    @Query("SELECT * FROM callsign_directory WHERE callArea = :callArea ORDER BY callsign ASC")
    fun getCallsignsByArea(callArea: Int): Flow<List<CallsignEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallsign(callsign: CallsignEntry)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialDirectory(callsigns: List<CallsignEntry>)

    @Update
    suspend fun updateCallsign(callsign: CallsignEntry)

    @Delete
    suspend fun deleteCallsign(callsign: CallsignEntry)

    @Query("SELECT COUNT(*) FROM callsign_directory")
    suspend fun getCount(): Int
}
