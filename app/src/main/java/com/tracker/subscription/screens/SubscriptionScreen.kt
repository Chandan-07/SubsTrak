package com.tracker.subscription.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory

@Composable
fun SubscriptionScreen(
    navController: NavController,
    onAddSubscription: () -> Unit = {}
){

    val context = LocalContext.current


    val db = DatabaseProvider.getDatabase(context)


    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context)
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )

    val state by viewModel.uiState.collectAsState()

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
//                            text = viewModel.currentUser?.displayName ?: "User",
//                            color = colorResource(R.color.orrange),
//                            fontSize = 20.sp,
//                            style = MaterialTheme.typography.bodyLarge
//                        )
//                        Spacer(Modifier.height(10.dp))
//                    }
                EmptySubscriptionScreen()

            }

        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "My Subscriptions",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(R.color.dark_blue),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(data.subscriptions) { sub ->

                    SubscriptionItem(
                        sub = sub,
                        onEdit = { subscription ->
                            navController.navigate("add_subscription?id=${subscription.id}")
                        },
                        onDelete = { subscription ->
                            viewModel.deleteSubscription(subscription)
                        }
                    )
                }

            }
        }
    }

}