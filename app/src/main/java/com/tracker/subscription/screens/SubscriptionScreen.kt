package com.tracker.subscription.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory
import com.tracker.subscription.screens.home.DashboardUiState
import com.tracker.subscription.screens.home.EmptySubscriptionScreen
import com.tracker.subscription.screens.home.cards.SubscriptionItem

@Composable
fun SubscriptionScreen(
    isLoggedIn:Boolean,
    navController: NavController,
    onAddSubscription: () -> Unit = {}
){

    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "OTT", "Music", "Productivity", "Shopping", "Fitness", "AI"
    )

    val db = DatabaseProvider.getDatabase(context)


    val smsDataSource = SmsDataSource(context)
    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
    }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository)
    )
    val subscriptions by viewModel.filteredSubscriptions.collectAsState()

    val state by viewModel.uiState.collectAsState()
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropesemiBold = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF5F6FA))
        .padding(horizontal = 25.dp)) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            "My Subscriptions",
            color = colorResource(R.color.dark_blue),
            fontSize = 24.sp,
            fontFamily = manropeExtraBold,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        when(state) {
            DashboardUiState.Loading -> {
                Log.d("TAG", "SubscriptionScreen: ")
            }
            is DashboardUiState.Success -> {
                val data = (state as DashboardUiState.Success).data

                    if (data.subscriptions.isEmpty()) {

                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(30.dp))
                            Icon(
                                painter = painterResource(R.drawable.empty_task),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(120.dp)
                            )

                            Text(
                                "No Subscriptions Yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = colorResource(R.color.dark_blue),
                                fontSize = 26.sp,
                                textAlign = TextAlign.Center,
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

                    } else {


                        Spacer(Modifier.height(20.dp))

                        // 🔍 Search
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.searchSubscriptions(it)
                            },
                            placeholder = { Text("Search Subscriptions...", fontFamily = manropeRegular) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .background(
                                    Color.LightGray.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                        )

                        Spacer(Modifier.height(12.dp))

                        // 🧩 Category chips
                        LazyRow {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                        selectedCategory = category
                                        viewModel.filterByCategory(category)
                                    },
                                    label = { Text(category, fontFamily = manropesemiBold, color = colorResource(R.color.dark_blue)) },
                                    shape = RoundedCornerShape(50), // 👈 MORE ROUNDED
                                    modifier = Modifier.padding(end = 15.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorResource(R.color.lime),
                                        selectedLabelColor = Color.White
                                    ),
                                    border = BorderStroke(0.7.dp, Color(0xFFD9D8D8))
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        LazyColumn() {
                            items(subscriptions) { sub ->

                                SubscriptionItem(
                                    sub = sub,
                                    viewModel.getServiceByKey(sub.key),
                                    onEdit = { subscription ->
                                        navController.navigate("add_subscription?id=${subscription.id}")
                                    },
                                    onDelete = { subscription ->
                                        viewModel.deleteSubscription(subscription.id)
                                    }
                                )
                            }

                        }
                }
            }
        }


    }


}