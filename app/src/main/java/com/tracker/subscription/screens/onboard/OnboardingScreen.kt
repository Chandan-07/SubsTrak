package com.tracker.subscription.screens.onboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tracker.subscription.R
import com.tracker.subscription.data.db.OnboardingPreference
import com.tracker.subscription.ui.data.pages
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.tracker.subscription.analytics.SubtlyAnalytics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onGetStarted: () -> Unit
) {
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val playFairBold = FontFamily( Font(R.font.playfair_display_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manrope_regular = FontFamily( Font(R.font.manrope_regular) )
    val elemesExtraBold = FontFamily( Font(R.font.elmes_sans_black) )
    val stackSansBold = FontFamily( Font(R.font.stack_sans_bold) )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        SubtlyAnalytics.logOnboardingStart()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) {
            SubtlyAnalytics.logOnboardingNext(pagerState.currentPage)
        }
    }

    var pendingAdvanceFromSecondSlide by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingAdvanceFromSecondSlide = false
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    LaunchedEffect(pendingAdvanceFromSecondSlide) {
        if (!pendingAdvanceFromSecondSlide) return@LaunchedEffect

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            pendingAdvanceFromSecondSlide = false
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
            return@LaunchedEffect
        }

        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            pendingAdvanceFromSecondSlide = false
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val pageColors = if (isDarkTheme) {
        listOf(
            Color(0xFF0F172A), // Page 1: Slate 900
            Color(0xFF1E1B4B), // Page 2: Indigo 950
            Color(0xFF121212)  // Page 3: Dark gray/black
        )
    } else {
        listOf(
            Color(0xFFF8FAFC), // Page 1: Clean soft Slate-white
            Color(0xFFF5F3FF), // Page 2: Soft Violet-white
            Color.White        // Page 3: Pure White
        )
    }

    val currentBgColor = remember(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        val page = pagerState.currentPage
        val fraction = pagerState.currentPageOffsetFraction
        val startColor = pageColors[page]
        val endColor = if (fraction > 0f) {
            pageColors.getOrElse(page + 1) { startColor }
        } else {
            pageColors.getOrElse(page - 1) { startColor }
        }
        androidx.compose.ui.graphics.lerp(startColor, endColor, fraction.absoluteValue.coerceIn(0f, 1f))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Top Header Bar: PagerIndicator on Left, Skip on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                currentPageOffsetFraction = pagerState.currentPageOffsetFraction
            )

            Text(
                text = "Skip",
                fontSize = 15.sp,
                fontFamily = manropeBold,
                color = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        SubtlyAnalytics.logOnboardingSkip()
                        scope.launch {
                            OnboardingPreference.setCompleted(context)
                        }
                        onGetStarted()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->

            val item = pages[page]
            val pageOffset = (page - pagerState.currentPage) - pagerState.currentPageOffsetFraction

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30.dp))

            if (page == 0) {
                FullOrbitSolarSystem(
                    item = item,
                    isDarkTheme = isDarkTheme,
                    pageOffset = pageOffset,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(30.dp))

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = pageOffset * size.width * 0.35f
                                val scale = 1f - (pageOffset.absoluteValue * 0.15f).coerceIn(0f, 0.15f)
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (page == 1) {
                            NotificationsLottie(
                                modifier = Modifier.size(256.dp)
                            )
                        } else if (page == 2) {
                            AnalyticsLottie(
                                modifier = Modifier.size(256.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(item.image),
                                modifier = Modifier.size(256.dp),
                                contentDescription = null
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                translationY = pageOffset.absoluteValue * 150f
                                alpha = (1f - pageOffset.absoluteValue * 1.8f).coerceIn(0f, 1f)
                                val scale = 0.92f + (1f - pageOffset.absoluteValue.coerceIn(0f, 1f)) * 0.08f
                                scaleX = scale
                                scaleY = scale
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 28.sp,
                            fontFamily = stackSansBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 42.sp,
                            color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.padding(top = 45.dp, start = 27.dp, end = 27.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = item.description,
                            fontSize = 13.sp,
                            fontFamily = manrope_regular,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
            }
        }

        val cardBgColor = if (isDarkTheme) Color(0xFF17171C) else Color(0xFFDFE3F3)
        val pillBgColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
        val pillTextColor = if (isDarkTheme) Color.Black else Color.White
        val arrowCircleBg = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1E293B)
        val arrowIconTint = if (isDarkTheme) Color.Black else Color.White
        val taglineTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

        // Bottom Premium Adaptive Card Container with Pill Action Button
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = cardBgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 45.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Adaptive Pill Action Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clickable {
                            if (pagerState.currentPage == pages.lastIndex) {
                                SubtlyAnalytics.logOnboardingComplete()
                                scope.launch {
                                    OnboardingPreference.setCompleted(context)
                                }
                                onGetStarted()
                            } else {
                                if (pagerState.currentPage == 1) {
                                    pendingAdvanceFromSecondSlide = true
                                } else {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            }
                        },
                    shape = CircleShape,
                    color = pillBgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.lastIndex)
                                "Let's Begin"
                            else
                                "Next Step",
                            color = pillTextColor,
                            fontFamily = manropeBold,
                            fontSize = 16.sp
                        )

                        // Circular Arrow Icon Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(arrowCircleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = arrowIconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Social Proof Tagline
                Text(
                    text = "Millions of subscriptions optimized worldwide.",
                    fontSize = 12.sp,
                    fontFamily = manropeMedium,
                    color = taglineTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(pageCount) { index ->
            // Calculate selection progress for this dot
            val progress = when {
                index == currentPage -> 1f - currentPageOffsetFraction.absoluteValue
                index == currentPage + 1 && currentPageOffsetFraction > 0 -> currentPageOffsetFraction
                index == currentPage - 1 && currentPageOffsetFraction < 0 -> -currentPageOffsetFraction
                else -> 0f
            }.coerceIn(0f, 1f)

            val color = androidx.compose.ui.graphics.lerp(
                Color.LightGray.copy(alpha = 0.5f),
                Color(0xFF2563EB),
                progress
            )

            val dotWidth = 8.dp + (16.dp * progress)

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = dotWidth, height = 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private data class SpaciousOrbitRing(
    val icons: List<Int>,
    val radiusX: Float,
    val radiusY: Float,
    val iconSize: Dp,
    val speedMultiplier: Float,
    val baseAlpha: Float
)

@Composable
fun FullOrbitSolarSystem(
    item: com.tracker.subscription.ui.data.OnboardingPage,
    isDarkTheme: Boolean,
    pageOffset: Float,
    modifier: Modifier = Modifier
) {
    val playFairBold = remember { FontFamily(Font(R.font.playfair_display_bold)) }
    val manropeMedium = remember { FontFamily(Font(R.font.manrope_medium)) }
    val density = LocalDensity.current.density

//    // 1. Continuous Orbital Angle Rotation (0 to 2*PI, ultra-slow 88s cycle)
//    val infiniteTransition = rememberInfiniteTransition(label = "FullOrbit")
//    val orbitAngle by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = (2 * Math.PI).toFloat(),
//        animationSpec = infiniteRepeatable(
//            animation = tween(88000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "Angle"
//    )

    // Entrance spring scale
    val entranceScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Touch gesture offset relative to center of box in DP
    var touchPoint by remember { mutableStateOf<Offset?>(null) }

    // Touch magnetic pull spring multiplier
    val touchAnim = remember { Animatable(0f) }
    LaunchedEffect(touchPoint != null) {
        if (touchPoint != null) {
            touchAnim.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            )
        } else {
            touchAnim.animateTo(
                0f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
    }

    // 3 Spacious Concentric Orbital Rings with uniform small icon sizes (28.dp)
    val rings = remember {
        listOf(
            SpaciousOrbitRing(
                icons = listOf(
                    R.drawable.spotify,
                    R.drawable.youtube,
                    R.drawable.prime,
                    R.drawable.notion,
                    R.drawable.google,
                    R.drawable.claude
                ),
                radiusX = 205f,
                radiusY = 150f,
                iconSize = 28.dp,
                speedMultiplier = 1.0f,
                baseAlpha = 0.42f
            ),
            SpaciousOrbitRing(
                icons = listOf(
                    R.drawable.chatgpt,
                    R.drawable.apple_tv,
                    R.drawable.jiohotstar,
                    R.drawable.canva,
                    R.drawable.duolingo,
                    R.drawable.midjourney,
                    R.drawable.photoshop,
                    R.drawable.linkedin
                ),
                radiusX = 265f,
                radiusY = 215f,
                iconSize = 28.dp,
                speedMultiplier = -0.6f, // Counter-clockwise
                baseAlpha = 0.30f
            ),
            SpaciousOrbitRing(
                icons = listOf(
                    R.drawable.perplexity,
                    R.drawable.grammarly,
                    R.drawable.cursor,
                    R.drawable.capcut,
                    R.drawable.coursera,
                    R.drawable.udemy,
                    R.drawable.dropbox,
                    R.drawable.instagram,
                    R.drawable.tinder,
                    R.drawable.googlegemini
                ),
                radiusX = 325f,
                radiusY = 280f,
                iconSize = 28.dp,
                speedMultiplier = 0.4f,
                baseAlpha = 0.20f
            )
        )
    }

    // Infinite gentle hover transition for the hero card
    val infiniteTransition = rememberInfiniteTransition(label = "heroHover")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hoverY"
    )

    val stackSansBold = FontFamily(Font(R.font.stack_sans_bold))
    val manrope_regular = FontFamily(Font(R.font.manrope_regular))

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = pageOffset * size.width * 0.35f
                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            },
        contentAlignment = Alignment.Center
    ) {
        // 🔮 Ambient Backdrop Circles (Reference screenshot style)
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-140).dp)
                .size(280.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color.White.copy(alpha = 0.04f) else Color(0xFF2563EB).copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 60.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color(0xFF3B82F6).copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⬛ Elevated Dark Squircle Hero Card Container (Reference screenshot style)
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = hoverY.dp.toPx() }
                    .size(175.dp)
                    .clip(RoundedCornerShape(38.dp))
                    .background(if (isDarkTheme) Color(0xFF16161A) else Color(0xFF0F172A))
                    .border(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(38.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.header_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(105.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(35.dp))

            // 📝 Clean Center Title + Subtitle Text (100% identical styling to slides 2 & 3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.title,
                    fontSize = 28.sp,
                    fontFamily = stackSansBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.padding(start = 27.dp, end = 27.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    fontFamily = manrope_regular,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
fun NotificationsLottie(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.notifications))
    val lottieState = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { lottieState.progress },
        modifier = modifier
    )
}

@Composable
fun AnalyticsLottie(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.analytics))
    val lottieState = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { lottieState.progress },
        modifier = modifier
    )
}


