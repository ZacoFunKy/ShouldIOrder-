package com.example.shouldiorder.data.weather

import android.content.Context
import com.example.shouldiorder.data.QuoteCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Provider pour la météo avec Open-Meteo.
 */
class WeatherProvider(context: Context) {

    private val weatherService: WeatherService

    init {
        // On s'assure que Moshi peut gérer les classes de données Kotlin.
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        weatherService = retrofit.create(WeatherService::class.java)
    }

    /**
     * Récupère la météo et retourne la catégorie de quote correspondante.
     * Retourne null si une erreur survient ou si la météo est "normale".
     */
    suspend fun getWeatherCategory(latitude: Double, longitude: Double): QuoteCategory? {
        return try {
            val response = weatherService.getCurrentWeather(latitude, longitude)
            analyzeWeatherCategory(response)
        } catch (e: Exception) {
            // En cas d'erreur (réseau, parsing, etc.), on retourne null pour un fallback sûr.
            null
        }
    }

    /**
     * Analyse les conditions météo (codes WMO) pour déterminer la catégorie de citation appropriée.
     */
    private fun analyzeWeatherCategory(response: WeatherResponse): QuoteCategory? {
        val weatherCode = response.currentWeather.weathercode
        val temp = response.currentWeather.temperature

        return when {
            // Pluie / Orage (codes WMO: 51-67, 80-82, 95-99)
            weatherCode in 51..67 || weatherCode in 80..82 || weatherCode in 95..99 -> QuoteCategory.RAIN
            // Neige ou Froid Extrême (< 5°C)
            weatherCode in 71..77 || weatherCode in 85..86 || temp < 5 -> QuoteCategory.COLD
            // Chaleur extrême (> 30°C)
            temp > 30 -> QuoteCategory.HEAT
            // Météo normale (ciel clair, nuageux, etc.)
            else -> null
        }
    }
}
