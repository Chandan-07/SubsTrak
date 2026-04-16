package com.tracker.subscription.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.tracker.subscription.Utility.getDaysLeft
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.screens.home.cards.RenewalItem

@Composable
fun SyncSMS(context: Context, viewModel: DashboardViewModel, permissionLauncher: ActivityResultLauncher<String>) {
    val smsState by viewModel.smsSyncState.collectAsState()
    val isLoading by viewModel.isLoadingSMS.collectAsState()
    Column() {

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.scanSms()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }) {
            Text("Scan SMS")
        }

        if (isLoading) {
            Dialog(onDismissRequest = {}) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning your messages...")
                }
            }
        } else {
            if (!smsState.isEmpty()){
                LazyRow() {
                    items(smsState) {
//                        RenewalItem(
//                            Renewal(
//                                id = System.currentTimeMillis().toString(),
//                                price = it.amount,
//                                name = it.service,
//                                nextBillingDate = it.date,
//                                daysLeft = getDaysLeft(it.date),
//                                key = it.service,
//                                packageName = "",
//                                currency = "",
//                                subscriptionType = SubscriptionType.PAID_SUBSCRIPTION.name
//                            ), context, viewModel.getServiceByKey(it.service))

                    }
                }
            }
        }
    }
}
