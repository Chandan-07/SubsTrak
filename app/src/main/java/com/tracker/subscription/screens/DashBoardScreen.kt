package com.tracker.subscription.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModelFactory
import okhttp3.internal.wait
import java.text.NumberFormat
import kotlin.math.abs

@Composable
fun DashboardScreen(
    navController: NavController,
    onAddSubscription: () -> Unit = {}
) {

    val context = LocalContext.current


    val db = DatabaseProvider.getDatabase(context)


    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context)
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 40.dp), // 👈 pushes down to touch navbar
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color(0xFF033556),
                            spotColor = colorResource(R.color.dark_blue)
                        )
                        .background(
                            color = colorResource(R.color.dark_blue),
                            shape = RoundedCornerShape(50)
                        )
                ) {

                    ExtendedFloatingActionButton(
                        onClick = onAddSubscription,
                        containerColor = Color.Transparent,
                        shape = RoundedCornerShape(50),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp) // 👈 remove double shadow
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "ADD SUBSCRIPTIONS",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->

        state?.let { data ->

            if (data.subscriptions.isEmpty()) {

                Box {
//                    Row {
//                        Text(
//                            text = "Good Afternoon",
//                            color = colorResource(R.color.dark_blue),
//                            fontSize = 16.sp,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Text(
//                            text = viewModel.getUser(),
//                            color = colorResource(R.color.orrange),
//                            fontSize = 20.sp,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Spacer(Modifier.height(10.dp))
//                    }
                    EmptySubscriptionScreen()

                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .background(color = Color(0xDCF6F8FF))
                        .fillMaxSize()
                        .padding(padding)
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
                ) {

//                    item {
//                        Row {
//                            Text(
//                                text = "Good Afternoon",
//                                color = colorResource(R.color.dark_blue),
//                                fontSize = 16.sp,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                            Text(
//                                text = viewModel.getUser(),
//                                color = colorResource(R.color.orrange),
//                                fontSize = 20.sp,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                            Spacer(Modifier.height(10.dp))
//                        }
//
//                    }
                    item {
                        MonthlySpendCard(data.currency, data.monthlySpend)
                    }


                    item {

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Free Trials",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorResource(R.color.dark_blue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )

                            if (!data.freeTrials.isEmpty() && data.freeTrials.size >2) {
                                Text(
                                    text = "View All",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        navController.navigate("view_all_free_trials")
                                    }.background(
                                        color = colorResource(R.color.dark_blue),
                                        shape = RoundedCornerShape(20.dp)
                                    ).padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                                )
                            }

                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

//                    item() {
//                        LazyRow {
//                            items(data.freeTrials) {
//                                FreeTrial(it)
//                            }
//
//                        }
//
//                    }
                    items(data.freeTrials) {
                        RenewalItem(it, context)
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
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colorResource(R.color.dark_blue),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )

                                if (data.upcomingRenewals.size >2){
                                    Text(
                                        text = "View All",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.clickable {
                                            navController.navigate("view_all_renewals")
                                        }.background(
                                            color = colorResource(R.color.dark_blue),
                                            shape = RoundedCornerShape(20.dp)
                                        ).padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.height(8.dp))

                        }
                    }


                    items(data.upcomingRenewals) {
                        RenewalItem(it, context)
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
fun MonthlySpendCard(currency: String, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)

        ,
        elevation = CardDefaults.cardElevation(20.dp)

    ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
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
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {

                    Text(
                        text = "Monthly Spend",
                        color = Color.Black.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(currency+ " "+ amount.toString(), style =  MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp), fontWeight = FontWeight.Bold,
                        color = Color.Black)

                }
            }

    }

}
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
    return format.format(amount)
}

@Composable
fun FreeTrial(renewal: Renewal) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        elevation = CardDefaults.cardElevation(2.dp)


    ) {

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
                    if (renewal.logoResId != null) {
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
                        text = renewalText(renewal.daysLeft, renewal.subscriptionType),
                        color = renewalColor(renewal.daysLeft),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (renewal.subscriptionType == SubscriptionType.FREE_TRIAL.value){
                Text(
                    text = "Free",
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF8BC34A), // green
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Text(formatCurrency(renewal.price))
            }

        }
    }
}
@Composable
fun RenewalItem(renewal: Renewal, context: Context) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(3.dp)


    ) {

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 20.dp, end = 10.dp),
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
                    if (renewal.logoResId != null) {
                        Image(
                            painter = painterResource(renewal.logoResId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
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

                    Row {
                        Text(
                            text = renewal.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (renewal.subscriptionType == SubscriptionType.FREE_TRIAL.value){
                            Text(
                                text = "Free",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF8BC34A), // green
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = renewalText(renewal.daysLeft, renewal.subscriptionType),
                        color = renewalColor(renewal.daysLeft),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(renewal.price), style = MaterialTheme.typography.bodyMedium, color = colorResource(R.color.orrange), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(30.dp))
                Text("Take Action", color = colorResource(R.color.blue), fontSize = 10.sp,
                    modifier = Modifier.background(
                        color = colorResource(R.color.light_grey),
                        shape = RoundedCornerShape(20.dp),
                    ).padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        .clickable {
                            openSubscription(context, renewal)
                    })
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
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName"))
            )
        } catch (e: Exception) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            )
        }

    } else {
        // fallback → Google search
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${sub.name}"))
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
fun renewalText(daysLeft: Int, subscriptionType: String): String {
    return when {
        subscriptionType == SubscriptionType.FREE_TRIAL.value -> when (daysLeft) {
            0 -> "Free trial ends today"
            1 -> "Free trial ends tomorrow"
            else -> "Free trial ends in ${abs(daysLeft)} days"
        }

        daysLeft < 0 -> "Renewed ${abs(daysLeft)} days ago"
        daysLeft == 0 -> "Renews today"
        daysLeft == 1 -> "Renews tomorrow"
        else -> "Renews in $daysLeft days"
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
    onEdit: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding( top = 16.dp, bottom = 12.dp)

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
                        if (sub.logoResId != null) {
                            Image(
                                painter = painterResource(sub.logoResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F3F3))
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = sub.name.first().uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        sub.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(R.color.blue_text),
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }



                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    "Next: ${formatDate(sub.nextBillingDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp)) {
                Text(
                    text = formatCurrency(sub.price),
                    color = colorResource(R.color.blue_text),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End).padding(end = 16.dp)
                )

                Row(modifier = Modifier.padding(top = 16.dp)) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(0.8.dp,Color(0xFFECECEC), CircleShape)
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
                            .border(0.8.dp,Color(0xFFECECEC), CircleShape)
                            .background(Color.White)
                            .clickable { onDelete(sub) },
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
}

@Composable
fun EmptySubscriptionScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Subscriptions Yet",
            style = MaterialTheme.typography.titleLarge,
            color = colorResource(R.color.dark_blue),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Add your first subscription to start tracking spending.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        )
    }
}