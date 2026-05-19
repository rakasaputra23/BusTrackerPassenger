package com.example.bustrackerpassenger.ui.viewmodel

import com.example.bustrackerpassenger.models.Bus
import com.google.android.gms.maps.model.LatLng

data class BusUiState(
    // ─── Data ─────────────────────────────────────────────────────────────────
    val allBuses: List<Bus>       = emptyList(),
    val filteredBuses: List<Bus>  = emptyList(),
    val isLoading: Boolean        = true,

    // ─── Selection ────────────────────────────────────────────────────────────
    val selectedBus: Bus?         = null,
    val showBottomSheet: Boolean  = false,

    // ─── Filter & Search ──────────────────────────────────────────────────────
    val searchQuery: String       = "",
    val filterClass: String?      = null,    // null = semua kelas

    // ─── User Location ────────────────────────────────────────────────────────
    val userLocation: LatLng?     = null,

    // ─── Error ────────────────────────────────────────────────────────────────
    val errorMessage: String?     = null,
)