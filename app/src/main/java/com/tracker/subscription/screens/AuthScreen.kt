package com.tracker.subscription.screens
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R
import com.tracker.subscription.auth.GoogleAuthHelper
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {


    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFFFFF),   // deep blue
                        Color(0xFFFFFFFF),   // deep blue
                    )
                )
            )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 50.dp)
            ) {

                Icon(painter = painterResource(R.drawable.header_title), contentDescription = "",
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "SubTracker",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.black),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Track, Manage, and Save all your subscriptions in one place",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.black),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 40.dp, end = 40.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))


            }
            Spacer(modifier = Modifier.height(80.dp))

            Column {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 30.dp)
                        .height(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1525A8), // blue
                                    Color(0xFFEADEDE)  // light blue
                                )
                            ),
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
                        Row {
                            Icon(painterResource(R.drawable.google), contentDescription = "",  modifier = Modifier.size(25.dp))
                            Spacer(Modifier.width(20.dp))
                            Text("Continue with Google",fontSize = 18.sp, color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }

                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Skip for now",color = colorResource(R.color.dark_blue), fontSize = 16.sp)
                }
            }
        }
    }

}