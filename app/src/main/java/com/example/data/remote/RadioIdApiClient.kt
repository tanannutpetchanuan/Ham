package com.example.data.remote

import android.util.Log
import com.example.data.model.RadioIdUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RadioIdApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "RadioIdApiClient"
        private const val BASE_URL = "https://radioid.net/api/dmr/user/"
        private const val USER_AGENT = "ThaiHamLog/1.0 (Amateur Radio Android App; contact: tanannutpetchanuan@gmail.com)"
    }

    suspend fun lookupByRadioId(radioId: String): Result<RadioIdUser?> = withContext(Dispatchers.IO) {
        val cleanId = radioId.trim()
        if (cleanId.isEmpty()) {
            return@withContext Result.success(null)
        }
        val url = "$BASE_URL?id=$cleanId"
        executeLookup(url)
    }

    suspend fun lookupByCallsign(callsign: String): Result<RadioIdUser?> = withContext(Dispatchers.IO) {
        val cleanCall = callsign.trim().uppercase()
        if (cleanCall.isEmpty()) {
            return@withContext Result.success(null)
        }
        val url = "$BASE_URL?callsign=$cleanCall"
        executeLookup(url)
    }

    suspend fun lookup(query: String): Result<RadioIdUser?> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success(null)
        }
        // If it consists entirely of digits, query by DMR Radio ID; otherwise query by callsign
        if (trimmed.all { it.isDigit() }) {
            lookupByRadioId(trimmed)
        } else {
            lookupByCallsign(trimmed)
        }
    }

    private fun executeLookup(url: String): Result<RadioIdUser?> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return Result.failure(Exception("RadioID.net HTTP error: ${response.code}"))
            }

            val body = response.body?.string() ?: return Result.success(null)
            val json = JSONObject(body)
            val count = json.optInt("count", 0)
            val results = json.optJSONArray("results")

            if (results == null || results.length() == 0) {
                return Result.success(null)
            }

            val first = results.getJSONObject(0)
            val user = RadioIdUser(
                radioId = first.optString("id", first.optInt("id", 0).toString()),
                callsign = first.optString("callsign", "").uppercase(),
                firstName = first.optString("fname", ""),
                surname = first.optString("surname", ""),
                city = first.optString("city", ""),
                state = first.optString("state", ""),
                country = first.optString("country", ""),
                remarks = first.optString("remarks", "")
            )
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed RadioID lookup at $url", e)
            Result.failure(e)
        }
    }
}
