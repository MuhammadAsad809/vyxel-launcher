package com.vyxel.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vyxel.launcher.core.model.ClockStyle
import com.vyxel.launcher.core.model.WeatherSnapshot
import com.vyxel.launcher.ui.theme.LocalVyxelFont
import com.vyxel.launcher.ui.theme.LocalVyxelTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockFace(
    style: ClockStyle,
    weather: WeatherSnapshot?,
    showWeather: Boolean,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(15_000)
        }
    }
    val time = remember(now) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val date = remember(now) { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now) }
    val font = LocalVyxelFont.current
    val theme = LocalVyxelTheme.current

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (style) {
            ClockStyle.ANALOG -> AnalogClock(Modifier.height(120.dp).fillMaxWidth().padding(8.dp))
            ClockStyle.MINIMAL -> Text(time, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Thin, fontFamily = font)
            ClockStyle.DIGITAL -> Text(time, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Medium, fontFamily = font)
            ClockStyle.IOS -> ReferenceDashboard(time, date, weather, font, theme.accent)
        }
        if (style != ClockStyle.IOS) {
            Text(date, color = Color.White.copy(alpha = 0.82f), fontSize = 16.sp, fontFamily = font)
        }
        if (showWeather && weather != null && style != ClockStyle.IOS) {
            Text(
                "${weather.city}  ${weather.celsius}°  ${weather.condition}",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 14.sp,
                fontFamily = font,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ReferenceDashboard(
    time: String,
    date: String,
    weather: WeatherSnapshot?,
    font: FontFamily,
    accent: Color
) {
    val panel = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .border(1.dp, Color.White.copy(alpha = 0.24f), panel)
            .background(Color.Black.copy(alpha = 0.38f), panel)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("VYXEL  /  HOME", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp, letterSpacing = 2.sp, fontFamily = font)
                Text(time, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Light, fontFamily = font)
                Text(date.uppercase(Locale.getDefault()), color = Color.White.copy(alpha = 0.58f), fontSize = 10.sp, letterSpacing = 1.2.sp, fontFamily = font)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("WEATHER", color = Color.White.copy(alpha = 0.48f), fontSize = 9.sp, letterSpacing = 1.4.sp, fontFamily = font)
                Text(weather?.let { "${it.celsius}°" } ?: "—°", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light, fontFamily = font)
                Text(weather?.condition?.uppercase() ?: "NO DATA", color = Color.White.copy(alpha = 0.58f), fontSize = 9.sp, letterSpacing = 1.sp, fontFamily = font)
            }
        }
        Spacer(Modifier.height(16.dp))
        DashboardRule(accent)
        Spacer(Modifier.height(12.dp))
        DashboardRow("SYSTEM STATUS", "READY", accent)
        DashboardRow("FOCUS MODE", "ACTIVE", Color.White.copy(alpha = 0.72f))
        DashboardRow("CONNECTIVITY", "SECURE", Color.White.copy(alpha = 0.72f))
    }
}

@Composable
private fun DashboardRule(accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BoxDot(accent)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.18f))
        )
        Spacer(Modifier.width(8.dp))
        Text("LIVE", color = accent, fontSize = 9.sp, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun DashboardRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        Text(value, color = color, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun BoxDot(color: Color) {
    Box(
        Modifier
            .size(6.dp)
            .background(color, RoundedCornerShape(50))
    )
}

@Composable
private fun AnalogClock(modifier: Modifier) {
    Canvas(modifier) {
        val cal = Calendar.getInstance()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f * 0.9f
        drawCircle(Color.White.copy(alpha = 0.18f), r, Offset(cx, cy))
        val hour = (cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) / 60f) * 30f
        val minute = cal.get(Calendar.MINUTE) * 6f
        fun hand(angleDeg: Float, length: Float, width: Float) {
            val rad = Math.toRadians((angleDeg - 90).toDouble())
            drawLine(
                Color.White,
                Offset(cx, cy),
                Offset(cx + (cos(rad) * length).toFloat(), cy + (sin(rad) * length).toFloat()),
                strokeWidth = width,
                cap = StrokeCap.Round
            )
        }
        hand(hour, r * 0.5f, 8f)
        hand(minute, r * 0.72f, 5f)
        drawCircle(Color.White, 6f, Offset(cx, cy))
    }
}
