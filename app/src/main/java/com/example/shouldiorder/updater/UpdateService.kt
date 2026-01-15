package com.example.shouldiorder.updater

import retrofit2.http.GET
import retrofit2.http.Url

data class UpdateInfo(
    val versionCode: Int,
    val url: String
)

interface UpdateService {
    @GET
    suspend fun checkForUpdates(@Url url: String): UpdateInfo
}
