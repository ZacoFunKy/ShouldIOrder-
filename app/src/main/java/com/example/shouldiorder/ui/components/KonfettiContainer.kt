package com.example.shouldiorder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party

/**
 * Wrapper autour de KonfettiView pour simplifier l'utilisation
 */
@Composable
fun KonfettiContainer(
    modifier: Modifier = Modifier,
    parties: List<Party> = emptyList()
) {
    KonfettiView(
        modifier = modifier,
        parties = parties
    )
}

