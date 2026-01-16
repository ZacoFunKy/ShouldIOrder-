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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

private const val APK_FILE_NAME = "app-release.apk"

class UpdateManager(private val context: Context) {

    private val updateJsonUrl = "https://raw.githubusercontent.com/ZacoFunKy/ShouldIOrder-/main/update.json"

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    suspend fun checkForUpdate() {
        // Log de débogage pour confirmer que la fonction est bien appelée
        Log.d("UpdateManager", "Lancement de la vérification de la mise à jour...")

        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(updateJsonUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Réponse du serveur inattendue: ${response.code}")

                    val json = response.body?.string() ?: return@use
                    val adapter = moshi.adapter(UpdateInfo::class.java)
                    val updateInfo = adapter.fromJson(json) ?: return@use

                    if (updateInfo.versionCode > context.getCurrentVersionCode()) {
                        Log.d("UpdateManager", "Nouvelle version trouvée: ${updateInfo.versionCode}")
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(updateInfo.url)
                        }
                    } else {
                        Log.d("UpdateManager", "Application à jour.")
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateManager", "La vérification de la mise à jour a échoué", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erreur MàJ: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showUpdateDialog(apkUrl: String) {
        AlertDialog.Builder(context)
            .setTitle("Mise à jour disponible")
            .setMessage("Une nouvelle version est disponible. Voulez-vous la télécharger ?")
            .setPositiveButton("Télécharger") { _, _ -> downloadAndInstall(apkUrl) }
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
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)

        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    context.unregisterReceiver(this)
                    val downloadedApk = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
                    installApk(downloadedApk)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse(String.format("package:%s", context.packageName))))
                return
            }
        }

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
