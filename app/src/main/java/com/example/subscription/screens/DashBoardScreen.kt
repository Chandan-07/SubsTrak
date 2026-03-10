package com.example.subscription.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.subscription.data.Renewal
import com.example.subscription.data.Subscription
import com.example.subscription.presentation.DashboardViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.example.subscription.R
import com.example.subscription.data.db.DatabaseProvider
import com.example.subscription.data.db.SubscriptionDatabase
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.presentation.DashboardViewModelFactory
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
        SubscriptionRepository(db.subscriptionDao())
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E88E5),  // blue
                                Color.White
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            ) {

                ExtendedFloatingActionButton(
                    onClick = onAddSubscription,
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "ADD SUBSCRIPTIONS",
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "ADD SUBSCRIPTIONS",
                        color =  Color.Black
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->

        state?.let { data ->

            if (data.subscriptions.isEmpty()) {

                EmptySubscriptionScreen()

            } else {

                LazyColumn(
                    modifier = Modifier
                        .background(color = Color(0xFFF5F7FA))
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {

                    item {
                        MonthlySpendCard(data.monthlySpend)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Upcoming Renewals",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorResource(R.color.dark_blue),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                    }

                    items(data.upcomingRenewals) {
                        RenewalItem(it)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Subscriptions",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorResource(R.color.dark_blue),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    items(data.subscriptions) { sub ->

                        SubscriptionItem(
                            sub = sub,
                            onEdit = { subscription ->
                                navController.navigate("add_subscription?id=${subscription.id}")                            },
                            onDelete = { subscription ->
                                viewModel.deleteSubscription(subscription)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySpendCard(amount: Double) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {

        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1565C0),   // deep blue
                            Color(0xFF1E88E5),
                            Color(0xFF42A5F5),
                            Color(0xFF90CAF9),
                            Color(0xFFE3F2FD)    // soft white-blue
                        )
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 30.dp)
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                Text(
                    text = "Monthly Spend",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
    return format.format(amount)
}

@Composable
fun RenewalItem(renewal: Renewal) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = avatarColor(renewal.name),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = renewal.name.first().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    Text(
                        text = renewal.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = renewalText(renewal.daysLeft),
                        color = renewalColor(renewal.daysLeft),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(formatCurrency(renewal.price))
        }
    }
}
fun avatarColor(name: String): Color {

    val colors = listOf(
        Color(0xFFEF5350), // red
        Color(0xFFAB47BC), // purple
        Color(0xFF42A5F5), // blue
        Color(0xFF26A69A), // teal
        Color(0xFFFFA726), // orange
        Color(0xFF66BB6A)  // green
    )

    val index = abs(name.hashCode()) % colors.size
    return colors[index]
}
fun renewalText(daysLeft: Int): String {
    return when {
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
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier = Modifier
                .background(color = colorResource(R.color.white))
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    sub.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.blue_text)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Next: ${formatDate(sub.nextBillingDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column {
                Text(
                    text = formatCurrency(sub.price),
                    color = colorResource(R.color.blue_text),
                    fontWeight = FontWeight.Bold
                )

                Row( verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.End) {

                    IconButton(onClick = { onEdit(sub) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFC5BFBF),
                            modifier = Modifier.size(20.dp)
                        )
                    }


                    IconButton(onClick = { onDelete(sub) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFC5BFBF),
                            modifier = Modifier.size(20.dp)
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