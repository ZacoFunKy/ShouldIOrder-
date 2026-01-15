package com.example.shouldiorder.data.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Réponses de l'API Open-Meteo (gratuit, pas de clé API nécessaire)
 */
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current_weather")
    val currentWeather: CurrentWeather
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature")
    val temperature: Double,
    @Json(name = "weathercode")
    val weathercode: Int,
    @Json(name = "windspeed")
    val windspeed: Double,
    @Json(name = "winddirection")
    val winddirection: Int?,
    @Json(name = "time")
    val time: String
)
