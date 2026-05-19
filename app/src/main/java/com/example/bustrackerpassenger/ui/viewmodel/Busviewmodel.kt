package com.example.bustrackerpassenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bustrackerpassenger.models.Bus
import com.example.bustrackerpassenger.models.BusLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BusViewModel : ViewModel() {

    companion object {
        private const val FIREBASE_URL =
            "https://buskrutracker-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    private val database = FirebaseDatabase.getInstance(FIREBASE_URL).getReference("buses")
    private var busListener: ValueEventListener? = null

    private val _uiState = MutableStateFlow(BusUiState())
    val uiState: StateFlow<BusUiState> = _uiState.asStateFlow()

    init {
        loadBusData()
    }

    // ─── Firebase Listener ────────────────────────────────────────────────────

    private fun loadBusData() {
        busListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val buses = mutableListOf<Bus>()

                for (busSnapshot in snapshot.children) {
                    try {
                        val busId            = busSnapshot.key ?: continue
                        val namaBus          = busSnapshot.child("namaBus").getValue(String::class.java)
                        val plateNumber      = busSnapshot.child("plateNumber").getValue(String::class.java)
                        val busClass         = busSnapshot.child("class").getValue(String::class.java)
                        val route            = busSnapshot.child("route").getValue(String::class.java)
                        val capacity         = busSnapshot.child("capacity").getValue(Int::class.java) ?: 0
                        val currentPassengers= busSnapshot.child("currentPassengers").getValue(Int::class.java) ?: 0
                        val driver           = busSnapshot.child("driver").getValue(String::class.java)
                        val status           = busSnapshot.child("status").getValue(String::class.java)
                        val kondisi          = busSnapshot.child("kondisi").getValue(String::class.java)
                        val kondisiUpdate    = busSnapshot.child("kondisiUpdate").getValue(String::class.java)
                        val totalDistance    = busSnapshot.child("totalDistance").getValue(Double::class.java)
                        val encodedRoute     = busSnapshot.child("routePolyline").getValue(String::class.java)

                        val lat    = busSnapshot.child("location/latitude").getValue(Double::class.java)
                        val lng    = busSnapshot.child("location/longitude").getValue(Double::class.java)
                        val speed  = busSnapshot.child("location/speed").getValue(Double::class.java)
                        val lastUp = busSnapshot.child("location/lastUpdate").getValue(String::class.java)

                        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) continue

                        val location = BusLocation(lat, lng, speed?.toFloat() ?: 0f, lastUp)

                        // ETA
                        val etaSnap = busSnapshot.child("eta")
                        val eta = if (etaSnap.exists()) Bus.Eta(
                            remainingDistance = etaSnap.child("remainingDistance").getValue(Double::class.java),
                            remainingTime     = etaSnap.child("remainingTime").getValue(Int::class.java),
                            estimatedArrival  = etaSnap.child("estimatedArrival").getValue(String::class.java),
                        ) else null

                        // Track — unlimited points, sama dengan asli
                        val track = busSnapshot.child("track").children.mapNotNull { tp ->
                            val tLat = tp.child("lat").getValue(Double::class.java)
                            val tLng = tp.child("lng").getValue(Double::class.java)
                            if (tLat != null && tLng != null) Bus.TrackPoint(tLat, tLng) else null
                        }

                        buses += Bus(
                            busId            = busId,
                            namaBus          = namaBus,
                            plateNumber      = plateNumber,
                            busClass         = busClass,
                            route            = route,
                            capacity         = capacity,
                            currentPassengers= currentPassengers,
                            driver           = driver,
                            status           = status,
                            kondisi          = kondisi,
                            kondisiUpdate    = kondisiUpdate,
                            totalDistance    = totalDistance,
                            location         = location,
                            track            = track,
                            encodedRoute     = encodedRoute,
                            eta              = eta,
                        )
                    } catch (e: Exception) {
                        // Skip bus dengan data corrupt
                    }
                }

                _uiState.update { state ->
                    val updated = state.copy(
                        allBuses  = buses,
                        isLoading = false,
                        // Refresh selected bus data secara realtime
                        selectedBus = if (state.selectedBus != null)
                            buses.find { it.busId == state.selectedBus.busId } ?: state.selectedBus
                        else null
                    )
                    updated.copy(filteredBuses = applyFilters(updated))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.update { it.copy(errorMessage = error.message, isLoading = false) }
            }
        }
        database.addValueEventListener(busListener!!)
    }

    // ─── Filter & Search ──────────────────────────────────────────────────────

    private fun applyFilters(state: BusUiState): List<Bus> {
        return state.allBuses.filter { bus ->
            val matchSearch = state.searchQuery.isEmpty() ||
                    bus.namaBus?.contains(state.searchQuery, ignoreCase = true) == true ||
                    bus.plateNumber?.contains(state.searchQuery, ignoreCase = true) == true ||
                    bus.route?.contains(state.searchQuery, ignoreCase = true) == true ||
                    bus.busClass?.contains(state.searchQuery, ignoreCase = true) == true

            val matchClass = state.filterClass == null ||
                    bus.busClass?.equals(state.filterClass, ignoreCase = true) == true

            matchSearch && matchClass
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val updated = state.copy(searchQuery = query)
            updated.copy(filteredBuses = applyFilters(updated))
        }
    }

    fun onFilterClassChange(busClass: String?) {
        _uiState.update { state ->
            val updated = state.copy(filterClass = busClass)
            updated.copy(filteredBuses = applyFilters(updated))
        }
    }

    fun onFilterAvailable() {
        _uiState.update { state ->
            state.copy(filteredBuses = state.allBuses.filter { it.availableSeats > 0 })
        }
    }

    // ─── Selection ────────────────────────────────────────────────────────────

    fun selectBus(bus: Bus) {
        _uiState.update { it.copy(selectedBus = bus, showBottomSheet = true) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedBus = null, showBottomSheet = false) }
    }

    fun showBottomSheet(show: Boolean) {
        _uiState.update { it.copy(showBottomSheet = show) }
    }

    // ─── User Location ────────────────────────────────────────────────────────

    fun updateUserLocation(latLng: LatLng) {
        _uiState.update { it.copy(userLocation = latLng) }
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        busListener?.let { database.removeEventListener(it) }
    }
}