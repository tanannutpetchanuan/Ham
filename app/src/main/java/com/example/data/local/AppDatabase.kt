package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallsignEntry
import com.example.data.model.QsoEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [QsoEntry::class, CallsignEntry::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qsoDao(): QsoDao
    abstract fun callsignDao(): CallsignDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thai_ham_log_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.callsignDao(), database.qsoDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(callsignDao: CallsignDao, qsoDao: QsoDao) {
            // Seed offline Thai ham directory
            callsignDao.insertInitialDirectory(DefaultCallsignData.initialEntries)

            // Seed initial sample QSOs for immediate out-of-the-box utility
            val now = System.currentTimeMillis()
            val sampleQsos = listOf(
                QsoEntry(
                    callsign = "HS0AC",
                    timestamp = now - 3600000L * 2,
                    band = "2m",
                    frequency = 144.900,
                    mode = "FM",
                    rstSent = "59",
                    rstRcvd = "59",
                    operatorName = "RAST Club Station",
                    qth = "Bangkok",
                    province = "Bangkok",
                    gridLocator = "OK03",
                    powerWatts = 50,
                    qslSent = true,
                    qslRcvd = true,
                    notes = "RAST Sunday morning net check-in. Excellent signal report."
                ),
                QsoEntry(
                    callsign = "E21EIC",
                    timestamp = now - 3600000L * 5,
                    band = "20m",
                    frequency = 14.205,
                    mode = "SSB",
                    rstSent = "59",
                    rstRcvd = "58",
                    operatorName = "Champ",
                    qth = "Bangkok",
                    province = "Bangkok",
                    gridLocator = "OK03gu",
                    powerWatts = 100,
                    qslSent = true,
                    qslRcvd = false,
                    notes = "QSO with Champ during regional contest. 5/9 loud & clear."
                ),
                QsoEntry(
                    callsign = "HS5AC",
                    timestamp = now - 3600000L * 24,
                    band = "2m",
                    frequency = 145.675,
                    mode = "FM",
                    rstSent = "57",
                    rstRcvd = "59",
                    operatorName = "Chiang Mai Amateur Radio Assoc.",
                    qth = "Doi Suthep",
                    province = "Chiang Mai",
                    gridLocator = "NL88",
                    powerWatts = 10,
                    qslSent = false,
                    qslRcvd = false,
                    notes = "Repeater contact from mountain summit during portable operation."
                )
            )
            qsoDao.insertAll(sampleQsos)
        }
    }
}
