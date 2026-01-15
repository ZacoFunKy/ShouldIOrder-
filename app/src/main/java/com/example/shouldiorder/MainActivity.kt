package com.example.shouldiorder

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.shouldiorder.ui.components.FoodSlotMachineDialog
import com.example.shouldiorder.ui.components.LocationPermissionHandler
import com.example.shouldiorder.ui.state.QuoteUiState
import com.example.shouldiorder.ui.theme.AppConstants
import com.example.shouldiorder.ui.theme.ShouldIOrderTheme
import com.example.shouldiorder.utils.DeliveryUtils
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }

        installSplashScreen().setKeepOnScreenCondition {
            !viewModel.isReady.value
        }

        enableEdgeToEdge()
        setContent {
            ShouldIOrderTheme {
                ShouldIOrderApp(viewModel)
            }
        }
    }
}

@Composable
fun ShouldIOrderApp(viewModel: MainViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initializeData()
    }

    val uiState by viewModel.uiState.collectAsState()
    val konfettiParty by viewModel.konfettiParty.collectAsState()

    var showFoodSlotMachine by rememberSaveable { mutableStateOf(false) }

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(AppConstants.Colors.GradientStart, AppConstants.Colors.GradientEnd),
        start = Offset.Zero,
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
            .systemBarsPadding()
    ) {
        konfettiParty?.let {
            KonfettiView(modifier = Modifier.fillMaxSize(), parties = listOf(it))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppConstants.Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderLogo()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                QuoteCard(uiState = uiState)

                LocationPermissionHandler { hasPermission ->
                    viewModel.onPermissionResult(hasPermission)
                }
            }

            FooterActions(
                context = context,
                viewModel = viewModel,
                uiState = uiState,
                onGoToFoodSlotMachine = { showFoodSlotMachine = true }
            )
        }

        // Affichage discret du numéro de version
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )

        FoodSlotMachineDialog(
            isVisible = showFoodSlotMachine,
            onDismiss = { showFoodSlotMachine = false }
        )
    }
}

@Composable
private fun HeaderLogo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppConstants.Dimensions.PaddingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-28).dp)
    ) {
        val fontFamily = FontFamily.Cursive
        val shadowStyle = TextStyle(
            shadow = Shadow(color = Color(0xFF3E2723).copy(alpha = 0.3f), offset = Offset(2f, 4f), blurRadius = 8f)
        )

        Text(
            text = stringResource(id = R.string.logo_should_i),
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = fontFamily,
            color = AppConstants.Colors.OrangePrimary,
            textAlign = TextAlign.Center,
            style = shadowStyle,
            lineHeight = 36.sp
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(id = R.string.logo_order),
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = fontFamily,
                color = AppConstants.Colors.OrangeSecondary,
                textAlign = TextAlign.Center,
                style = shadowStyle,
                lineHeight = 36.sp,
                modifier = Modifier.rotate(-3f)
            )
            Text(
                text = stringResource(id = R.string.logo_question_mark),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                fontFamily = fontFamily,
                color = AppConstants.Colors.OrangeSecondary,
                textAlign = TextAlign.Center,
                style = shadowStyle,
                lineHeight = 36.sp,
                modifier = Modifier
                    .padding(start = AppConstants.Dimensions.PaddingSmall, bottom = AppConstants.Dimensions.PaddingXSmall)
                    .rotate(5f)
            )
        }
    }
}

