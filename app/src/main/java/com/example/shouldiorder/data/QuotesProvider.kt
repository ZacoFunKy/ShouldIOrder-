package com.example.shouldiorder.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Provider pour les citations, implémenté en singleton pour agir comme un cache en mémoire.
 * Les citations sont chargées du JSON une seule fois et conservées pour les lancements futurs.
 */
object QuotesProvider {

    private var quotesData: QuotesData? = null
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private var isInitialized = false

    /**
     * Initialise le provider en chargeant les citations. Ne fait le travail qu'une seule fois.
     */
    suspend fun initialize(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)

        return@withContext try {
            val jsonString = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            val adapter = moshi.adapter(QuotesData::class.java)
            quotesData = adapter.fromJson(jsonString)

            if (quotesData != null) {
                isInitialized = true
                Result.success(Unit)
            } else {
                Result.failure(Exception("Les données des citations sont nulles après le parsing."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Construit une phrase en assemblant aléatoirement une intro, un milieu et une fin
     * pour une catégorie donnée, avec un fallback sur la catégorie par défaut.
     */
    fun getRandomQuoteByCategory(category: QuoteCategory): String {
        var parts = getQuoteParts(category)

        // Si la catégorie demandée est vide (ou n'existe pas), on se rabat sur la catégorie par défaut.
        if (parts == null || parts.intros.isEmpty() || parts.middles.isEmpty() || parts.endings.isEmpty()) {
            parts = getQuoteParts(QuoteCategory.DEFAULT)
        }

        // Si même la catégorie par défaut est invalide, on retourne une phrase de secours.
        return if (parts != null && parts.intros.isNotEmpty() && parts.middles.isNotEmpty() && parts.endings.isNotEmpty()) {
            val intro = parts.intros.random()
            val middle = parts.middles.random()
            val ending = parts.endings.random()
            "$intro $middle $ending"
        } else {
            "Commande quelque chose, tu as faim !" // Ultime fallback
        }
    }

    /**
     * Récupère l'objet QuoteParts pour une catégorie spécifique.
     */
    private fun getQuoteParts(category: QuoteCategory): QuoteParts? {
        return when (category) {
            QuoteCategory.DEFAULT -> quotesData?.default
            QuoteCategory.RAIN -> quotesData?.rain
            QuoteCategory.COLD -> quotesData?.cold
            QuoteCategory.HEAT -> quotesData?.heat
        }
    }
}
