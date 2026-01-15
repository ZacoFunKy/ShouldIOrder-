package com.example.shouldiorder

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shouldiorder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodSlotMachineViewModel : ViewModel() {

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _reel1TargetIndex = MutableStateFlow(0)
    val reel1TargetIndex: StateFlow<Int> = _reel1TargetIndex.asStateFlow()

    private val _reel2TargetIndex = MutableStateFlow(0)
    val reel2TargetIndex: StateFlow<Int> = _reel2TargetIndex.asStateFlow()

    private val _reel3TargetIndex = MutableStateFlow(0)
    val reel3TargetIndex: StateFlow<Int> = _reel3TargetIndex.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _winningFood = MutableStateFlow<FoodItem?>(null)
    val winningFood: StateFlow<FoodItem?> = _winningFood.asStateFlow()

    private val _isSaladRejected = MutableStateFlow(false)
    val isSaladRejected: StateFlow<Boolean> = _isSaladRejected.asStateFlow()

    fun initialize(context: Context) {
        if (_foodItems.value.isNotEmpty()) return
        // Ordre important pour le "bump" de la salade vers le burger
        _foodItems.value = listOf(
            FoodItem("🍔", context.getString(R.string.food_burger)),
            FoodItem("🍕", context.getString(R.string.food_pizza)),
            FoodItem("🍣", context.getString(R.string.food_sushi)),
            FoodItem("🌮", context.getString(R.string.food_tacos)),
            FoodItem("🥗", context.getString(R.string.food_salad)) // La salade est en dernier
        )
    }

    fun spin(context: Context) {
        val items = _foodItems.value
        if (items.isEmpty() || _isSpinning.value) return

        viewModelScope.launch {
            _isSpinning.value = true
            _winningFood.value = null
            _isSaladRejected.value = false

            val targetFood = items.random()
            val targetFoodIndex = items.indexOf(targetFood)
            val isSalad = targetFood.name == context.getString(R.string.food_salad)

            val baseSpins = 50
            _reel1TargetIndex.value = (baseSpins * items.size) + targetFoodIndex
            _reel2TargetIndex.value = (baseSpins * items.size) + targetFoodIndex
            _reel3TargetIndex.value = (baseSpins * items.size) + targetFoodIndex

            delay(2000) // Laisse le temps à l'animation principale de se terminer

            if (isSalad) {
                _winningFood.value = targetFood
                delay(500)

                playErrorFeedback(context)
                _isSaladRejected.value = true

                // Le "Bump" : on vise l'item suivant (le burger, car la liste est cyclique)
                val nextIndex = (targetFoodIndex + 1) % items.size
                val finalTarget = (baseSpins * items.size) + nextIndex

                // On ajoute 1 tour pour que le bump soit visible
                _reel1TargetIndex.value = finalTarget + items.size
                _reel2TargetIndex.value = finalTarget + items.size
                _reel3TargetIndex.value = finalTarget + items.size

                delay(300) // Animation du bump
                _winningFood.value = items[nextIndex]
            } else {
                _winningFood.value = targetFood
            }

            _isSpinning.value = false
        }
    }

    private suspend fun playErrorFeedback(context: Context) {
        try {
            // Son (si un fichier `cheat_sound.mp3` est ajouté dans `res/raw`)
            // MediaPlayer.create(context, R.raw.cheat_sound).start()

            // Vibration
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        } catch (e: Exception) {
            Log.e("SlotMachineFeedback", "Impossible de jouer le son ou de vibrer", e)
        }
    }
}
