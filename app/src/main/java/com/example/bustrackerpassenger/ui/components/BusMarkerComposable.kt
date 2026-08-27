package com.example.bustrackerpassenger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bustrackerpassenger.models.Bus
import com.example.bustrackerpassenger.ui.theme.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberMarkerState

/**
 * Converter agar LatLng bisa dianimasikan oleh Animatable
 * (lat & lng diperlakukan sebagai 2 nilai float independen).
 */
private val LatLngToVector: TwoWayConverter<LatLng, AnimationVector2D> = TwoWayConverter(
    convertToVector = { latLng ->
        AnimationVector2D(latLng.latitude.toFloat(), latLng.longitude.toFloat())
    },
    convertFromVector = { vector ->
        LatLng(vector.v1.toDouble(), vector.v2.toDouble())
    }
)

/**
 * Durasi animasi perpindahan marker.
 * Firebase update tiap ~3-5 detik, jadi 2500ms cukup mulus
 * dan tetap "selesai" sebelum update berikutnya datang.
 * Kalau update baru datang saat animasi masih jalan,
 * Animatable akan otomatis re-target dari posisi saat ini (tidak snap).
 */
private const val MARKER_ANIMATION_DURATION_MS = 2500

@Composable
fun BusMarkerItem(
    bus: Bus,
    isSelected: Boolean,
    onBusClick: () -> Unit,
) {
    val location = bus.location ?: return
    val targetLatLng = LatLng(location.latitude, location.longitude)

    // Animatable menyimpan posisi "saat ini" yang dianimasikan secara kontinu.
    val animatedPosition = remember(bus.busId) {
        Animatable(targetLatLng, LatLngToVector)
    }

    val markerState = rememberMarkerState(position = animatedPosition.value)

    // Setiap kali lat/lng baru datang dari Firebase, animasikan dari posisi
    // SAAT INI (bukan posisi lama yang statis) menuju posisi baru.
    LaunchedEffect(targetLatLng) {
        animatedPosition.animateTo(
            targetValue = targetLatLng,
            animationSpec = tween(
                durationMillis = MARKER_ANIMATION_DURATION_MS,
            )
        )
    }

    // Sinkronkan setiap frame animasi ke posisi marker di peta.
    LaunchedEffect(animatedPosition.value) {
        markerState.position = animatedPosition.value
    }

    MarkerComposable(
        keys    = arrayOf(bus.busId, isSelected, bus.isOnline, bus.occupancyPercent),
        state   = markerState,
        onClick = {
            onBusClick()
            true
        },
    ) {
        BusMarkerContent(bus = bus, isSelected = isSelected)
    }
}

@Composable
fun BusMarkerContent(bus: Bus, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                if (!isSelected) {
                    Box(modifier = Modifier.size(5.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color  = if (bus.isOnline) Green500 else Slate300,
                                radius = size.minDimension / 2,
                            )
                        }
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