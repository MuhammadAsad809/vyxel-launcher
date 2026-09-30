package com.vyxel.launcher.core.weather

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.vyxel.launcher.core.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class WeatherRepository(private val context: Context) {
    suspend fun fetch(cityOverride: String): WeatherSnapshot? = withContext(Dispatchers.IO) {
        val coords = resolveCoords(cityOverride) ?: return@withContext null
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=${coords.first}&longitude=${coords.second}" +
                "&current=temperature_2m,weather_code&timezone=auto"
        )
        val body = httpGet(url) ?: return@withContext null
        val json = JSONObject(body)
        val current = json.optJSONObject("current") ?: return@withContext null
        val temp = current.optDouble("temperature_2m", Double.NaN)
        if (temp.isNaN()) return@withContext null
        val code = current.optInt("weather_code", 0)
        WeatherSnapshot(
            city = coords.third,
            celsius = temp.toInt(),
            condition = conditionFor(code),
            code = code
        )
    }

    private fun resolveCoords(cityOverride: String): Triple<Double, Double, String>? {
        if (cityOverride.isNotBlank()) {
            val geo = runCatching {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault()).getFromLocationName(cityOverride, 1)
            }.getOrNull()
            val first = geo?.firstOrNull() ?: return null
            return Triple(first.latitude, first.longitude, cityOverride)
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = runCatching {
            lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }.getOrNull() ?: return null
        val city = runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)
                ?.firstOrNull()?.locality
        }.getOrNull() ?: "Here"
        return Triple(loc.latitude, loc.longitude, city)
    }

    private fun httpGet(url: URL): String? {
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            requestMethod = "GET"
        }
        return try {
            if (conn.responseCode !in 200..299) null
            else conn.inputStream.bufferedReader().readText()
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun conditionFor(code: Int): String = when (code) {
        0 -> "Clear"
        1, 2 -> "Mostly clear"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
        71, 73, 75, 77, 85, 86 -> "Snow"
        95, 96, 99 -> "Storm"
        else -> "Clouds"
    }
}
