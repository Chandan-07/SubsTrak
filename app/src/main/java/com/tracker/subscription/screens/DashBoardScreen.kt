package com.tracker.subscription.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.presentation.DashboardViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.Utility.formatCurrency
import com.tracker.subscription.Utility.getDaysLeft
import com.tracker.subscription.Utility.getGreeting
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun DashboardScreen(
    navController: NavController,
    onAddSubscription: () -> Unit = {}
) {

    val context = LocalContext.current




    val db = DatabaseProvider.getDatabase(context)

    val smsDataSource = SmsDataSource(context)
    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsState()
    val smsState by viewModel.smsSyncState.collectAsState()
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val isLoading by viewModel.isLoadingSMS.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanSms()
        } else {
            // show message
        }
    }
    Log.d("ASKNMRDSA", "DashboardScreen: "+smsState.size)
    Scaffold(
        floatingActionButton = {
            if (state?.subscriptions?.isEmpty() == true) {

                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 10.dp,
                                    shape = RoundedCornerShape(30),
                                    ambientColor = Color(0xFF033556),
                                    spotColor = colorResource(R.color.dark_blue)
                                )
                                .background(
                                    color = colorResource(R.color.lime),
                                    shape = if (state?.subscriptions?.isEmpty() == true) RoundedCornerShape(
                                        30
                                    ) else RoundedCornerShape(100.dp),
                                )
                        ) {

                            ExtendedFloatingActionButton(
                                onClick = onAddSubscription,
                                containerColor = Color.Transparent,
                                shape = if (state?.subscriptions?.isEmpty() == true) RoundedCornerShape(
                                    30
                                ) else CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(0.dp) // 👈 remove double shadow
                            ) {

                                if (state?.subscriptions?.isEmpty() == true) {

                                   Icon(imageVector = Icons.Default.Add,"")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add Subscriptions",
                                        color = colorResource(R.color.dark_blue),
                                        fontFamily = manropeBold
                                    )
                                }

                            }
                        }
            } else {
                FloatingActionButton(
                    onClick = onAddSubscription,
                    containerColor = Color.Transparent,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.fab_add),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        },
        floatingActionButtonPosition = if (state?.subscriptions?.isEmpty() == true) FabPosition.Center else FabPosition.End,
    ) { padding ->

        val manropeSemiBold = FontFamily( Font(R.font.manrope_semi_bold) )
        val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
        val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val manropeMedium = FontFamily( Font(R.font.manrope_medium) )


        state?.let { data ->

            if (data.subscriptions.isEmpty()) {

                Box {
                    EmptySubscriptionScreen(data)
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .background(color = Color(0xDCF6F8FF))
                        .fillMaxSize()
                        .padding(padding)
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
                ) {
                    item {
                        MonthlySpendCard(data, data.currency, data.monthlySpend, navController)
                    }

//                    item {
//                        Column() {
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Button(onClick = {
////                                onScanSms()
//                                if (ContextCompat.checkSelfPermission(
//                                        context,
//                                        Manifest.permission.READ_SMS
//                                    ) == PackageManager.PERMISSION_GRANTED
//                                ) {
//                                    viewModel.scanSms()
//                                } else {
//                                    permissionLauncher.launch(Manifest.permission.READ_SMS)
//                                }
//                            }) {
//                                Text("Scan SMS")
//                            }
//
//                            Log.d("ASJNDA", "DashboardScreen: "+isLoading)
//                            Log.d("ASJNDA", "DashboardScreen: "+smsState.size)
//
//                            if (isLoading) {
//                                Dialog(onDismissRequest = {}) {
//                                    Column(
//                                        modifier = Modifier
//                                            .clip(RoundedCornerShape(20.dp))
//                                            .background(Color.White)
//                                            .padding(24.dp),
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        CircularProgressIndicator()
//                                        Spacer(modifier = Modifier.height(16.dp))
//                                        Text("Scanning your messages...")
//                                    }
//                                }
//                            } else {
//                                if (!smsState.isEmpty()){
//                                    LazyRow() {
//                                        items(smsState){
//                                            RenewalItem(Renewal(
//                                                price = it.amount,
//                                                name = it.service,
//                                                nextBillingDate = it.date,
//                                                daysLeft = getDaysLeft(it.date),
//                                                key = it.service,
//                                                packageName = "",
//                                                subscriptionType = SubscriptionType.PAID_SUBSCRIPTION.name
//                                            ), context, viewModel.getServiceByKey(it.service))
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }



                    if ( !data.freeTrials.isEmpty()){
                        item {

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {


                                Text(
                                    text = "Free Trials",
                                    color = colorResource(R.color.black),
                                    fontSize = 21.sp,
                                    fontFamily = manropeExtraBold,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )


                                if (!data.freeTrials.isEmpty() && data.freeTrials.size >2) {
                                    Text(
                                        text = "View All",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = manropeRegular,
                                        modifier = Modifier
                                            .clickable {
                                                navController.navigate("view_all_free_trials")
                                            }
                                            .background(
                                                color = colorResource(R.color.dark_blue),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(
                                                start = 10.dp,
                                                end = 10.dp,
                                                top = 4.dp,
                                                bottom = 4.dp
                                            )
                                    )
                                }

                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(data.freeTrials.take(3)) {
                            RenewalItem(it, context, viewModel.getServiceByKey(it.key))
                        }
                    }


                    if (!data.upcomingRenewals.isEmpty() ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Upcoming Renewals",
                                    fontFamily = manropeExtraBold,
                                    color = colorResource(R.color.black),
                                    fontSize = 21.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )

                                if (data.upcomingRenewals.size >2){
                                    Text(
                                        text = "View All",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .clickable {
                                                navController.navigate("view_all_renewals")
                                            }
                                            .background(
                                                color = colorResource(R.color.dark_blue),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(
                                                start = 10.dp,
                                                end = 10.dp,
                                                top = 4.dp,
                                                bottom = 4.dp
                                            )
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.height(8.dp))

                        }
                    }


                    items(data.upcomingRenewals.take(3)) {
                        RenewalItem(it, context, viewModel.getServiceByKey(it.key))
                    }

//                    item {
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            "Subscriptions",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = Color.White,
//                            fontSize = 15.sp,
//                            modifier = Modifier
//                                .background(
//                                    color = colorResource(R.color.dark_blue),
//                                    shape = RoundedCornerShape(20.dp)
//                                )
//                                .padding(horizontal = 12.dp, vertical = 6.dp)
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                    items(data.subscriptions) { sub ->
//
//                        SubscriptionItem(
//                            sub = sub,
//                            onEdit = { subscription ->
//                                navController.navigate("add_subscription?id=${subscription.id}")                            },
//                            onDelete = { subscription ->
//                                viewModel.deleteSubscription(subscription)
//                            }
//                        )
//                    }
                }
            }
        }
    }

}


@Composable
fun MonthlySpendCard(data: DashboardData, currency: String, amount: Double, navController: NavController) {

    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)

            ,
            elevation = CardDefaults.cardElevation(20.dp),
            shape = RoundedCornerShape(30.dp)

        ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF4866F1)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {

                    Text(
                        text = getGreeting() + data.user?.name,
                        color = colorResource(R.color.white),
                        fontFamily = manropeExtraBold,
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row( modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,) {
                        Text(
                        text = "${data.subscriptions.size}/5 subscriptions",
                        color = colorResource(R.color.white),
                        fontFamily = manropeMedium,
                        fontSize = 16.sp
                       )
                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = "Upgrade",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = manropeMedium,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("premium")
                                }
                                .background(
                                    color = colorResource(R.color.dark_blue),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(0.5.dp, Color.White, RoundedCornerShape(20.dp))
                                .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                        )

                    }


                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "You have ${data.upcomingRenewals.size} Subscriptions & ${data.freeTrials.size} FreeTrials",
                        color = colorResource(R.color.white),
                        fontFamily = manropeMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                }
            }

        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)

            ,
            elevation = CardDefaults.cardElevation(30.dp),
            shape = RoundedCornerShape(30.dp)

        ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF3E0),
                                Color(0xFFF1DEF3),
                                Color(0xFFE3F2FD)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 20.dp)) {

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Monthly Spend",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontFamily = manropeMedium,
                            modifier = Modifier.padding(top = 15.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatCurrency(amount),
                            fontSize = 35.sp,
                            fontFamily = manropeExtraBold,
                            color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.white), shape = RoundedCornerShape(20.dp))
                        .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)) {
                        Row {
                            Icon(painterResource(R.drawable.chart), "")
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "YEARLY PROJECTION",
                                color = Color.Black.copy(alpha = 0.7f),
                                fontFamily = manropeMedium,
                                fontSize = 12.sp,
                            )
                        }
                        Text(
                            formatCurrency(amount*12), style =  MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 20.sp),
                            fontFamily = manropeBold,
                            color = colorResource(R.color.dark_blue)
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Based on current spending",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontFamily = manropeMedium,
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }



                }
            }

        }

    }


}


