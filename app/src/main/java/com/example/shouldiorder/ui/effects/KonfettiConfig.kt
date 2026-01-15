package com.example.shouldiorder.ui.effects

import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Configuration des effets Konfetti pour les explosions
 * Explosion aggressive et visible
 */
object KonfettiConfig {

    /**
     * Crée une explosion de confettis depuis le bas de l'écran
     * Effet "Blast" premium avec particules qui montent puis retombent
     */
    fun createBlastParty(): Party {
        return Party(
            angle = Angle.TOP,
            spread = Spread.WIDE,
            speed = 40f,  // Vitesse augmentée pour meilleure visibilité
            damping = 0.9f,
            timeToLive = 3500L,  // Durée augmentée
            emitter = Emitter(
                duration = 800L,  // Durée d'émission augmentée
                TimeUnit.MILLISECONDS
            ).max(300),  // Plus de particules
            colors = intArrayOf(
                0xFFFF6F00.toInt(),  // Orange vif
                0xFFFFB74D.toInt(),  // Orange clair
                0xFFFFC107.toInt(),  // Jaune doré
                0xFFFF9800.toInt(),  // Orange moyen
                0xFFE65100.toInt(),  // Orange sombre
                0xFFFF7043.toInt(),  // Rouge-orange
                0xFF2196F3.toInt(),  // Bleu (contraste)
                0xFF4CAF50.toInt()   // Vert (contraste)
            ).toList()
        )
    }
}

