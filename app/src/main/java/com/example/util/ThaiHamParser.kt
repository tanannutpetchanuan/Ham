package com.example.util

data class CallsignAnalysis(
    val callsign: String,
    val isThai: Boolean,
    val country: String,
    val callArea: Int? = null,
    val areaTitle: String = "",
    val provinceHint: String = "",
    val ituZone: Int = 49,
    val cqZone: Int = 26,
    val defaultGrid: String = "OK03",
    val isClubStation: Boolean = false,
    val licenseClassHint: String = ""
)

object ThaiHamParser {

    private val THAI_ZONE_DATA = mapOf(
        0 to Triple(
            "Zone 0 - Special & Headquarters",
            "Bangkok / RAST HQ (สถานีกลาง/สมาคมฯ)",
            "OK03"
        ),
        1 to Triple(
            "Zone 1 - Central (ภาคกลาง)",
            "Bangkok, Nonthaburi, Pathum Thani, Samut Prakan, Ayutthaya, Ang Thong, Lopburi, Sing Buri, Chai Nat, Saraburi",
            "OK03"
        ),
        2 to Triple(
            "Zone 2 - Eastern (ภาคตะวันออก)",
            "Chonburi, Rayong, Chanthaburi, Trat, Chachoengsao, Prachinburi, Sa Kaeo, Nakhon Nayok",
            "OK02"
        ),
        3 to Triple(
            "Zone 3 - Lower Northeast (ภาคอีสานตอนล่าง)",
            "Nakhon Ratchasima, Buriram, Surin, Sisaket, Ubon Ratchathani, Yasothon, Amnat Charoen, Chaiyaphum",
            "OK14"
        ),
        4 to Triple(
            "Zone 4 - Upper Northeast (ภาคอีสานตอนบน)",
            "Khon Kaen, Udon Thani, Loei, Nong Khai, Sakon Nakhon, Nakhon Phanom, Mukdahan, Kalasin, Maha Sarakham, Bueng Kan",
            "OK16"
        ),
        5 to Triple(
            "Zone 5 - Upper North (ภาคเหนือตอนบน)",
            "Chiang Mai, Chiang Rai, Mae Hong Son, Phayao, Lampang, Lamphun, Phrae, Nan",
            "NL88"
        ),
        6 to Triple(
            "Zone 6 - Lower North / Upper Central (ภาคเหนือตอนล่าง)",
            "Phitsanulok, Nakhon Sawan, Sukhothai, Phichit, Kamphaeng Phet, Phetchabun, Tak, Uthai Thani",
            "NL96"
        ),
        7 to Triple(
            "Zone 7 - Western / Central West (ภาคตะวันตก)",
            "Nakhon Pathom, Ratchaburi, Kanchanaburi, Suphan Buri, Phetchaburi, Prachuap Khiri Khan, Samut Sakhon, Samut Songkhram",
            "OK03"
        ),
        8 to Triple(
            "Zone 8 - Upper South (ภาคใต้ตอนบน)",
            "Surat Thani, Chumphon, Ranong, Nakhon Si Thammarat, Krabi, Phang Nga, Phuket",
            "OJ07"
        ),
        9 to Triple(
            "Zone 9 - Lower South (ภาคใต้ตอนล่าง)",
            "Songkhla, Satun, Trang, Phatthalung, Pattani, Yala, Narathiwat",
            "OJ17"
        )
    )