@Composable
fun FreeTrial(renewal: Renewal) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        elevation = CardDefaults.cardElevation(2.dp)


    ) {
        val manropeSemiBold = FontFamily( Font(R.font.manrope_semi_bold) )
        val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
        val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 20.dp)) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (renewal.logoResId != null && renewal.logoResId != -1) {
                        Log.d("ASFDKDN", "RenewalItem: "+renewal.logoResId)
                        Image(
                            painter = painterResource(renewal.logoResId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F3F3))
                               ,
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = renewal.name.first().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    Text(
                        text = renewal.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = renewalDateText(renewal.nextBillingDate, renewal.subscriptionType),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = manropeMedium
                    )

                    Text(
                        text = remainingTimeText(renewal.daysLeft, renewal.subscriptionType),
                        color = renewalColor(renewal.daysLeft),
                        fontSize = 12.sp,
                        fontFamily = manropeBold
                    )
                }
            }



        }
    }
}



@Composable
fun RenewalItem(renewal: Renewal, context: Context, service: Service?) {
    val manropeSemiBold = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

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
                                fontSize = 20.sp,
                                fontFamily = manropeExtraBold,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (renewal.subscriptionType == SubscriptionType.FREE_TRIAL.value){

                            Row (modifier = Modifier
                                .background(
                                    color = Color(0xFFFBBC05), // green
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                            ){
                                Icon(painter = painterResource(R.drawable.glit_black),
                                    "", modifier = Modifier.size(15.dp).padding(top = 5.dp).align(Alignment.CenterVertically))
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
                                        color = Color(0xFF1A73E8), // green
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .padding(horizontal = 3.dp, vertical = 2.dp)
                                ){
                                    Icon(painter = painterResource(R.drawable.container__2_), "",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(15.dp).padding(start = 2.dp, top = 3.dp, bottom = 3.dp).align(Alignment.CenterVertically))
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

                Spacer(Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(renewal.price), style = MaterialTheme.typography.bodyMedium, color = colorResource(R.color.black), fontSize = 18.sp, fontFamily = manropeExtraBold)
                    Spacer(Modifier.height(30.dp))
                }


            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = renewalDateText(renewal.nextBillingDate, renewal.subscriptionType),
                color = colorResource(R.color.dark_grey),
                fontSize = 12.sp,
                fontFamily = manropeMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                Text(
                    text = remainingTimeText(renewal.daysLeft, renewal.subscriptionType),
                    color = renewalColor(renewal.daysLeft),
                    fontSize = 10.sp,
                    fontFamily = manropeBold
                )

                Text("Take Action", color = colorResource(R.color.blue), fontSize = 10.sp,
                    fontFamily = manropeMedium,
                    modifier = Modifier
                        .background(
                            color = colorResource(R.color.white),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(
                            0.5.dp,
                            color = colorResource(R.color.blue),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .clickable {
                            openSubscription(context, renewal)
                        },
                    textAlign = TextAlign.End
                )
            }

        }

    }
}


fun openSubscription(context: Context, sub: Renewal) {

    val packageName = sub.packageName
    if (!packageName.isNullOrEmpty()) {

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            context.startActivity(launchIntent)
            return
        }

        // fallback → Play Store
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/account/subscriptions")
                    setPackage("com.android.vending")
                }
            )
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions")
                )
            )
        }

    } else {
        // fallback → Google search
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${sub.name} unsubscribe"))
        )
    }
}
fun avatarColor(name: String): Color {

    val colors = listOf(
        Color(0xFF980702), // red
        Color(0xFF97E099), // purple
        Color(0xFF456D8D), // blue
        Color(0xFFFFC107), // teal
        Color(0xFF791296), // orange
        Color(0xFF720623)  // green
    )

    val index = abs(name.hashCode()) % colors.size
    return colors[index]
}
fun renewalDateText(date: Long, subscriptionType: String): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    val formattedDate = sdf.format(Date(date))

    return if (subscriptionType == SubscriptionType.FREE_TRIAL.value) {
        "Trial ends on $formattedDate"
    } else {
        "Renews on $formattedDate"
    }

}

