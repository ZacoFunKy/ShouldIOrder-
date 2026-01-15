package com.example.shouldiorder.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shouldiorder.FoodItem
import com.example.shouldiorder.FoodSlotMachineViewModel
import kotlinx.coroutines.launch

@Composable
fun FoodSlotMachineDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val viewModel: FoodSlotMachineViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val foodItems by viewModel.foodItems.collectAsState()
    val reel1Target by viewModel.reel1TargetIndex.collectAsState()
    val reel2Target by viewModel.reel2TargetIndex.collectAsState()
    val reel3Target by viewModel.reel3TargetIndex.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val winningFood by viewModel.winningFood.collectAsState()
    val isSaladRejected by viewModel.isSaladRejected.collectAsState()

    // Correction : On mémorise le dernier résultat valide pour l'afficher pendant l'animation de sortie.
    var itemToDisplay by remember { mutableStateOf<FoodItem?>(null) }
    if (winningFood != null) {
        itemToDisplay = winningFood
    }

    var saladRejectToDisplay by remember { mutableStateOf(false) }
    if (isSaladRejected) {
        saladRejectToDisplay = true
    }
    if (isSpinning) {
        saladRejectToDisplay = false
    }

    Dialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(420.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LA ROUE DU GRAS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SlotReel(modifier = Modifier.weight(1f), foods = foodItems, targetIndex = reel1Target)
                        Spacer(modifier = Modifier.width(2.dp).height(120.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        SlotReel(modifier = Modifier.weight(1f), foods = foodItems, targetIndex = reel2Target)
                        Spacer(modifier = Modifier.width(2.dp).height(120.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        SlotReel(modifier = Modifier.weight(1f), foods = foodItems, targetIndex = reel3Target)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(70.dp).align(Alignment.TopCenter).background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color.Transparent))))
                    Box(modifier = Modifier.fillMaxWidth().height(70.dp).align(Alignment.BottomCenter).background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black))))
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = winningFood != null && !isSpinning,
                    enter = fadeIn(animationSpec = tween(delayMillis = 200)) + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    // On utilise la valeur mémorisée pour éviter le "null null"
                    itemToDisplay?.let { food ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (saladRejectToDisplay) "SALADE REFUSÉE !" else "C'est parti pour :",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (saladRejectToDisplay) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${food.emoji} ${food.name} ! 🎉",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.spin(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSpinning && foodItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = if (isSpinning) "..." else "SPIN!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun SlotReel(
    modifier: Modifier = Modifier,
    foods: List<FoodItem>,
    targetIndex: Int
) {
    val listState = rememberLazyListState()

    LaunchedEffect(targetIndex) {
        if (foods.isNotEmpty()) {
            try {
                launch {
                    listState.animateScrollToItem(targetIndex)
                }
            } catch (e: Exception) {
                Log.e("SlotReel", "Crash évité lors de l'animation de défilement.", e)
            }
        }
    }

    val verticalPadding = 40.dp

    LazyColumn(
        modifier = modifier.height(180.dp),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = verticalPadding)
    ) {
        if (foods.isNotEmpty()) {
            items(Int.MAX_VALUE) {
                val food = foods[it % foods.size]
                Text(text = food.emoji, fontSize = 64.sp, modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}
