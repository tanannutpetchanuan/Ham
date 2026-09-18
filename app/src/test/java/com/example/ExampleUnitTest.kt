package com.example

import com.example.data.model.QsoEntry
import com.example.util.AdifConverter
import com.example.util.ThaiHamParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testThaiCallsignParsing() {
        val analysisHs1 = ThaiHamParser.parse("HS1AB")
        assertTrue(analysisHs1.isThai)
        assertEquals(1, analysisHs1.callArea)
        assertEquals(49, analysisHs1.ituZone)
        assertEquals(26, analysisHs1.cqZone)
        assertEquals("OK03", analysisHs1.defaultGrid)

        val analysisE21 = ThaiHamParser.parse("E21EIC")
        assertTrue(analysisE21.isThai)
        assertEquals(1, analysisE21.callArea)

        val analysisHs5 = ThaiHamParser.parse("HS5AC")
        assertTrue(analysisHs5.isThai)
        assertEquals(5, analysisHs5.callArea)
        assertEquals("NL88", analysisHs5.defaultGrid)

        val analysisHs8 = ThaiHamParser.parse("HS8AX")
        assertTrue(analysisHs8.isThai)
        assertEquals(8, analysisHs8.callArea)
        assertEquals("OJ07", analysisHs8.defaultGrid)

        val analysisDx = ThaiHamParser.parse("JA1ABC")
        assertFalse(analysisDx.isThai)
        assertEquals("Japan", analysisDx.country)
    }

    @Test
    fun testAdifExportAndImport() {
        val qso = QsoEntry(
            callsign = "HS1AB",
            timestamp = 1700000000000L,
            band = "2m",
            frequency = 144.900,
            mode = "FM",
            rstSent = "59",
            rstRcvd = "59",
            operatorName = "Bangkok Radio Club",
            qth = "Bangkok",
            gridLocator = "OK03",
            radioId = "5201001",
            powerWatts = 50,
            notes = "Test QSO"
        )

        val adifString = AdifConverter.exportToAdif(listOf(qso))
        assertTrue(adifString.contains("<CALL:5>HS1AB"))
        assertTrue(adifString.contains("<MODE:2>FM"))
        assertTrue(adifString.contains("<RADIO_ID:7>5201001"))
        assertTrue(adifString.contains("<EOR>"))

        val parsed = AdifConverter.parseAdif(adifString)
        assertEquals(1, parsed.size)
        assertEquals("HS1AB", parsed[0].callsign)
        assertEquals("FM", parsed[0].mode)
        assertEquals("5201001", parsed[0].radioId)
        assertEquals("Bangkok Radio Club", parsed[0].operatorName)
    }

    @Test
    fun testBangkokUtc7TimeFormatting() {
        val testEpochMillis = 1700000000000L // 2023-11-14 22:13:20 UTC -> 2023-11-15 05:13:20 ICT
        val bkkFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Bangkok")
        }
        val formatted = bkkFormat.format(java.util.Date(testEpochMillis))
        assertEquals("2023-11-15 05:13:20", formatted)
    }
}
