package com.example.shouldiorder.data

import com.squareup.moshi.JsonClass

/**
 * Représente la structure complète du fichier quotes.json.
 * Contient des catégories pour chaque condition météo.
 */
@JsonClass(generateAdapter = true)
data class QuotesData(
    val default: QuoteParts,
    val rain: QuoteParts,
    val cold: QuoteParts,
    val heat: QuoteParts
)

/**
 * Contient les trois parties d'une phrase : intro, milieu, et fin.
 */
@JsonClass(generateAdapter = true)
data class QuoteParts(
    val intros: List<String>,
    val middles: List<String>,
    val endings: List<String>
)

/**
 * Enum pour les catégories de quotes, aligné sur le JSON.
 */
enum class QuoteCategory {
    DEFAULT,
    RAIN,
    COLD,
    HEAT
}
