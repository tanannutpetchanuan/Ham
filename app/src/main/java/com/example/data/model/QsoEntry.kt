package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qso_logs")
data class QsoEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callsign: String,
    val timestamp: Long = System.currentTimeMillis(),
    val band: String = "2m",
    val frequency: Double = 144.900,
    val mode: String = "FM",
    val rstSent: String = "59",
    val rstRcvd: String = "59",
    val operatorName: String = "",
    val qth: String = "",
    val province: String = "",
    val gridLocator: String = "",
    val radioId: String = "",
    val powerWatts: Int = 50,
    val qslSent: Boolean = false,
    val qslRcvd: Boolean = false,
    val notes: String = ""
)
