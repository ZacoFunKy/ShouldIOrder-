package com.example.shouldiorder.data.weather

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Service Retrofit pour Open-Meteo API (gratuit, pas de clé API)
 * https://open-meteo.com/
 */
interface WeatherService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse
}


