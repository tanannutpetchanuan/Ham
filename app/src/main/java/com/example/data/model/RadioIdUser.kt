package com.example.data.model

data class RadioIdUser(
    val radioId: String,
    val callsign: String,
    val firstName: String,
    val surname: String,
    val city: String,
    val state: String,
    val country: String,
    val remarks: String = ""
) {
    val fullName: String
        get() = listOf(firstName, surname).filter { it.isNotBlank() }.joinToString(" ")
}
