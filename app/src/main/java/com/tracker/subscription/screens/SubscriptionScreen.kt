package com.tracker.subscription.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import com.tracker.subscription.screens.home.EmptySubscriptionScreen
import com.tracker.subscription.screens.home.cards.SubscriptionItem

@Composable
fun SubscriptionScreen(
    isLoggedIn:Boolean,
    navController: NavController,
    onAddSubscription: () -> Unit = {}
){

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
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 25.dp).background(Color(0xFFF5F6FA))) {

        state?.let { data ->

            if (data.subscriptions.isEmpty()) {

                Box {
                    EmptySubscriptionScreen(navController,isLoggedIn, data)
                }

            } else {
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
                LazyColumn() {
                    items(data.subscriptions) { sub ->

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