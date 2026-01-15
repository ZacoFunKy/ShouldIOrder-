package com.example.shouldiorder.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Constantes de design et couleurs pour l'application
 */
object AppConstants {
    // Couleurs - Palette Orange/Jaune
    object Colors {
        val OrangePrimary = Color(0xFFE65100)
        val OrangeSecondary = Color(0xFFFF6F00)
        val OrangeTertiary = Color(0xFFFFB74D)
        val GradientStart = Color(0xFFFFB74D)      // Orange doux
        val GradientEnd = Color(0xFFFFF8E1)        // Jaune crème
        val CardBackground = Color.White
        val CardText = Color(0xFF3E2723)
        val ButtonPrimary = Color(0xFFFF6F00)
        val ButtonSecondary = Color(0xFF000000)
        val ButtonTertiary = Color(0xFF00CCBB)
    }

    // Typographie
    object Typography {
        val TitleSize = 56.sp
        val TitleSecondarySize = 48.sp
        val QuestionMarkSize = 72.sp
        val QuoteTextSize = 22.sp
        val ButtonTextSize = 18.sp
    }

    // Dimensions
    object Dimensions {
        val PaddingXSmall = 4.dp
        val PaddingSmall = 8.dp
        val PaddingMedium = 12.dp
        val PaddingLarge = 16.dp
        val PaddingXLarge = 20.dp
        val PaddingHuge = 24.dp
        val PaddingExtra = 40.dp

        val CornerRadiusSmall = 12.dp
        val CornerRadiusMedium = 16.dp
        val CornerRadiusLarge = 24.dp
        val CornerRadiusXLarge = 32.dp

        val ButtonHeight = 60.dp
        val ButtonHeightSmall = 50.dp
        val CardHeight = 200.dp
        val CardElevation = 16.dp
    }

    // Animations
    object Animations {
        val DebounceClickDuration = 300L       // Délai anti-spam
        val AnimationDuration = 600             // Durée animation texte
    }

    // Confettis
    object Confetti {
        val Count = 26
        val DelayBetween = 25L
        val DurationMin = 4500
        val DurationMax = 5700
        val FadeOutDuration = 400
    }
}

