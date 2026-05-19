package com.example.bustrackerpassenger.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bustrackerpassenger.ui.components.*
import com.example.bustrackerpassenger.ui.theme.*
import com.example.bustrackerpassenger.ui.viewmodel.BusViewModel
import com.example.bustrackerpassenger.utils.MAP_STYLE_JSON
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * MapScreen — layar utama aplikasi Bus Tracker Passenger.
 *
 * Struktur:
 * ┌─────────────────────────────────────────┐
 * │  [SearchBar]                            │  ← overlay atas
 * │  [BusCountChip]                         │
 * │                                         │
 * │           Google Map                    │
 * │    (markers, polylines, location)       │
 * │                                         │
 * │  [MapLegend]              [MyLocation]  │  ← overlay bawah
 * └─────────────────────────────────────────┘
 *
 * Modal:
 *  - FilterDialog    → saat icon filter di-tap
 *  - BusBottomSheet  → saat marker bus di-tap
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(viewModel: BusViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var showFilterDialog by remember { mutableStateOf(false) }

    // ── Camera ─────────────────────────────────────────────────────────────────
    val cameraPositionState = rememberCameraPositionState {
        // Default: Madiun, Jawa Timur
        position = CameraPosition.fromLatLngZoom(LatLng(-7.6298, 111.5239), 12f)
    }

    // ── Map style ──────────────────────────────────────────────────────────────
    val mapStyleOptions = remember { MapStyleOptions(MAP_STYLE_JSON) }

    // ── Location permission ────────────────────────────────────────────────────
    val locationPermission = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Request permission saat pertama kali masuk layar
    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    // Ambil last known location setelah permission granted
    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        viewModel.updateUserLocation(latLng)
                        // Pindahkan kamera ke lokasi user saat pertama kali
                        scope.launch {
                            cameraPositionState.animate(
                                update      = CameraUpdateFactory.newLatLngZoom(latLng, 14f),
                                durationMs  = 800,
                            )
                        }
                    }
                }
            } catch (_: SecurityException) { /* belum granted */ }
        }
    }

    // Ikuti bus yang dipilih dengan animasi kamera
    LaunchedEffect(uiState.selectedBus?.busId) {
        uiState.selectedBus?.location?.let { loc ->
            scope.launch {
                cameraPositionState.animate(
                    update     = CameraUpdateFactory.newLatLngZoom(
                        LatLng(loc.latitude, loc.longitude), 15f
                    ),
                    durationMs = 700,
                )
            }
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Google Map ──────────────────────────────────────────────────────────
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties          = MapProperties(
                isMyLocationEnabled = locationPermission.status.isGranted,
                mapStyleOptions     = mapStyleOptions,
                mapType             = MapType.NORMAL,
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,  // Kita buat custom button sendiri
                zoomControlsEnabled     = false,
                compassEnabled          = false,
                mapToolbarEnabled       = false,
                rotationGesturesEnabled = true,
            ),
            onMapClick = {
                // Tap kosong di peta → tutup selection
                viewModel.clearSelection()
            },
        ) {

            uiState.filteredBuses.forEach { bus ->
                val location = bus.location ?: return@forEach
                val busLatLng = LatLng(location.latitude, location.longitude)
                val isSelected = bus.busId == uiState.selectedBus?.busId

                // ── Custom bus marker ───────────────────────────────────────────
                BusMarkerItem(
                    bus        = bus,
                    isSelected = isSelected,
                    onBusClick = { viewModel.selectBus(bus) },
                )

                // ── Actual GPS track (hijau solid) ──────────────────────────────
                if (bus.track.size >= 2) {
                    Polyline(
                        points      = bus.track.map { LatLng(it.lat, it.lng) },
                        color       = androidx.compose.ui.graphics.Color(0xFF10B981),
                        width       = 7f,
                        zIndex      = 1f,
                        jointType   = JointType.ROUND,
                        startCap    = RoundCap(),
                        endCap      = RoundCap(),
                    )
                }

                // ── Planned route / encoded polyline (biru putus-putus) ─────────
                bus.encodedRoute?.takeIf { it.isNotEmpty() }?.let { encoded ->
                    runCatching { PolyUtil.decode(encoded) }
                        .getOrNull()
                        ?.takeIf { it.size >= 2 }
                        ?.let { points ->
                            Polyline(
                                points  = points,
                                color   = androidx.compose.ui.graphics.Color(0x882196F3),
                                width   = 5f,
                                pattern = listOf(Dash(20f), Gap(12f)),
                            )
                        }
                }

                // ── Garis jarak user ↔ bus yang dipilih (abu-abu dotted) ─────────
                if (isSelected) {
                    uiState.userLocation?.let { userLoc ->
                        Polyline(
                            points  = listOf(userLoc, busLatLng),
                            color   = androidx.compose.ui.graphics.Color(0x669E9E9E),
                            width   = 3f,
                            pattern = listOf(Dot(), Gap(8f)),
                        )
                    }
                }
            }
        }

        // ── Overlay: Search bar + bus count ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            SearchBarComponent(
                query         = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onFilterClick = { showFilterDialog = true },
            )

            AnimatedVisibility(
                visible  = !uiState.isLoading,
                enter    = fadeIn() + slideInVertically(),
                exit     = fadeOut() + slideOutVertically(),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                BusCountChip(
                    count      = uiState.filteredBuses.size,
                    totalCount = uiState.allBuses.size,
                )
            }
        }

        // ── Overlay: Map Legend (kiri bawah) ───────────────────────────────────
        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 20.dp)
                .navigationBarsPadding(),
        )

        // ── Loading spinner ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = uiState.isLoading,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = White.copy(alpha = 0.92f),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        color     = Blue600,
                        strokeWidth = 2.5.dp,
                    )
                    Text(
                        text  = "Memuat data bus...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                    )
                }
            }
        }

        // ── Error snackbar (opsional) ───────────────────────────────────────────
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                containerColor = Red500,
                contentColor   = White,
            ) {
                Text(text = "Gagal memuat: $error", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    // ── Filter Dialog ───────────────────────────────────────────────────────────
    if (showFilterDialog) {
        FilterDialog(
            currentFilter        = uiState.filterClass,
            onFilterSelected     = viewModel::onFilterClassChange,
            onAvailabilityFilter = {
                viewModel.onFilterAvailable()
            },
            onDismiss            = { showFilterDialog = false },
        )
    }

    // ── Bus Bottom Sheet ────────────────────────────────────────────────────────
    if (uiState.showBottomSheet && uiState.selectedBus != null) {
        BusBottomSheet(
            bus          = uiState.selectedBus!!,
            userLocation = uiState.userLocation,
            onDismiss    = viewModel::clearSelection,
        )
    }
}

// ─── Internal sub-composables ─────────────────────────────────────────────────

/**
 * Chip kecil di bawah search bar yang menampilkan jumlah bus ditemukan.
 *
 * Contoh: "12 bus ditemukan" atau "3 / 12 bus (filter aktif)"
 */
@Composable
private fun BusCountChip(count: Int, totalCount: Int) {
    val isFiltered = count != totalCount && totalCount > 0
    val label = if (isFiltered) "$count / $totalCount bus" else "$count bus ditemukan"

    Surface(
        shape           = RoundedCornerShape(20.dp),
        color           = White.copy(alpha = 0.93f),
        shadowElevation = 4.dp,
        modifier        = Modifier.wrapContentWidth(),
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = if (isFiltered) Blue600 else Slate600,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}