@Composable
private fun QuoteCard(uiState: QuoteUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.CardHeight),
        shape = RoundedCornerShape(AppConstants.Dimensions.CornerRadiusXLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = AppConstants.Dimensions.CardElevation),
        colors = CardDefaults.cardColors(
            containerColor = AppConstants.Colors.CardBackground,
            contentColor = AppConstants.Colors.CardText
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppConstants.Dimensions.PaddingHuge),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is QuoteUiState.Loading -> {
                    CircularProgressIndicator(color = AppConstants.Colors.OrangePrimary)
                }
                is QuoteUiState.Success, is QuoteUiState.Error -> {
                    AnimatedContent(
                        targetState = uiState.getDisplayText(),
                        transitionSpec = {
                            (slideInVertically { -40 } + scaleIn(initialScale = 0.8f) + fadeIn()) togetherWith
                                (slideOutVertically { 40 } + fadeOut())
                        },
                        label = "quotePopAnimation"
                    ) { quote ->
                        AdaptiveQuoteText(quote = quote)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveQuoteText(quote: String) {
    val fontSize = when {
        quote.length > 100 -> 18.sp
        quote.length > 80 -> 20.sp
        else -> AppConstants.Typography.QuoteTextSize
    }

    Text(
        text = quote,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = AppConstants.Colors.OrangeSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 32.sp,
        modifier = Modifier.padding(AppConstants.Dimensions.PaddingMedium)
    )
}

@Composable
private fun FooterActions(
    context: Context,
    viewModel: MainViewModel,
    uiState: QuoteUiState,
    onGoToFoodSlotMachine: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppConstants.Dimensions.PaddingXLarge),
        verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.PaddingMedium)
    ) {
        PrimaryActionButton(
            viewModel = viewModel,
            uiState = uiState
        )
        SecondaryActionsPanel(
            context = context,
            uiState = uiState,
            onGoToFoodSlotMachine = onGoToFoodSlotMachine
        )
    }
}

@Composable
private fun PrimaryActionButton(
    viewModel: MainViewModel,
    uiState: QuoteUiState
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScaleAnimation"
    )

    Button(
        onClick = { viewModel.getRandomReason() },
        modifier = Modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.ButtonHeight)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppConstants.Colors.ButtonPrimary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(AppConstants.Dimensions.CornerRadiusMedium),
        interactionSource = interactionSource,
        enabled = uiState !is QuoteUiState.Loading
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = AppConstants.Dimensions.PaddingMedium)
        )
        Text(
            text = stringResource(id = R.string.main_button_text),
            fontSize = AppConstants.Typography.ButtonTextSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecondaryActionsPanel(
    context: Context,
    uiState: QuoteUiState,
    onGoToFoodSlotMachine: () -> Unit
) {
    val initialQuote = stringResource(id = R.string.initial_quote)
    AnimatedVisibility(
        visible = uiState is QuoteUiState.Success && uiState.quote != initialQuote,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.PaddingMedium)
        ) {
            SecondaryButton(
                label = stringResource(id = R.string.secondary_button_find_food),
                backgroundColor = Color(0xFFFF6F00),
                onClick = onGoToFoodSlotMachine
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE0E0E0)))
            SecondaryButton(
                label = stringResource(id = R.string.secondary_button_uber_eats),
                backgroundColor = AppConstants.Colors.ButtonSecondary,
                onClick = { DeliveryUtils.openUberEats(context) }
            )
            SecondaryButton(
                label = stringResource(id = R.string.secondary_button_deliveroo),
                backgroundColor = AppConstants.Colors.ButtonTertiary,
                onClick = { DeliveryUtils.openDeliveroo(context) }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppConstants.Dimensions.PaddingMedium)
            ) {
                SecondaryButton(
                    label = stringResource(id = R.string.secondary_button_share),
                    backgroundColor = Color(0xFFE0E0E0),
                    textColor = AppConstants.Colors.CardText,
                    onClick = { (uiState as? QuoteUiState.Success)?.quote?.let { DeliveryUtils.shareReason(context, it) } },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Share
                )
                SecondaryButton(
                    label = stringResource(id = R.string.secondary_button_copy),
                    backgroundColor = Color(0xFFE0E0E0),
                    textColor = AppConstants.Colors.CardText,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        (uiState as? QuoteUiState.Success)?.quote?.let {
                            val clip = android.content.ClipData.newPlainText("reason", it)
                            clipboard.setPrimaryClip(clip)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(AppConstants.Dimensions.ButtonHeightSmall),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = textColor),
        shape = RoundedCornerShape(AppConstants.Dimensions.CornerRadiusMedium)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(end = AppConstants.Dimensions.PaddingSmall)
            )
        }
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
