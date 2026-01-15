package com.example.shouldiorder.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shouldiorder.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Composable pour demander la permission de localisation
 * Retourne true si la permission est accordée
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit
): Boolean {
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    LaunchedEffect(locationPermissionState.status) {
        onPermissionResult(locationPermissionState.status.isGranted)
    }

    // Request permission if it's not granted and we don't need to show rationale.
    // This happens on first launch.
    LaunchedEffect(locationPermissionState) {
        if (!locationPermissionState.status.isGranted && !locationPermissionState.status.shouldShowRationale) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    if (locationPermissionState.status.shouldShowRationale) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.permission_rationale),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text(stringResource(id = R.string.permission_button_text))
            }
        }
    }

    return locationPermissionState.status.isGranted
}


/**
 * Composable pour demander la permission et afficher un indicateur visuel discret
 * Montre une petite icône 🌍 si la météo est utilisée
 */
@Composable
fun WeatherIndicator(isWeatherBased: Boolean) {
    if (isWeatherBased) {
        Text(
            text = "🌍",
            fontSize = 16.sp
        )
    }
}