package com.example.bustrackerpassenger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bustrackerpassenger.models.Bus
import com.example.bustrackerpassenger.ui.theme.*
import com.example.bustrackerpassenger.utils.PassengerETACalculator
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusBottomSheet(
    bus: Bus,
    userLocation: LatLng?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest    = onDismiss,
        sheetState          = sheetState,
        containerColor      = White,
        dragHandle          = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Slate200),
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            BusHeader(bus)

            // ── Speed badge ───────────────────────────────────────────────────
            SpeedBadge(speed = bus.location?.speed ?: 0f)

            // ── Route card ────────────────────────────────────────────────────
            RouteCard(route = bus.route ?: "N/A")

            // ── 3 info boxes: ETA | Jarak | Kursi ────────────────────────────
            InfoBoxRow(bus = bus, userLocation = userLocation)

            // ── ETA Detail ────────────────────────────────────────────────────
            bus.eta?.let { EtaDetailCard(it) }

            // ── Total Distance ───────────────────────────────────────────────
            TotalDistanceCard(totalDistance = bus.totalDistance)

            // ── Traffic Condition ─────────────────────────────────────────────
            bus.kondisi?.let { KondisiCard(kondisi = it, kondisiUpdate = bus.kondisiUpdate) }

            // ── Driver ────────────────────────────────────────────────────────
            DriverCard(driver = bus.driver ?: "N/A")

            // ── Last update ───────────────────────────────────────────────────
            LastUpdateCard(lastUpdate = bus.location?.lastUpdate)
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun BusHeader(bus: Bus) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = bus.displayName,
                style = MaterialTheme.typography.headlineLarge,
                color = Slate800,
            )
            Text(
                text     = bus.plateNumber ?: "-",
                style    = MaterialTheme.typography.bodyMedium,
                color    = Slate400,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // Class badge
        bus.busClass?.let { cls ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Blue50,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text     = cls.uppercase(),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = Blue600,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun SpeedBadge(speed: Float) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate100,
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Speed,
                    contentDescription = null,
                    tint               = Blue600,
                    modifier           = Modifier.size(18.dp),
                )
                Text(
                    text  = "%.0f km/j".format(speed),
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate800,
                )
            }
        }
    }
}

@Composable
private fun RouteCard(route: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Slate50,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dot-line-dot indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(end = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Blue500)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(Slate300)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Red500)
                )
            }
            Column {
                Text(
                    text  = "RUTE",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = Slate400,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = route,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate800,
                )
            }
        }
    }
}