    fun parse(rawCall: String): CallsignAnalysis {
        val clean = rawCall.trim().uppercase()
        if (clean.isEmpty()) {
            return CallsignAnalysis(
                callsign = "",
                isThai = false,
                country = "Unknown"
            )
        }

        // Check if starts with HS or E2 (standard Thai Ham prefixes)
        val thaiRegex = Regex("^(HS|E2)([0-9])([A-Z]{1,4})$")
        val thaiMatch = thaiRegex.find(clean)

        if (thaiMatch != null) {
            val (prefix, digitStr, suffix) = thaiMatch.destructured
            val areaDigit = digitStr.toIntOrNull() ?: 1
            val zoneInfo = THAI_ZONE_DATA[areaDigit] ?: THAI_ZONE_DATA[1]!!

            val isClub = clean == "HS0AC" || suffix.startsWith("A") && suffix.length == 2 && prefix == "HS"
            val licenseHint = when {
                prefix == "HS" && areaDigit == 0 -> "Club / Special Event"
                suffix.length == 2 -> "Intermediate / Advanced (ขั้นกลาง/สูง)"
                else -> "Basic / Intermediate (ขั้นต้น/กลาง)"
            }

            return CallsignAnalysis(
                callsign = clean,
                isThai = true,
                country = "Thailand (HS/E2)",
                callArea = areaDigit,
                areaTitle = zoneInfo.first,
                provinceHint = zoneInfo.second,
                ituZone = 49,
                cqZone = 26,
                defaultGrid = zoneInfo.third,
                isClubStation = isClub,
                licenseClassHint = licenseHint
            )
        }

        // Special Thai patterns like E2X, E2E, HS50...
        if (clean.startsWith("HS") || clean.startsWith("E2")) {
            val digit = clean.firstOrNull { it.isDigit() }?.digitToIntOrNull() ?: 1
            val zoneInfo = THAI_ZONE_DATA[digit] ?: THAI_ZONE_DATA[1]!!
            return CallsignAnalysis(
                callsign = clean,
                isThai = true,
                country = "Thailand (HS/E2)",
                callArea = digit,
                areaTitle = zoneInfo.first,
                provinceHint = zoneInfo.second,
                ituZone = 49,
                cqZone = 26,
                defaultGrid = zoneInfo.third,
                isClubStation = clean.length <= 4,
                licenseClassHint = "Thai Station"
            )
        }

        // International DX parsing
        val (dxCountry, itu, cq) = parseInternationalPrefix(clean)
        return CallsignAnalysis(
            callsign = clean,
            isThai = false,
            country = dxCountry,
            callArea = null,
            areaTitle = "International DX ($dxCountry)",
            provinceHint = "",
            ituZone = itu,
            cqZone = cq,
            defaultGrid = ""
        )
    }

    private fun parseInternationalPrefix(call: String): Triple<String, Int, Int> {
        return when {
            call.startsWith("JA") || call.startsWith("JH") || call.startsWith("JR") ||
            call.startsWith("JE") || call.startsWith("JF") || call.startsWith("JG") ||
            call.startsWith("JI") || call.startsWith("JJ") || call.startsWith("7K") ||
            call.startsWith("7L") || call.startsWith("7M") || call.startsWith("7N") -> Triple("Japan", 45, 25)

            call.startsWith("9M") || call.startsWith("9W") -> Triple("Malaysia", 54, 28)
            call.startsWith("9V") -> Triple("Singapore", 54, 28)
            call.startsWith("YB") || call.startsWith("YC") || call.startsWith("YD") -> Triple("Indonesia", 54, 28)
            call.startsWith("VR2") -> Triple("Hong Kong", 44, 24)
            call.startsWith("BV") || call.startsWith("BX") || call.startsWith("BM") -> Triple("Taiwan", 44, 24)
            call.startsWith("BY") || call.startsWith("BA") || call.startsWith("BD") ||
            call.startsWith("BG") || call.startsWith("BH") -> Triple("China", 44, 24)
            call.startsWith("HL") || call.startsWith("DS") -> Triple("South Korea", 44, 25)
            call.startsWith("DU") || call.startsWith("DV") || call.startsWith("DW") -> Triple("Philippines", 50, 27)
            call.startsWith("XV") || call.startsWith("3W") -> Triple("Vietnam", 49, 26)
            call.startsWith("XW") -> Triple("Laos", 49, 26)
            call.startsWith("XU") -> Triple("Cambodia", 49, 26)
            call.startsWith("XZ") -> Triple("Myanmar", 49, 26)
            call.startsWith("VK") -> Triple("Australia", 59, 30)
            call.startsWith("ZL") -> Triple("New Zealand", 60, 32)
            call.startsWith("VU") -> Triple("India", 41, 22)
            call.startsWith("K") || call.startsWith("W") || call.startsWith("N") ||
            call.startsWith("AA") || call.startsWith("AB") || call.startsWith("AC") ||
            call.startsWith("AD") || call.startsWith("AE") || call.startsWith("AF") -> Triple("United States", 8, 4)
            call.startsWith("VE") || call.startsWith("VA") -> Triple("Canada", 4, 4)
            call.startsWith("G") || call.startsWith("M") || call.startsWith("2E") -> Triple("United Kingdom", 27, 14)
            call.startsWith("DL") || call.startsWith("DJ") || call.startsWith("DK") -> Triple("Germany", 28, 14)
            call.startsWith("F") -> Triple("France", 27, 14)
            call.startsWith("I") -> Triple("Italy", 28, 15)
            call.startsWith("EA") -> Triple("Spain", 37, 14)
            call.startsWith("UA") || call.startsWith("RA") || call.startsWith("RU") -> Triple("Russia", 29, 16)
            else -> Triple("International", 0, 0)
        }
    }

    fun getAllZones(): List<Pair<Int, Triple<String, String, String>>> {
        return THAI_ZONE_DATA.toList()
    }
}
