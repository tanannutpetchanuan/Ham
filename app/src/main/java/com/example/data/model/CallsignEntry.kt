package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "callsign_directory")
data class CallsignEntry(
    @PrimaryKey
    val callsign: String,
    val operatorName: String,
    val qth: String,
    val province: String,
    val callArea: Int,
    val gridLocator: String,
    val licenseClass: String = "Basic (ขั้นต้น)",
    val clubAffiliation: String = "",
    val radioId: String = "",
    val notes: String = "",
    val isCustomUserAdded: Boolean = false
)
