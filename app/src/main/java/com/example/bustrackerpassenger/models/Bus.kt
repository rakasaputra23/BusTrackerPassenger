package com.example.bustrackerpassenger.models

data class Bus(
    val busId: String            = "",
    val namaBus: String?         = null,
    val plateNumber: String?     = null,
    val busClass: String?        = null,
    val route: String?           = null,
    val capacity: Int            = 0,
    val currentPassengers: Int   = 0,
    val driver: String?          = null,
    val status: String?          = null,
    val kondisi: String?         = null,
    // FIX Bug #1: kondisiUpdate di database adalah Long (timestamp), bukan String
    val kondisiUpdate: Long?     = null,
    val totalDistance: Double?   = null,
    val location: BusLocation?   = null,
    val track: List<TrackPoint>  = emptyList(),
    val encodedRoute: String?    = null,
    val eta: Eta?                = null,
) {
    /** Nama tampil: prioritas namaBus, fallback ke plateNumber */
    val displayName: String
        get() = namaBus?.takeIf { it.isNotBlank() }
            ?: plateNumber
            ?: "Unknown"

    /** Sisa kursi tersedia */
    val availableSeats: Int
        get() = (capacity - currentPassengers).coerceAtLeast(0)

    /** Teks okupansi "penumpang/kapasitas" */
    val occupancyText: String
        get() = "$currentPassengers/$capacity"

    /** Persentase keterisian kursi */
    val occupancyPercent: Int
        get() = if (capacity > 0) ((currentPassengers * 100f) / capacity).toInt() else 0

    /** Apakah bus sedang bergerak (online) */
    val isOnline: Boolean
        get() = (location?.speed ?: 0f) > 0f || status.equals("active", ignoreCase = true)

    // ─── Inner: Track Point ────────────────────────────────────────────────────
    data class TrackPoint(
        val lat: Double       = 0.0,
        val lng: Double       = 0.0,
        // FIX Bug #3: tambahkan field timestamp agar tidak terbuang
        val timestamp: Long?  = null,
    )

    // ─── Inner: ETA ───────────────────────────────────────────────────────────
    data class Eta(
        val remainingDistance: Double? = null,
        val remainingTime: Int?        = null,
        val estimatedArrival: String?  = null,
    )
}