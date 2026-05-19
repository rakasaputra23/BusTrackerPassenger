package com.example.bustrackerpassenger.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.bustrackerpassenger.ui.theme.*

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), clip = false),
        shape           = RoundedCornerShape(28.dp),
        color           = White,
        tonalElevation  = 0.dp,
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Search icon ──────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Slate100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Search,
                    contentDescription = null,
                    tint               = Slate400,
                    modifier           = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // ── Input field ───────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                BasicSearchField(
                    query         = query,
                    onQueryChange = onQueryChange,
                    onDone        = { focusManager.clearFocus() },
                )
                Text(
                    text     = "Contoh: Surabaya, Madiun",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = Slate300,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            // ── Clear button ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                IconButton(
                    onClick  = { onQueryChange("") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Close,
                        contentDescription = "Hapus",
                        tint               = Slate400,
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ── Filter button ────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Blue50),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick  = onFilterClick,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Tune,
                        contentDescription = "Filter",
                        tint               = Blue600,
                        modifier           = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    TextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = {
            Text(
                text  = "Cari Jurusan...",
                style = MaterialTheme.typography.titleSmall,
                color = Slate300,
            )
        },
        singleLine    = true,
        colors        = TextFieldDefaults.colors(
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor  = Color.Transparent,
            focusedTextColor        = Slate800,
            unfocusedTextColor      = Slate800,
            cursorColor             = Blue600,
        ),
        textStyle       = MaterialTheme.typography.titleSmall.copy(color = Slate800),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onDone() }),
        // ⚠️ FIX: contentPadding bukan parameter valid di Material3 TextField.
        // Gunakan modifier padding di luar atau biarkan default.
        modifier        = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    )
}