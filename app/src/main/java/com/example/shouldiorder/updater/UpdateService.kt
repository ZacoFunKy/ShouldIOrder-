package com.example.shouldiorder.updater

import com.squareup.moshi.JsonClass
import retrofit2.http.GET

// Correction : Ajout de l'annotation pour la génération de code KSP
@JsonClass(generateAdapter = true)
data class UpdateInfo(
    val versionCode: Int,
    val url: String
)

interface UpdateService {
    @GET("update.json")
    suspend fun checkForUpdates(): UpdateInfo
}
