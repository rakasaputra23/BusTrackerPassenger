package com.example.bustrackerpassenger.utils

import android.location.Location

object DistanceCalculator {

    /**
     * Menghitung jarak antara dua koordinat dalam kilometer.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val loc1 = Location("").apply { latitude = lat1; longitude = lon1 }
        val loc2 = Location("").apply { latitude = lat2; longitude = lon2 }
        return loc1.distanceTo(loc2) / 1000f
    }

    /**
     * Estimasi waktu kedatangan berdasarkan jarak (km) dan kecepatan (km/jam).
     * Mengembalikan teks ETA dalam Bahasa Indonesia.
     */
    fun calculateEtaText(distanceKm: Float, speedKmh: Float): String {
        if (speedKmh <= 0f) return "Bus berhenti"
        val minutes = ((distanceKm / speedKmh) * 60).toInt()
        return when {
            minutes < 1  -> "Kurang dari 1 menit"
            minutes < 60 -> "$minutes menit"
            else -> {
                val h = minutes / 60
                val m = minutes % 60
                if (m == 0) "$h jam" else "$h jam $m menit"
            }
        }
    }
}