fun remainingTimeText(daysLeft: Int, subscriptionType: String): String {

    val absDays = abs(daysLeft)

    if (subscriptionType == SubscriptionType.FREE_TRIAL.value) {
        return when (daysLeft) {
            0 -> "Ends today"
            1 -> "Ends tomorrow"
            else -> formatDuration(absDays)
        }
    }

    return when {
        daysLeft < 0 -> "Renewed ${formatDuration(absDays)} ago"
        daysLeft == 0 -> "Renews today"
        daysLeft == 1 -> "Renews tomorrow"
        else -> formatDuration(daysLeft)
    }
}

fun formatDuration(days: Int): String {
    return when {
        days >= 365 -> {
            val years = days / 365
            if (years == 1) "1 year left" else "$years years left"
        }
        days >= 30 -> {
            val months = days / 30
            if (months == 1) "1 month left" else "$months months left"
        }
        else -> {
            if (days == 1) "1 day left" else "$days days left"
        }
    }
}

fun renewalColor(daysLeft: Int): Color {
    return when {
        daysLeft <= 2 -> Color(0xFFE53935)   // Red
        daysLeft <= 7 -> Color(0xFFFB8C00)   // Orange
        else -> Color.Gray
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
                            .size(42.dp)
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
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F3F3))
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(randomColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sub.name.first().uppercase(),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = manropeBold
                                )
                            }
                        }

                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        sub.name,
                        color = colorResource(R.color.blue_text),
                        fontFamily = manropeBold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }



                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    "Next: ${formatDate(sub.nextBillingDate)}",
                   fontFamily = manropeRegular,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = formatCurrency(sub.price),
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

