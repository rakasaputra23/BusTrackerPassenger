package com.example.bustrackerpassenger.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Estimasi waktu & jarak dari LOKASI USER ke LOKASI BUS SAAT INI,
 * menggunakan Google Routes API (sama seperti ETACalculator di crew app),
 * supaya hasilnya mengikuti jalur jalan asli + kondisi traffic (traffic-aware),
 * bukan garis lurus (Haversine).
 *
 * Ini SENGAJA dipisah dari ETACalculator versi crew app karena beda konteks:
 * - Crew app: bus -> tujuan akhir rute
 * - Passenger app (di sini): user -> lokasi bus saat ini
 */
object PassengerETACalculator {

    private const val TAG = "PassengerETACalculator"

    // ⚠️ Ganti dengan API key milikmu sendiri (idealnya key terpisah dengan restriction
    // khusus untuk passenger app, jangan reuse key crew app apa adanya di production).
    private const val GOOGLE_API_KEY = "AIzaSyDtP56h8vDFWJ5jSL9c4bFoIRwG6gTp2u8"
    private const val ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes"

    data class EtaResult(
        val distanceKm: Double,
        val durationMinutes: Int
    )

    /**
     * Hitung ETA user -> bus pakai Routes API.
     * travelMode DRIVE dipakai karena yang diestimasi adalah posisi BUS (bergerak di jalan raya),
     * bukan cara user menuju bus.
     */
    suspend fun calculateEta(
        userLat: Double,
        userLng: Double,
        busLat: Double,
        busLng: Double
    ): Result<EtaResult> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("origin", JSONObject().apply {
                    put("location", JSONObject().apply {
                        put("latLng", JSONObject().apply {
                            put("latitude", userLat)
                            put("longitude", userLng)
                        })
                    })
                })
                put("destination", JSONObject().apply {
                    put("location", JSONObject().apply {
                        put("latLng", JSONObject().apply {
                            put("latitude", busLat)
                            put("longitude", busLng)
                        })
                    })
                })
                put("travelMode", "DRIVE")
                put("routingPreference", "TRAFFIC_AWARE")
            }

            val url  = URL(ROUTES_API_URL)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod  = "POST"
                connectTimeout = 10_000
                readTimeout    = 10_000
                doOutput       = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("X-Goog-Api-Key", GOOGLE_API_KEY)
                setRequestProperty("X-Goog-FieldMask", "routes.duration,routes.distanceMeters")
            }

            conn.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                parseResponse(response)
            } else {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: ""
                conn.disconnect()
                Log.e(TAG, "HTTP Error ${conn.responseCode}: $errorBody")
                Result.failure(Exception("HTTP Error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating ETA: ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseResponse(jsonResponse: String): Result<EtaResult> {
        return try {
            val json   = JSONObject(jsonResponse)
            val routes = json.optJSONArray("routes")

            if (routes == null || routes.length() == 0) {
                return Result.failure(Exception("Routes API: tidak ada rute ditemukan"))
            }

            val route = routes.getJSONObject(0)
            val distanceMeters = route.getInt("distanceMeters")
            val distanceKm     = distanceMeters / 1000.0

            val durationStr = route.getString("duration") // contoh: "125s"
            val durationSec = durationStr.removeSuffix("s").toIntOrNull() ?: 0
            val durationMin = durationSec / 60

            Result.success(EtaResult(distanceKm, durationMin))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Routes API response: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fallback manual (Haversine) — dipakai HANYA kalau Routes API gagal
     * (mis. tidak ada koneksi, rate limit, dsb), supaya tetap ada estimasi kasar.
     */
    fun calculateEtaManual(
        userLat: Double, userLng: Double,
        busLat: Double,  busLng: Double,
        averageSpeedKmh: Float = 30f
    ): EtaResult {
        val distance = calculateDistanceHaversine(userLat, userLng, busLat, busLng)
        val speed = if (averageSpeedKmh > 0) averageSpeedKmh else 30f
        val durationMin = ((distance / speed) * 60).toInt()
        return EtaResult(distance, durationMin)
    }

    private fun calculateDistanceHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r    = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a    = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).let { it * it }
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}