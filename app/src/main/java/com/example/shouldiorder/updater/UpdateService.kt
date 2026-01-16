package com.example.shouldiorder.updater

import retrofit2.http.GET

// La data class ne change pas
data class UpdateInfo(
    val versionCode: Int,
    val url: String
)

interface UpdateService {
    // On spécifie le fichier directement. Retrofit le combinera avec la baseUrl.
    @GET("update.json")
    suspend fun checkForUpdates(): UpdateInfo
}
