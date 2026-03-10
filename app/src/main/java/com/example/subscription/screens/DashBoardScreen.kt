package com.example.subscription.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.room.Room
import com.example.subscription.data.db.DatabaseProvider
import com.example.subscription.data.db.SubscriptionDatabase
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.presentation.DashboardViewModelFactory

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
            ExtendedFloatingActionButton(
                onClick = onAddSubscription,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subscription"
                    )
                },
                text = { Text("Add Subscription") }
            )
        }

    ) { padding ->

        state?.let { data ->

            if (data.subscriptions.isEmpty()) {

                EmptySubscriptionScreen()

            } else {

                LazyColumn(
                    modifier = Modifier
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
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(data.upcomingRenewals) {
                        RenewalItem(it)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Subscriptions",
                            style = MaterialTheme.typography.titleMedium
                        )
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
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text("Monthly Spend")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "₹$amount",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(renewal.name)

                Text(
                    "Renews in ${renewal.daysLeft} days",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("₹${renewal.price}")
        }
    }
}

@Composable
fun SubscriptionItem(
    sub: Subscription,
    onEdit: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    sub.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Next: ${formatDate( sub.nextBillingDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text("₹${sub.price}")

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { expanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEdit(sub)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDelete(sub)
                        }
                    )
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
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Add your first subscription to start tracking spending.",
            textAlign = TextAlign.Center
        )
    }
}