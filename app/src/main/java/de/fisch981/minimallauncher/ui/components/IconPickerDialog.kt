package de.fisch981.minimallauncher.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.fisch981.minimallauncher.data.model.AppItem
import de.fisch981.minimallauncher.data.repository.IconPackManager
import de.fisch981.minimallauncher.ui.theme.DarkSurface
import de.fisch981.minimallauncher.ui.theme.DarkSurfaceVariant
import de.fisch981.minimallauncher.ui.theme.GrayMedium
import de.fisch981.minimallauncher.ui.theme.LocalAppStrings
import de.fisch981.minimallauncher.ui.theme.PureBlack
import de.fisch981.minimallauncher.ui.theme.PureWhite

@Composable
fun IconPickerDialog(
    app: AppItem,
    iconPackManager: IconPackManager,
    onSelectIcon: (drawableName: String) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    val allDrawables = remember {
        iconPackManager.getAllAvailableDrawables()
            .filter { !it.startsWith(".") && iconPackManager.getIconByName(it) != null }
    }

    val filteredDrawables = remember(searchQuery, allDrawables) {
        if (searchQuery.isBlank()) {
            allDrawables.take(80)
        } else {
            allDrawables.filter { it.contains(searchQuery.trim(), ignoreCase = true) }.take(80)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.changeIconForHeader,
                            fontSize = 13.sp,
                            color = GrayMedium
                        )
                        Text(
                            text = app.label,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.close, tint = PureWhite)
                    }
                }

                // Search field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(strings.searchIconsPlaceholder, color = GrayMedium, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = PureWhite, fontSize = 14.sp),
                        cursorBrush = SolidColor(PureWhite),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDrawables, key = { it }) { drawableName ->
                        val bitmap = remember(drawableName) {
                            iconPackManager.getIconByName(drawableName)
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(DarkSurface, RoundedCornerShape(8.dp))
                                .clickable { onSelectIcon(drawableName) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = drawableName,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text(
                                    text = drawableName.take(3),
                                    fontSize = 10.sp,
                                    color = GrayMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onResetDefault,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.restoreDefaultIconAction, fontSize = 14.sp)
                }
            }
        }
    }
}