@Composable
private fun InfoBoxRow(bus: Bus, userLocation: LatLng?) {
    // ✅ FIX: ESTIMASI & JARAK sekarang pakai Google Routes API (PassengerETACalculator),
    // sama seperti pendekatan ETACalculator di crew app — supaya mengikuti jalur jalan asli
    // + traffic-aware, bukan garis lurus (Haversine).
    // Dipanggil async via LaunchedEffect dengan throttling (ETA_THROTTLE_MS) supaya tidak
    // spam request tiap kali lokasi bus update dari Firebase.

    var distanceKm by remember { mutableStateOf<Double?>(null) }
    var etaMinutes by remember { mutableStateOf<Int?>(null) }
    var lastCalculatedAt by remember { mutableStateOf(0L) }

    val busLat = bus.location?.latitude
    val busLng = bus.location?.longitude

    LaunchedEffect(userLocation, busLat, busLng) {
        if (userLocation == null || busLat == null || busLng == null) {
            distanceKm = null
            etaMinutes = null
            return@LaunchedEffect
        }

        val now = System.currentTimeMillis()
        // Throttle: skip kalau baru saja dihitung < 15 detik lalu
        if (now - lastCalculatedAt < 15_000L && lastCalculatedAt != 0L) return@LaunchedEffect
        lastCalculatedAt = now

        val result = PassengerETACalculator.calculateEta(
            userLocation.latitude, userLocation.longitude,
            busLat, busLng
        )
        result.fold(
            onSuccess = { eta ->
                distanceKm = eta.distanceKm
                etaMinutes = eta.durationMinutes
            },
            onFailure = {
                // Fallback Haversine kalau Routes API gagal (mis. tidak ada koneksi)
                val fallback = PassengerETACalculator.calculateEtaManual(
                    userLocation.latitude, userLocation.longitude,
                    busLat, busLng,
                    averageSpeedKmh = if ((bus.location.speed) > 5f) bus.location.speed else 30f
                )
                distanceKm = fallback.distanceKm
                etaMinutes = fallback.durationMinutes
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ETA box — primary (blue)
        // ✅ FIX: kalau >= 60 menit, tampilkan ringkas "1j 10m" (value) tanpa unit terpisah,
        // supaya tidak perlu ubah ukuran teks. Kalau < 60 menit, tetap format lama (angka + "Menit").
        val etaDisplay = formatDurationCompact(etaMinutes)
        InfoBox(
            modifier  = Modifier.weight(1f),
            label     = "ESTIMASI",
            value     = etaDisplay.value,
            unit      = etaDisplay.unit,
            bgColor   = Blue600,
            textColor = White,
        )
        // Jarak box
        InfoBox(
            modifier  = Modifier.weight(1f),
            label     = "JARAK",
            value     = if (distanceKm != null) "%.1f".format(distanceKm) else "-",
            unit      = "Km",
            bgColor   = Slate100,
            textColor = Slate800,
            labelColor = Slate500,
        )
        // Kursi box
        InfoBox(
            modifier  = Modifier.weight(1f),
            label     = "SISA KURSI",
            value     = bus.availableSeats.toString(),
            unit      = "Seat",
            bgColor   = Green50,
            textColor = Green700,
            labelColor = Green600,
        )
    }
}

@Composable
private fun InfoBox(
    modifier: Modifier   = Modifier,
    label: String,
    value: String,
    unit: String?        = null, // ✅ FIX: opsional, supaya value yang sudah lengkap (mis. "1j 10m") tidak perlu unit tambahan
    bgColor: Color,
    textColor: Color,
    labelColor: Color    = textColor.copy(alpha = 0.7f),
) {
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = bgColor,
        modifier = modifier,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = labelColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = value,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                color = textColor,
            )
            if (unit != null) {
                Text(
                    text  = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun EtaDetailCard(eta: Bus.Eta) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Blue50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint               = Blue600,
                    modifier           = Modifier.size(18.dp),
                )
                Text(
                    text  = "Estimasi Kedatangan",
                    style = MaterialTheme.typography.titleSmall,
                    color = Blue700,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EtaDetailItem(
                    modifier = Modifier.weight(1f),
                    label    = "Waktu Tiba",
                    value    = parseArrivalTime(eta.estimatedArrival),
                )
                EtaDetailItem(
                    modifier = Modifier.weight(1f),
                    label    = "Jarak Tersisa",
                    value    = eta.remainingDistance?.let { "%.1f km".format(it) } ?: "-",
                )
            }

            // ✅ FIX: pakai formatDuration supaya 70 menit tampil "1 jam 10 menit", bukan "70 menit"
            EtaDetailItem(
                label = "Durasi Perjalanan",
                value = formatDuration(eta.remainingTime),
            )
        }
    }
}

@Composable
private fun EtaDetailItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.titleMedium,
            color = Slate800,
        )
    }
}

