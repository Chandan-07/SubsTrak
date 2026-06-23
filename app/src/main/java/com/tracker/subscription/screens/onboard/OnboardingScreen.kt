package com.tracker.subscription.screens.onboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tracker.subscription.R
import com.tracker.subscription.data.db.OnboardingPreference
import com.tracker.subscription.ui.data.pages
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onGetStarted: () -> Unit
) {
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val playFairBold = FontFamily( Font(R.font.playfair_display_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.white)).padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->

            val item = pages[page]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                Image(
                    painter = painterResource(item.image),
                    modifier = Modifier.size(256.dp),
                    contentDescription = null
                )

                Text(
                    text = item.title,
                    fontSize = 34.sp,
                    fontFamily = playFairBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 60.dp, start = 27.dp, end = 27.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = item.description,
                    fontSize = 16.sp,
                    fontFamily = manropeMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        PagerIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF558B2F), // blue
                            Color(0xFFFFFF00)  // light blue
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(10.dp),
                onClick = {

                    if (pagerState.currentPage == pages.lastIndex) {
                        scope.launch {
                            OnboardingPreference.setCompleted(context)
                        }
                        onGetStarted()
                    } else {
                        // Ask notification permission when the user leaves slide 2 (index 1)
                        if (pagerState.currentPage == 1) {
                            pendingAdvanceFromSecondSlide = true
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1
                                )
                            }
                        }
                    }
                }
            ) {

                Text(
                    text = if (pagerState.currentPage == pages.lastIndex)
                        "Get Started"
                    else
                        "Next",
                    color = Color.White,
                    fontFamily = manropeBold,
                    fontSize = 20.sp
                )
            }
        }

    }
}

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {

        repeat(pageCount) { index ->

            val color =
                if (index == currentPage)
                    colorResource(R.color.lime)
                else
                    Color.LightGray

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (index == currentPage) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


