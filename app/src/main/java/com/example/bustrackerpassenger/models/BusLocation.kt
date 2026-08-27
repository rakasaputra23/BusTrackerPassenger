package com.example.bustrackerpassenger.models

data class BusLocation(
    val latitude: Double    = 0.0,
    val longitude: Double   = 0.0,
    val speed: Float        = 0f,
    // FIX Bug #2: lastUpdate di database adalah Long (timestamp), bukan String
    val lastUpdate: Long?   = null,
)