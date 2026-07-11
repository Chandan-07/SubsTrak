package com.tracker.subscription.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.screens.home.cards.RenewalItem
import com.tracker.subscription.screens.home.cards.SubscriptionItem
import com.tracker.subscription.ui.theme.ThemeColors

@Composable
 fun ViewAllScreen(
    title: String,
    renewals: List<Renewal>?,
    onBack: () -> Unit,
    viewModel: DashboardViewModel,
    navController: NavController,
    isDarkTheme: Boolean
) {

    val context = LocalContext.current

    Column(modifier = Modifier.background(ThemeColors.getBlueBgColor(isDarkTheme)).fillMaxSize()) {
        Box{

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 56.dp)
            ) {

                IconButton(
                    onClick = { onBack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ThemeColors.getDarkGreyColor(isDarkTheme)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {

                    Text(
                        title,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            renewals?.let {
                items(renewals) { renewal ->

                    RenewalItem(
                        renewal = renewal,
                        context,
                        viewModel.getServiceByKey(renewal.key), isDarkTheme, onEdit = { subscription ->
                            navController.navigate("add_subscription?id=${subscription.id}")

                        }, onDelete = { subscription ->
                            viewModel.deleteSubscription(subscription.id)
                        })

                }
            }

        }
    }

}

@Composable
fun ViewAllSubscriptionsScreen(
    title: String,
    subscriptions: List<Subscription>?,
    onBack: () -> Unit,
    viewModel: DashboardViewModel,
    navController: NavController,
    isDarkTheme: Boolean
) {
    Column(modifier = Modifier.background(ThemeColors.getBlueBgColor(isDarkTheme)).fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 56.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ThemeColors.getDarkGreyColor(isDarkTheme)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                title,
                color = ThemeColors.getTextColor(isDarkTheme),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            subscriptions?.let { items ->
                items(items) { subscription ->
                    SubscriptionItem(
                        sub = subscription,
                        service = viewModel.getServiceByKey(subscription.key),
                        isDarkTheme = isDarkTheme,
                        onEdit = { navController.navigate("add_subscription?id=${it.id}") },
                        onDelete = { viewModel.deleteSubscription(it.id) }
                    )
                }
            }
        }
    }
}