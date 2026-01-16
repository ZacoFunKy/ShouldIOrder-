package com.example.shouldiorder.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utility pour ouvrir les apps de livraison et autres actions.
 */
object DeliveryUtils {

    private const val UBER_EATS_PACKAGE = "com.ubercab.eats"
    private const val DELIVEROO_PACKAGE = "com.deliveroo.orderapp"

    fun openUberEats(context: Context) {
        openAppOrPlayStore(context, UBER_EATS_PACKAGE)
    }

    fun openDeliveroo(context: Context) {
        openAppOrPlayStore(context, DELIVEROO_PACKAGE)
    }

    /**
     * Tente d'ouvrir une application par son nom de package.
     * Si elle n'est pas installée, ouvre sa page sur le Play Store.
     */
    private fun openAppOrPlayStore(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            // L'application est installée, on la lance.
            context.startActivity(launchIntent)
        } else {
            // L'application n'est pas installée, on redirige vers le Play Store.
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: ActivityNotFoundException) {
                // Si le Play Store n'est pas disponible, on ouvre le site web.
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
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
}
