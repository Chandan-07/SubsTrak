package com.tracker.subscription.screens.onboard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {

    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),   // deep blue

                    )
                )
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(painter = painterResource(R.drawable.header_title), contentDescription = "",
                    tint = Color.Unspecified, modifier = Modifier.clip(CircleShape).size(150.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sign in to Subtly",
                    color = colorResource(R.color.black),
                    fontSize = 20.sp,
                    fontFamily = manropeExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Continue with Google to get started",
                    color = colorResource(R.color.text_grey),
                    fontSize = 14.sp,
                    fontFamily = manropeMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp)
                )


            }
            Spacer(modifier = Modifier.height(80.dp))

            Column {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 30.dp)
                        .height(56.dp)
                        .background(colorResource(R.color.dark_blue),
                            shape = RoundedCornerShape(25.dp)
                        )
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(5.dp),
                        onClick = onGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                        )}else {
                                Row {
                                    Icon(
                                        painterResource(R.drawable.google),
                                        contentDescription = "",
                                        modifier = Modifier.size(25.dp)
                                    )
                                    Spacer(Modifier.width(20.dp))
                                    Text(
                                        "Continue with Google",
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        fontFamily = manropeBold
                                    )
                                }
                            }

                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Skip for now",color = colorResource(R.color.dark_blue),
                        fontFamily = manropeBold, fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }

            }
        }
    }

}