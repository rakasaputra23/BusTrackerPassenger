package com.example.bustrackerpassenger.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bustrackerpassenger.models.Bus
import com.example.bustrackerpassenger.ui.theme.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberMarkerState

/**
 * Wrapper composable yang menaruh [BusMarkerContent] sebagai custom marker
 * di dalam GoogleMap composable.
 *
 * @param bus        Data bus yang akan ditampilkan
 * @param isSelected Apakah marker ini sedang dipilih
 * @param onBusClick Callback saat marker di-tap
 */
@Composable
fun BusMarkerItem(
    bus: Bus,
    isSelected: Boolean,
    onBusClick: () -> Unit,
) {
    val location = bus.location ?: return

    val markerState = rememberMarkerState(
        position = LatLng(location.latitude, location.longitude)
    )

    // Sinkronisasi posisi marker dengan data Firebase realtime
    LaunchedEffect(location.latitude, location.longitude) {
        markerState.position = LatLng(location.latitude, location.longitude)
    }

    MarkerComposable(
        // keys memicu re-render marker saat data berubah
        keys     = arrayOf(bus.busId, isSelected, bus.isOnline, bus.occupancyPercent),
        state    = markerState,
        onClick  = {
            onBusClick()
            true // consume event agar info window default tidak muncul
        },
    ) {
        BusMarkerContent(bus = bus, isSelected = isSelected)
    }
}

/**
 * Tampilan visual marker bus — bubble label + triangle pointer.
 *
 * Desain:
 * - Normal   : putih dengan border hijau (online) / abu (offline)
 * - Selected : biru solid (Blue600)
 */
@Composable
fun BusMarkerContent(bus: Bus, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // ── Bubble ────────────────────────────────────────────────────────────
        Surface(
            shape           = RoundedCornerShape(12.dp),
            color           = if (isSelected) Blue600 else White,
            shadowElevation = if (isSelected) 12.dp else 4.dp,
            border          = if (!isSelected) BorderStroke(
                width = 1.5.dp,
                color = if (bus.isOnline) Green500 else Slate300,
            ) else null,
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .also {
                            // Hanya tampilkan dot saat tidak selected
                            if (isSelected) return@also
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color  = if (bus.isOnline) Green500 else Slate300,
                            radius = size.minDimension / 2,
                        )
                    }
                }

                Icon(
                    imageVector        = Icons.Rounded.DirectionsBus,
                    contentDescription = null,
                    tint               = if (isSelected) White else Blue600,
                    modifier           = Modifier.size(14.dp),
                )

                Text(
                    text       = bus.displayName.take(12),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = if (isSelected) White else Slate800,
                    maxLines   = 1,
                )
            }
        }

        // ── Triangle pointer ──────────────────────────────────────────────────
        val pointerColor = if (isSelected) Blue600 else White
        Canvas(modifier = Modifier.size(width = 14.dp, height = 7.dp)) {
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                },
                color = pointerColor,
            )
        }
    }
}