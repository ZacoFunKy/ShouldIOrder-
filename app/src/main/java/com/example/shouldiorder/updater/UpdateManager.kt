package com.example.shouldiorder.updater

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

private const val APK_FILE_NAME = "app-release.apk"
private const val TAG = "UpdateManager"

class UpdateManager(private val context: Context) {

    private val updateJsonUrl = "https://raw.githubusercontent.com/ZacoFunKy/ShouldIOrder-/main/update.json"

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    suspend fun checkForUpdate() {
        Log.d(TAG, "Lancement de la vérification de la mise à jour sur: $updateJsonUrl")
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(updateJsonUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw Exception("Réponse du serveur inattendue: ${response.code}")
                    }

                    val json = response.body?.string() ?: return@use
                    val adapter = moshi.adapter(UpdateInfo::class.java)
                    val updateInfo = adapter.fromJson(json) ?: return@use

                    val currentVersionCode = context.getCurrentVersionCode()
                    Log.d(TAG, "Version distante: ${updateInfo.versionCode} | Version locale: $currentVersionCode")

                    if (updateInfo.versionCode > currentVersionCode) {
                        Log.d(TAG, "Nouvelle version trouvée. URL: ${updateInfo.url}")
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(updateInfo.url)
                        }
                    } else {
                        Log.d(TAG, "Application déjà à jour.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "La vérification de la mise à jour a échoué", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Erreur MàJ: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showUpdateDialog(apkUrl: String) {
        Log.d(TAG, "Affichage de la boîte de dialogue de mise à jour.")
        val builder = AlertDialog.Builder(context)
            .setTitle("Mise à jour disponible")
            .setNegativeButton("Plus tard", null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            builder
                .setMessage("Pour mettre à jour, veuillez autoriser l'installation depuis cette application.")
                .setPositiveButton("Autoriser") { _, _ ->
                    Log.d(TAG, "Ouverture des paramètres pour la permission d'installation.")
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", context.packageName)))
                    context.startActivity(intent)
                    Toast.makeText(context, "Veuillez accorder la permission, puis relancer la mise à jour.", Toast.LENGTH_LONG).show()
                }
        } else {
            builder
                .setMessage("Voulez-vous télécharger la nouvelle version ?")
                .setPositiveButton("Télécharger") { _, _ ->
                    downloadUpdate(apkUrl)
                }
        }
        builder.show()
    }

    private fun downloadUpdate(apkUrl: String) {
        Log.d(TAG, "Lancement du téléchargement depuis l'URL: $apkUrl")
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(apkUrl.toUri())
            .setTitle(context.getString(R.string.app_name))
            .setDescription("Téléchargement de la nouvelle version...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)

        downloadManager.enqueue(request)
        Toast.makeText(context, "Téléchargement en cours... Vérifiez vos notifications.", Toast.LENGTH_SHORT).show()
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
