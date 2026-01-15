package com.example.shouldiorder.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Utility pour ouvrir les apps de livraison
 */
object DeliveryUtils {

    fun openUberEats(context: Context) {
        try {
            // Essayer d'ouvrir l'app Uber Eats si elle est installée
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("ubereats://home")
                `package` = "com.ubercab.eats"
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Sinon, ouvrir le site web
                openWebsite(context, "https://www.ubereats.com")
            }
        } catch (e: Exception) {
            openWebsite(context, "https://www.ubereats.com")
        }
    }

    fun openDeliveroo(context: Context) {
        try {
            // Essayer d'ouvrir l'app Deliveroo si elle est installée
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("deliveroo://home")
                `package` = "com.deliveroo"
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Sinon, ouvrir le site web
                openWebsite(context, "https://deliveroo.fr")
            }
        } catch (e: Exception) {
            openWebsite(context, "https://deliveroo.fr")
        }
    }

    fun shareReason(context: Context, reason: String) {
        val shareText = "L'appli a dit : \"$reason\" 🍕 Donc on commande ? 😄"
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Partager la raison"))
    }

    private fun openWebsite(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    }
}

