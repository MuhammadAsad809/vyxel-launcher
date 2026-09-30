package com.vyxel.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.icons.IconPackManager
import com.vyxel.launcher.core.icons.asComposeShape
import com.vyxel.launcher.core.model.LaunchApp
import com.vyxel.launcher.core.model.LauncherSettings
import com.vyxel.launcher.ui.theme.LocalVyxelFont

@Composable
fun AppIcon(
    app: LaunchApp,
    settings: LauncherSettings,
    icons: IconPackManager,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    size: Dp = settings.iconSizeDp.dp,
    interactive: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val px = with(density) { size.roundToPx() }
    val bitmap = remember(app.component, settings.iconPackPackage, settings.customIcons, px) {
        runCatching { icons.bitmapFor(app, settings.iconPackPackage, settings.customIcons, px) }.getOrNull()
    }
    val shape = settings.iconShape.asComposeShape()
    val clickMod = if (interactive) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    } else {
        Modifier
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.then(clickMod)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .background(Color(0xFF2A3142)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    app.label.take(1).uppercase(),
                    color = Color.White,
                    fontFamily = LocalVyxelFont.current
                )
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontFamily = LocalVyxelFont.current,
                modifier = Modifier.width(size + 16.dp)
            )
        }
    }
}

@Composable
fun FolderGlyph(
    apps: List<LaunchApp>,
    settings: LauncherSettings,
    icons: IconPackManager,
    size: Dp,
    name: String,
    showLabel: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val shape = settings.iconShape.asComposeShape()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.18f))
        ) {
            val mini = size / 3
            apps.take(4).forEachIndexed { index, app ->
                val row = index / 2
                val col = index % 2
                Box(
                    Modifier
                        .align(
                            when {
                                row == 0 && col == 0 -> Alignment.TopStart
                                row == 0 && col == 1 -> Alignment.TopEnd
                                row == 1 && col == 0 -> Alignment.BottomStart
                                else -> Alignment.BottomEnd
                            }
                        )
                        .size(mini + 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        app = app,
                        settings = settings,
                        icons = icons,
                        showLabel = false,
                        size = mini,
                        interactive = false
                    )
                }
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = LocalVyxelFont.current
            )
        }
    }
}
