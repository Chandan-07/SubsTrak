package com.tracker.subscription.screens.home.cards

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R
import com.tracker.subscription.Utility.formatCurrency
import com.tracker.subscription.Utility.remainingTimeText
import com.tracker.subscription.Utility.renewalColor
import com.tracker.subscription.Utility.renewalDateText
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.screens.addSub.formatDate
import com.tracker.subscription.screens.home.openSubscription

@Composable
fun RenewalItem(renewal: Renewal, context: Context, service: Service?,
                onEdit: (Renewal) -> Unit,
                onDelete: (Renewal) -> Unit) {
    val manropeSemiBold = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    var showDialog by remember { mutableStateOf(false) }
    var selectedSub by remember { mutableStateOf<Renewal?>(null) }

    val colors = listOf(
        Color(0xFF90CAF9),
        Color(0xFFA5D6A7),
        Color(0xFFFFCC80),
        Color(0xFFCE93D8),
        Color(0xFFFFAB91),
        Color(0xFF80DEEA),
        Color(0xFFE6EE9C)
    )

    val randomColor = colors.random()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(25.dp)

    ) {

        Column(modifier = Modifier
            .background(color = colorResource(R.color.white))
            .fillMaxWidth()
            .padding(start = 18.dp, top = 20.dp, bottom = 10.dp, end = 18.dp)

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                Row(verticalAlignment = Alignment.Top) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        if (service != null) {
                            Column {
                                Image(
                                    painter = painterResource(service.logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3F3F3))
                                    ,
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                            }

                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(randomColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = renewal.name.first().uppercase(),
                                    color = Color.White,
                                    fontFamily = manropeBold,
                                    fontSize = 30.sp
                                )
                            }

                        }

                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {

                        Row {
                            Text(
                                text = renewal.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 18.sp,
                                fontFamily = manropeExtraBold,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (renewal.subscriptionType == SubscriptionType.FREE_TRIAL.value){

                            Row (modifier = Modifier
                                .background(
                                    color = Color(0xFFF3DDB9), // green
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                            ){
                                Icon(painter = painterResource(R.drawable.timer),
                                    "", modifier = Modifier.size(12.dp).padding(start = 3.dp).align(
                                        Alignment.CenterVertically), tint = Color.Unspecified)
                                Text(
                                    text = "Free Trial",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontFamily = manropeBold,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ,
                                )
                            }

                        } else {

                            Row (modifier = Modifier
                                .background(
                                    color = Color(0xFF8BB755), // green
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                            ){
                                Icon(painter = painterResource(R.drawable.container__2_), "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(12.dp).padding(start = 2.dp, top = 2.dp, bottom = 2.dp).align(
                                        Alignment.CenterVertically))
                                Text(
                                    text = "Active",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = manropeBold,
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                    ,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatCurrency(renewal.price, renewal.currency), style = MaterialTheme.typography.bodyMedium, color = colorResource(
                            R.color.black), fontSize = 18.sp, fontFamily = manropeExtraBold)
                    Spacer(Modifier.height(30.dp))
                }


            }
            Spacer(modifier = Modifier.height(24.dp))
            Row( horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
                Text(
                    text = renewalDateText(renewal.nextBillingDate, renewal.subscriptionType),
                    color = colorResource(R.color.dark_grey),
                    fontSize = 12.sp,
                    fontFamily = manropeMedium,
                    textAlign = TextAlign.Left
                )
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = remainingTimeText(renewal.daysLeft, renewal.subscriptionType),
                    color = renewalColor(renewal.daysLeft),
                    fontSize = 12.sp,
                    fontFamily = manropeBold,
                    textAlign = TextAlign.Left

                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceAround) {

                Row(modifier = Modifier.weight(0.8f)) {

                    Row (modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .border(0.5.dp, colorResource(R.color.blue), CircleShape)
                        .background(
                            color = Color.White, // green
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable { onEdit(renewal) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                    ){
                        Text(
                            text = "Edit",
                            color = colorResource(R.color.blue),
                            fontSize = 11.sp,
                            fontFamily = manropeBold,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                            ,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Row (modifier = Modifier
                        .clip(RoundedCornerShape(15.dp))
                        .border(0.5.dp, Color(0xFFD50000), CircleShape)
                        .background(
                            color = Color.White, // green
                            shape = RoundedCornerShape(15.dp)
                        ).clickable {
                            selectedSub = renewal
                            showDialog = true
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                    ){
                        Text(
                            text = "Delete",
                            color = Color(0xFFD50000),
                            fontSize = 11.sp,
                            fontFamily = manropeBold,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                            ,
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.2f).height(10.dp))


                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A2AC0),
                                    Color(0xFF1A2AC0)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable {  openSubscription(context, renewal) }
                        .padding(horizontal = 8.dp, vertical = 7.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Take Action",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = manropeExtraBold
                        )
                        Spacer(Modifier.width(5.dp))
                        Icon(painterResource(R.drawable.gem), "", modifier = Modifier.size(12.dp), tint = Color.Unspecified)

                    }
                }

            }

        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text("Delete "+selectedSub?.name)
                },
                text = {
                    Text("Are you sure you want to delete this item?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedSub?.let { onDelete(it) }
                            showDialog = false
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("No")
                    }
                }
            )
        }

    }
}





@Composable
fun SubscriptionItem(
    sub: Subscription,
    service: Service?,
    onEdit: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedSub by remember { mutableStateOf<Subscription?>(null) }
    val colors = listOf(
        Color(0xFF90CAF9),
        Color(0xFFA5D6A7),
        Color(0xFFFFCC80),
        Color(0xFFCE93D8),
        Color(0xFFFFAB91),
        Color(0xFF80DEEA),
        Color(0xFFE6EE9C)
    )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

    val randomColor = colors.random()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(25.dp)
    ) {

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp)

            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {


            Column(modifier = Modifier.padding(start = 16.dp)) {

                Row(verticalAlignment = Alignment.Top) {

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (service != null) {
                            Image(
                                painter = painterResource(service.logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F3F3))
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(randomColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sub.name.first().uppercase(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontFamily = manropeBold
                                )
                            }
                        }

                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            sub.name,
                            color = colorResource(R.color.blue_text),
                            fontFamily = manropeBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (sub.subscriptionType == SubscriptionType.FREE_TRIAL.value){

                            Row (modifier = Modifier
                                .background(
                                    color = Color(0xFFF3DDB9), // green
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                            ){
                                Icon(painter = painterResource(R.drawable.timer),
                                    "", modifier = Modifier.size(15.dp).padding(start = 3.dp).align(
                                        Alignment.CenterVertically), tint = Color.Unspecified)
                                Text(
                                    text = "Free Trial",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontFamily = manropeBold,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ,
                                )
                            }

                        } else {

                            Row (modifier = Modifier
                                .background(
                                    color = Color(0xFF8BB755), // green
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 3.dp, vertical = 2.dp)
                            ){
                                Icon(painter = painterResource(R.drawable.container__2_), "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(15.dp).padding(start = 2.dp, top = 3.dp, bottom = 3.dp).align(
                                        Alignment.CenterVertically))
                                Text(
                                    text = "Active",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontFamily = manropeBold,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ,
                                )
                            }
                        }
                    }

                }



                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Next: ${formatDate(sub.nextBillingDate)}",
                    fontFamily = manropeMedium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = formatCurrency(sub.price, sub.currency),
                    color = colorResource(R.color.blue_text),
                    fontWeight = FontWeight.Bold,
                    fontFamily = manropeMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp)
                )

                Row(modifier = Modifier.padding(top = 16.dp)) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(0.8.dp, Color(0xFFECECEC), CircleShape)
                            .background(Color.White)
                            .clickable { onEdit(sub) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(0.8.dp, Color(0xFFECECEC), CircleShape)
                            .background(Color.White)
                            .clickable {
                                selectedSub = sub
                                showDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }



        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Delete "+selectedSub?.name)
            },
            text = {
                Text("Are you sure you want to delete this item?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSub?.let { onDelete(it) }
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}