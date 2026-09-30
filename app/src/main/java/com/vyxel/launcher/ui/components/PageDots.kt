package com.vyxel.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    if (count <= 1) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == selected) 8.dp else 7.dp)
                    .clip(CircleShape)
                    .background(if (index == selected) Color.White else Color.White.copy(alpha = 0.35f))
            )
        }
    }
}
