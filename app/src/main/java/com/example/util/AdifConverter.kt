package com.example.util

import com.example.data.model.QsoEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AdifConverter {

    private val adifDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val adifTimeFormat = SimpleDateFormat("HHmmss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun exportToAdif(qsos: List<QsoEntry>): String {
        val sb = StringBuilder()
        sb.append("Thai Ham Log ADIF Export\n")
        sb.append("<ADIF_VER:5>3.1.4\n")
        sb.append("<PROGRAMID:11>ThaiHamLog\n")
        sb.append("<EOH>\n\n")

        for (qso in qsos) {
            val date = Date(qso.timestamp)
            val qsoDate = adifDateFormat.format(date)
            val qsoTime = adifTimeFormat.format(date)

            sb.append(formatField("CALL", qso.callsign))
            sb.append(formatField("QSO_DATE", qsoDate))
            sb.append(formatField("TIME_ON", qsoTime))
            sb.append(formatField("BAND", qso.band.uppercase()))
            sb.append(formatField("MODE", qso.mode.uppercase()))
            sb.append(formatField("FREQ", String.format(Locale.US, "%.4f", qso.frequency)))
            sb.append(formatField("RST_SENT", qso.rstSent))
            sb.append(formatField("RST_RCVD", qso.rstRcvd))

            if (qso.operatorName.isNotEmpty()) {
                sb.append(formatField("NAME", qso.operatorName))
            }
            if (qso.qth.isNotEmpty()) {
                sb.append(formatField("QTH", qso.qth))
            }
            if (qso.gridLocator.isNotEmpty()) {
                sb.append(formatField("GRIDSQUARE", qso.gridLocator))
            }
            if (qso.powerWatts > 0) {
                sb.append(formatField("TX_PWR", qso.powerWatts.toString()))
            }
            if (qso.radioId.isNotEmpty()) {
                sb.append(formatField("RADIO_ID", qso.radioId))
            }
            if (qso.notes.isNotEmpty()) {
                sb.append(formatField("COMMENT", qso.notes))
            }
            if (qso.qslSent) {
                sb.append(formatField("QSL_SENT", "Y"))
            }
            if (qso.qslRcvd) {
                sb.append(formatField("QSL_RCVD", "Y"))
            }

            sb.append("<EOR>\n")
        }

        return sb.toString()
    }

    private fun formatField(tag: String, value: String): String {
        val bytes = value.toByteArray(Charsets.UTF_8).size
        return "<$tag:$bytes>$value "
    }

    fun parseAdif(text: String): List<QsoEntry> {
        val result = mutableListOf<QsoEntry>()
        val upper = text
        val headerEnd = upper.indexOf("<EOH>", ignoreCase = true)
        val body = if (headerEnd != -1) upper.substring(headerEnd + 5) else upper

        val records = body.split(Regex("(?i)<EOR>"))
        for (rec in records) {
            val trimmed = rec.trim()
            if (trimmed.isEmpty()) continue

            var call = ""
            var band = "2m"
            var freq = 144.900
            var mode = "FM"
            var rstS = "59"
            var rstR = "59"
            var name = ""
            var qth = ""
            var grid = ""
            var radioId = ""
            var comment = ""
            var pwr = 50

            val fieldRegex = Regex("<([A-Za-z0-9_]+):(\\d+)(?::[A-Za-z])?>([^<]*)")
            val matches = fieldRegex.findAll(trimmed)

            for (m in matches) {
                val tag = m.groupValues[1].uppercase()
                val len = m.groupValues[2].toIntOrNull() ?: 0
                val rawVal = m.groupValues[3]
                val value = if (rawVal.length >= len) rawVal.substring(0, len).trim() else rawVal.trim()

                when (tag) {
                    "CALL" -> call = value
                    "BAND" -> band = value.lowercase()
                    "FREQ" -> freq = value.toDoubleOrNull() ?: 144.900
                    "MODE" -> mode = value.uppercase()
                    "RST_SENT" -> rstS = value
                    "RST_RCVD" -> rstR = value
                    "NAME" -> name = value
                    "QTH" -> qth = value
                    "GRIDSQUARE" -> grid = value
                    "RADIO_ID", "DMR_ID" -> radioId = value
                    "COMMENT" -> comment = value
                    "TX_PWR" -> pwr = value.toIntOrNull() ?: 50
                }
            }

            if (call.isNotEmpty()) {
                result.add(
                    QsoEntry(
                        callsign = call.uppercase(),
                        timestamp = System.currentTimeMillis(),
                        band = band,
                        frequency = freq,
                        mode = mode,
                        rstSent = rstS,
                        rstRcvd = rstR,
                        operatorName = name,
                        qth = qth,
                        gridLocator = grid,
                        radioId = radioId,
                        powerWatts = pwr,
                        notes = comment
                    )
                )
            }
        }
        return result
    }
}
