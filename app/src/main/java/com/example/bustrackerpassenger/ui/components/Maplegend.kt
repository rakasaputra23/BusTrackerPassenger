package com.example.bustrackerpassenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.bustrackerpassenger.ui.theme.*

private data class LegendItem(
    val label: String,
    val color: Color,
    val isDashed: Boolean = false,
    val isDotted: Boolean = false,
)

private val legendItems = listOf(
    LegendItem("Planned Route", Color(0xFF2196F3), isDashed = true),
    LegendItem("Actual Track",  Color(0xFF10B981)),
    LegendItem("Your Distance", Color(0xFF9E9E9E), isDotted = true),
)

@Composable
fun MapLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), clip = false),
        shape          = RoundedCornerShape(14.dp),
        color          = White.copy(alpha = 0.95f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text     = "Map Legend",
                style    = MaterialTheme.typography.labelLarge,
                color    = Slate700,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            legendItems.forEach { item ->
                LegendRow(item)
            }
        }
    }
}

@Composable
private fun LegendRow(item: LegendItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Line preview
        Canvas(modifier = Modifier.size(width = 36.dp, height = 12.dp)) {
            val y = size.height / 2
            when {
                item.isDotted -> {
                    var x = 0f
                    while (x < size.width) {
                        drawCircle(color = item.color, radius = 2.5f, center = Offset(x, y))
                        x += 8f
                    }
                }
                item.isDashed -> {
                    drawLine(
                        color       = item.color,
                        start       = Offset(0f, y),
                        end         = Offset(size.width, y),
                        strokeWidth = 4f,
                        cap         = StrokeCap.Round,
                        pathEffect  = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 5f)
                        ),
                    )
                }
                else -> {
                    drawLine(
                        color       = item.color,
                        start       = Offset(0f, y),
                        end         = Offset(size.width, y),
                        strokeWidth = 4f,
                        cap         = StrokeCap.Round,
                    )
                }
            }
        }
        Text(
            text  = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate500,
        )
    }
}