@Composable
fun EmptySubscriptionScreen(data: DashboardData) {
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)

            ,
            elevation = CardDefaults.cardElevation(20.dp),
            shape = RoundedCornerShape(30.dp)

        ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF4866F1)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {

                    Text(
                        text = getGreeting() + data.user?.name,
                        color = colorResource(R.color.white),
                        fontFamily = manropeExtraBold,
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row( modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,) {
                        Text(
                            text = "${data.subscriptions.size}/5 subscriptions",
                            color = colorResource(R.color.white),
                            fontFamily = manropeMedium,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = "Upgrade",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = manropeMedium,
                            modifier = Modifier
                                .clickable {

                                }
                                .background(
                                    color = colorResource(R.color.dark_blue),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .border(0.5.dp, Color.White, RoundedCornerShape(20.dp))
                                .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                        )

                    }


                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "You have ${data.upcomingRenewals.size} Subscriptions & ${data.freeTrials.size} FreeTrials",
                        color = colorResource(R.color.white),
                        fontFamily = manropeMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))


                }
            }

        }
        Spacer(modifier = Modifier.height(30.dp))

        Icon(
           painter = painterResource(R.drawable.empty_task),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .width(200.dp)
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Subscriptions Yet",
            style = MaterialTheme.typography.titleLarge,
            color = colorResource(R.color.dark_blue),
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Add your first subscription to start tracking spending.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        )
    }
}