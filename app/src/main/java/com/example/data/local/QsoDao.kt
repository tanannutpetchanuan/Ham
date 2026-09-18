package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QsoEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface QsoDao {
    @Query("SELECT * FROM qso_logs ORDER BY timestamp DESC")
    fun getAllQsos(): Flow<List<QsoEntry>>

    @Query("SELECT * FROM qso_logs WHERE id = :id")
    suspend fun getQsoById(id: Long): QsoEntry?

    @Query("SELECT * FROM qso_logs WHERE callsign LIKE '%' || :query || '%' OR operatorName LIKE '%' || :query || '%' OR qth LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchQsos(query: String): Flow<List<QsoEntry>>

    @Query("SELECT * FROM qso_logs WHERE callsign = :callsign ORDER BY timestamp DESC")
    suspend fun getQsosForCallsign(callsign: String): List<QsoEntry>

    @Query("SELECT COUNT(*) FROM qso_logs WHERE callsign = :callsign")
    suspend fun getQsoCountForCallsign(callsign: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQso(qso: QsoEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(qsos: List<QsoEntry>)

    @Update
    suspend fun updateQso(qso: QsoEntry)

    @Delete
    suspend fun deleteQso(qso: QsoEntry)

    @Query("DELETE FROM qso_logs WHERE id = :id")
    suspend fun deleteQsoById(id: Long)

    @Query("SELECT COUNT(*) FROM qso_logs")
    fun getTotalQsoCount(): Flow<Int>
}
