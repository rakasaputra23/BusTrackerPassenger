package com.example.bustrackerpassenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bustrackerpassenger.models.Bus
import com.example.bustrackerpassenger.ui.theme.*

/**
 * Composable untuk custom info window di marker peta.
 * Ditampilkan saat marker bus di-tap — compact & clean.
 */
@Composable
fun BusInfoWindow(bus: Bus) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // ─── Header: Nama + Status dot ────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (bus.isOnline) Green500 else Slate300)
            )
            Text(
                text  = run {
                    val name = bus.displayName
                    if (name.length > 14) name.take(12) + "…" else name
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp,
                    color      = Slate800,
                ),
                maxLines = 1,
            )
        }

        // ─── Plate + Class ────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = bus.plateNumber ?: "-",
                style = MaterialTheme.typography.labelSmall.copy(color = Slate400),
            )
            if (bus.busClass != null) {
                Text(
                    text  = "•",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate300),
                )
                Text(
                    text  = bus.busClass.uppercase().take(3),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = Blue600,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }

        // ─── Divider ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Slate200)
        )

        // ─── Stats ────────────────────────────────────────────────────────────
        InfoRow(label = "Penumpang", value = bus.occupancyText + " (${bus.occupancyPercent}%)")
        InfoRow(
            label = "Kecepatan",
            value = "%.1f km/j".format(bus.location?.speed ?: 0f),
        )
        InfoRow(
            label = "Jarak",
            value = if (bus.totalDistance != null) "%.2f km".format(bus.totalDistance) else "0 km",
        )

        // ─── Tap hint ─────────────────────────────────────────────────────────
        Text(
            text  = "Tap untuk detail →",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Blue500,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Slate400),
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color      = Slate700,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}