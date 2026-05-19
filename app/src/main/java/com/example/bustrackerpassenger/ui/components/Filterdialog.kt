package com.example.bustrackerpassenger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.bustrackerpassenger.ui.theme.*

private data class FilterOption(
    val label: String,
    val icon: ImageVector,
    val value: String?,
    val isAvailability: Boolean = false,
)

private val filterOptions = listOf(
    FilterOption("Semua Bus",                   Icons.Rounded.DirectionsBus, null),
    FilterOption("Ekonomi",                     Icons.Rounded.Chair,         "Ekonomi"),
    FilterOption("Eksekutif",                   Icons.Rounded.AirlineSeatReclineExtra, "Eksekutif"),
    FilterOption("VIP",                         Icons.Rounded.StarBorder,    "VIP"),
    FilterOption("Kursi Tersedia",              Icons.Rounded.EventSeat,     null, true),
)

@Composable
fun FilterDialog(
    currentFilter: String?,
    onFilterSelected: (String?) -> Unit,
    onAvailabilityFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(20.dp),
        containerColor   = White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Rounded.FilterList,
                    contentDescription = null,
                    tint               = Blue600,
                    modifier           = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = "Filter Bus",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate800,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Tampilkan bus berdasarkan:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                filterOptions.forEach { option ->
                    val isSelected = when {
                        option.isAvailability -> false
                        else -> option.value == currentFilter
                    }

                    FilterOptionRow(
                        option     = option,
                        isSelected = isSelected,
                        onClick    = {
                            if (option.isAvailability) {
                                onAvailabilityFilter()
                            } else {
                                onFilterSelected(option.value)
                            }
                            onDismiss()
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text  = "Tutup",
                    color = Slate500,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
private fun FilterOptionRow(
    option: FilterOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick      = onClick,
        shape        = RoundedCornerShape(12.dp),
        color        = if (isSelected) Blue50 else Slate50,
        border       = if (isSelected)
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Blue200),
            ) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = option.icon,
                contentDescription = null,
                tint               = if (isSelected) Blue600 else Slate400,
                modifier           = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text  = option.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Blue700 else Slate700,
            )
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector        = Icons.Rounded.Check,
                    contentDescription = null,
                    tint               = Blue600,
                    modifier           = Modifier.size(18.dp),
                )
            }
        }
    }
}