@Composable
private fun TotalDistanceCard(totalDistance: Double?) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Slate50,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate200),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Timeline,
                    contentDescription = null,
                    tint               = Slate500,
                    modifier           = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text  = "Total Jarak Tempuh",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                )
                Text(
                    text  = totalDistance?.let { "%.1f km".format(it) } ?: "0 km",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate800,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun KondisiCard(kondisi: String, kondisiUpdate: Long?) {  // FIX: Long? bukan String?
    val kondisiColor by animateColorAsState(
        targetValue = when (kondisi.lowercase()) {
            "lancar" -> KondisiLancar
            "macet"  -> KondisiMacet
            "mogok"  -> KondisiMogok
            else     -> Slate400
        },
        label = "kondisiColor"
    )
    val kondisiBg = kondisiColor.copy(alpha = 0.08f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = kondisiBg,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Rounded.Traffic,
                contentDescription = null,
                tint               = kondisiColor,
                modifier           = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Kondisi Lalu Lintas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                )
                kondisiUpdate?.let {
                    Text(
                        text  = "Update: ${getTimeAgo(it)}",  // FIX: pakai Long langsung
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = kondisiColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text     = kondisi.uppercase(),
                    style    = MaterialTheme.typography.labelLarge,
                    color    = kondisiColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun DriverCard(driver: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Slate50,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Blue100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Person,
                    contentDescription = null,
                    tint               = Blue600,
                    modifier           = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    text  = "Pengemudi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                )
                Text(
                    text  = driver,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate800,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LastUpdateCard(lastUpdate: Long?) {  // FIX: Long? bukan String?
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate100,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector        = Icons.Rounded.Sync,
                contentDescription = null,
                tint               = Slate400,
                modifier           = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text  = buildString {
                    append("Data real-time")
                    if (lastUpdate != null) append(" • Update ${getTimeAgo(lastUpdate)}")  // FIX: Long langsung
                },
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Slate400,
            )
        }
    }
}

// ─── Helper functions ─────────────────────────────────────────────────────────

/**
 * ✅ TAMBAH: versi ringkas untuk InfoBox (value besar + unit kecil di bawahnya).
 * < 60 menit  -> value="45", unit="Menit"
 * >= 60 menit -> value="1j 10m", unit=null (sudah lengkap, tidak perlu unit tambahan)
 */
private data class CompactDuration(val value: String, val unit: String?)

private fun formatDurationCompact(minutes: Int?): CompactDuration {
    if (minutes == null) return CompactDuration("-", "Menit")
    if (minutes < 60) return CompactDuration(minutes.toString(), "Menit")
    val jam = minutes / 60
    val sisaMenit = minutes % 60
    val text = if (sisaMenit == 0) "${jam}j" else "${jam}j ${sisaMenit}m"
    return CompactDuration(text, null)
}

/**
 * ✅ TAMBAH: format durasi dalam menit menjadi teks yang lebih mudah dibaca.
 * < 60 menit  -> "45 menit"
 * >= 60 menit -> "1 jam 10 menit" (atau "1 jam" kalau pas habis)
 */
private fun formatDuration(minutes: Int?): String {
    if (minutes == null) return "-"
    if (minutes < 60) return "$minutes menit"
    val jam = minutes / 60
    val sisaMenit = minutes % 60
    return if (sisaMenit == 0) "$jam jam" else "$jam jam $sisaMenit menit"
}


// ✅ FIX: HAPUS asumsi UTC saat parsing.
// Root cause: string "estimatedArrival" yang disimpan di RTDB oleh ETACalculator.formatTimestamp()
// sebenarnya SUDAH memakai timezone device (WIB) meskipun diberi akhiran 'Z' (mislabel, bukan UTC asli).
// Kode lama memaksa parse sebagai UTC lalu format ulang ke WIB → menambahkan +7 jam ekstra
// (18:50 jadi tampil 01:50). Data di RTDB sendiri sudah benar, jadi cukup parse & tampilkan apa adanya
// tanpa konversi timezone tambahan.
private fun parseArrivalTime(estimatedArrival: String?): String {
    if (estimatedArrival == null) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        // TIDAK di-set ke UTC — biarkan default timezone device (WIB),
        // karena string yang datang sudah representasi jam lokal.
        val date = inputFormat.parse(estimatedArrival) ?: return "-"
        SimpleDateFormat("HH:mm", Locale.US).format(date)
    } catch (e: Exception) {
        "-"
    }
}

// FIX: parameter diubah dari String ke Long (timestamp dalam milliseconds)
private fun getTimeAgo(timestamp: Long): String {
    return try {
        val diff    = System.currentTimeMillis() - timestamp
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours   = TimeUnit.MILLISECONDS.toHours(diff)
        val days    = TimeUnit.MILLISECONDS.toDays(diff)
        when {
            seconds < 60 -> "$seconds detik lalu"
            minutes < 60 -> "$minutes menit lalu"
            hours   < 24 -> "$hours jam lalu"
            else         -> "$days hari lalu"
        }
    } catch (e: Exception) {
        "N/A"
    }
}