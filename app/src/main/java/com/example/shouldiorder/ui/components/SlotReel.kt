package com.example.shouldiorder.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shouldiorder.FoodItem
import kotlinx.coroutines.delay

/**
 * Un rouleau individuel de la machine à sous
 * Design épuré : emoji uniquement, hauteur fixe pour alignement pixel-perfect
 */
@Composable
fun SlotReel(
    foods: List<FoodItem>,
    targetIndex: Int,
    isSpinning: Boolean,
    delayMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    // Créer une liste infinie (répétition pour effet infini)
    val infiniteList = List(1000) { index -> foods[index % foods.size] }

    // État du scroll
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    // Animation de spin
    LaunchedEffect(targetIndex, isSpinning) {
        if (isSpinning && targetIndex > 0) {
            // Reset à la position initiale
            listState.scrollToItem(index = 0)
            delay(50)

            // Délai pour le stagger effect
            if (delayMs > 0) {
                delay(delayMs)
            }

            // Animer le scroll vers l'index cible
            // L'item centré sera targetIndex % foods.size
            listState.animateScrollToItem(
                index = targetIndex,
                scrollOffset = 0
            )
        }
    }

    Box(
        modifier = modifier
            .height(80.dp)  // HAUTEUR EXACTE pour afficher 1 ligne
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // LazyColumn avec scroll désactivé
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            userScrollEnabled = false
        ) {
            items(infiniteList.size) { index ->
                val food = infiniteList[index]

                // HAUTEUR FIXE OBLIGATOIRE : 80dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),  // HAUTEUR FIXE
                    contentAlignment = Alignment.Center
                ) {
                    // EMOJI UNIQUEMENT (pas de texte)
                    Text(
                        text = food.emoji,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay dégradé gauche (effet de profondeur)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .height(80.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            Color.Transparent
                        )
                    )
                )
        )

        // Overlay dégradé droit (effet de profondeur)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .height(80.dp)
                .align(Alignment.CenterEnd)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
        )
    }
}



