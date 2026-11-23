package de.fisch981.minimallauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.ui.theme.DarkSurfaceVariant
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite

@Composable
fun SearchOverlay(
    allApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onOpenSettings: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isSettingsMatch = remember(query) {
        query.isNotBlank() && ("einstellungen".contains(query.lowercase()) || "settings".contains(query.lowercase()) || "minimal".contains(query.lowercase()))
    }

    val filteredApps = remember(query, allApps) {
        if (query.isBlank()) {
            emptyList()
        } else {
            allApps.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.originalLabel.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(top = 16.dp)
    ) {
        // Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück",
                    tint = PureWhite
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Apps durchsuchen...",
                        color = GrayMedium,
                        fontSize = 18.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = PureWhite,
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(PureWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Löschen",
                        tint = GrayMedium
                    )
                }
            }
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            if (isSettingsMatch) {
                item(key = "launcher_settings_search_item") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClose()
                                onOpenSettings()
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "MinimalLauncher-Einstellungen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = PureWhite
                            )
                            Text(
                                text = "Nova Backup importieren, Favoriten, Design",
                                fontSize = 12.sp,
                                color = GrayMedium
                            )
                        }
                    }
                }
            }

            items(filteredApps, key = { it.key }) { app ->
                AppListItem(
                    app = app,
                    notifications = emptyList(),
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppClick(app) }
                )
            }
        }
    }
}
