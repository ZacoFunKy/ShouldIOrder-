package com.example.shouldiorder.updater

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.shouldiorder.R
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

private const val APK_FILE_NAME = "app-release.apk"

class UpdateManager(private val context: Context) {

    private val updateJsonUrl = "https://raw.githubusercontent.com/ZacoFunKy/ShouldIOrder-/main/update.json"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Ajout de l'adapteur pour Kotlin
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/ZacoFunKy/ShouldIOrder-/main/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val updateService = retrofit.create(UpdateService::class.java)

    suspend fun checkForUpdate() {
        withContext(Dispatchers.IO) {
            try {
                val updateInfo = updateService.checkForUpdates()
                val currentVersionCode = context.getCurrentVersionCode()

                if (updateInfo.versionCode > currentVersionCode) {
                    withContext(Dispatchers.Main) {
                        showUpdateDialog(updateInfo.url)
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "La vérification de la mise à jour a échoué", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erreur de vérification de mise à jour", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUpdateDialog(apkUrl: String) {
        AlertDialog.Builder(context)
            .setTitle("Mise à jour disponible")
            .setMessage("Une nouvelle version est disponible. Voulez-vous la télécharger ?")
            .setPositiveButton("Télécharger") { _, _ ->
                downloadAndInstall(apkUrl)
            }
            .setNegativeButton("Plus tard", null)
            .show()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun downloadAndInstall(apkUrl: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(apkUrl.toUri())
            .setTitle(context.getString(R.string.app_name))
            .setDescription("Téléchargement de la nouvelle version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    installApk(APK_FILE_NAME)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", context.packageName)))
                context.startActivity(intent)
                return
            }
        }

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        val authority = "${context.packageName}.provider"
        val apkUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

fun Context.getCurrentVersionCode(): Long {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        -1
    }
}
