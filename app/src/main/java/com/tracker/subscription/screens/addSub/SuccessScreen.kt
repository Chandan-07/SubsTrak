package com.tracker.subscription.screens.addSub

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.play.core.review.ReviewManagerFactory
import com.tracker.subscription.R
import com.tracker.subscription.ui.theme.ThemeColors
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    navController: NavController,
    isDarkTheme: Boolean
) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.success)
    )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_medium) )


    val context = LocalContext.current
    val activity = context as Activity
    LaunchedEffect(Unit) {

        delay(1500)
        navController.navigate("dashboard") {

            popUpTo("dashboard") {
                inclusive = true
            }

            launchSingleTop = true
        }
        launchReviewFlow(activity)
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Subscription Added Successfully",
            fontSize = 22.sp,
            fontFamily = manropeBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your subscription is now being tracked.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontFamily = manropeRegular
        )

//        Spacer(modifier = Modifier.height(40.dp))
//
//        Box(
//            modifier = Modifier.clip(RoundedCornerShape(25.dp)).background(color = colorResource(R.color.blue))
//                .fillMaxWidth().height(65.dp).clickable {
//
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Done", fontFamily = manropeExtraBold, fontSize = 20.sp,color = Color(0xFFFFFFFF),)
//        }


    }
}

fun launchInAppReview(context: Context) {

    val manager = ReviewManagerFactory.create(context)

    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->

        if (task.isSuccessful) {

            val reviewInfo = task.result

            manager.launchReviewFlow(
                context as Activity,
                reviewInfo
            )
        }
    }
}

fun launchReviewFlow(
    activity: Activity
) {

    val manager = ReviewManagerFactory.create(activity)

    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->

        if (task.isSuccessful) {

            val reviewInfo = task.result

            manager.launchReviewFlow(
                activity,
                reviewInfo
            )

        } else {

            Toast.makeText(
                activity,
                "Review flow unavailable",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    onRateClick: () -> Unit,
    onDismiss: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Enjoying the app?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your support helps us improve and build more useful features.",
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row {

                repeat(5) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRateClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Rate Us")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDismiss
            ) {
                Text("Maybe Later")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}