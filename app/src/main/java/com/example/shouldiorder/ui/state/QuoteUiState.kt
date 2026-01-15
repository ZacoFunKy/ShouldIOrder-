package com.example.shouldiorder.ui.state

/**
 * Représente l'état de l'UI
 * Utilisé pour gérer les cas Loading, Success et Error
 */
sealed class QuoteUiState {
    data object Loading : QuoteUiState()
    data class Success(val quote: String) : QuoteUiState()
    data class Error(val message: String) : QuoteUiState()

    fun getDisplayText(): String = when (this) {
        is Loading -> "On a faim ?"
        is Success -> quote
        is Error -> "Parce que tu as faim.\nComme d'habitude."
    }
}

