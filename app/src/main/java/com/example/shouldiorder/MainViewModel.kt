package com.example.shouldiorder

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shouldiorder.data.QuoteCategory
import com.example.shouldiorder.data.QuotesProvider
import com.example.shouldiorder.data.location.LocationProvider
import com.example.shouldiorder.data.weather.WeatherProvider
import com.example.shouldiorder.updater.UpdateManager
import com.example.shouldiorder.ui.effects.KonfettiConfig
import com.example.shouldiorder.ui.state.QuoteUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import nl.dionsegijn.konfetti.core.Party
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(private val context: Context) : ViewModel() {

    private val weatherProvider = WeatherProvider(context)
    private val locationProvider = LocationProvider(context)
    private val updateManager = UpdateManager(context)
    private var lastReason = ""

    private val _uiState = MutableStateFlow<QuoteUiState>(QuoteUiState.Loading)
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _konfettiParty = MutableStateFlow<Party?>(null)
    val konfettiParty: StateFlow<Party?> = _konfettiParty.asStateFlow()

    private val _isWeatherBased = MutableStateFlow(false)
    val isWeatherBased: StateFlow<Boolean> = _isWeatherBased.asStateFlow()

    private val isAnimating = AtomicBoolean(false)
    private var hasLocationPermission = false

    fun initializeData() {
        viewModelScope.launch {
            _isReady.value = false
            try {
                withContext(Dispatchers.IO) { QuotesProvider.initialize(context) }
                _uiState.value = QuoteUiState.Success(context.getString(R.string.initial_quote))
            } catch (e: Exception) {
                _uiState.value = QuoteUiState.Error(context.getString(R.string.error_loading_quotes))
            }
            _isReady.value = true

            // Lance la vérification de mise à jour en arrière-plan, après un délai.
            checkForUpdates()
        }
    }

    private fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000) // Laisse le temps à l'UI de se stabiliser
            try {
                updateManager.checkForUpdate()
            } catch (e: Exception) {
                Log.e("UpdateManager", "Échec de la vérification de mise à jour", e)
            }
        }
    }

    fun onPermissionResult(hasPermission: Boolean) {
        this.hasLocationPermission = hasPermission
    }

    fun getRandomReason() {
        if (!isAnimating.compareAndSet(false, true)) return

        viewModelScope.launch {
            var category = QuoteCategory.DEFAULT
            var isFromWeather = false

            if (hasLocationPermission) {
                try {
                    withContext(Dispatchers.IO) {
                        withTimeout(2000L) {
                            locationProvider.getLastLocation()?.let { (lat, lon) ->
                                weatherProvider.getWeatherCategory(lat, lon)?.let {
                                    category = it
                                    isFromWeather = true
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignoré volontairement
                }
            }

            val newReason = QuotesProvider.getRandomQuoteByCategory(category)
            _uiState.value = QuoteUiState.Success(newReason)
            lastReason = newReason
            _isWeatherBased.value = isFromWeather

            _konfettiParty.value = KonfettiConfig.createBlastParty()

            delay(300)
            isAnimating.set(false)

            delay(2500)
            _konfettiParty.value = null
        }
    }
}
