package com.example.subscription.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.subscription.data.Subscription
import com.example.subscription.data.db.DatabaseProvider
import com.example.subscription.data.db.SubscriptionDatabase
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.presentation.AddSubscriptionViewModel
import com.example.subscription.presentation.AddSubscriptionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    existingSubscription: Subscription? = null,
    onSave: (Subscription) -> Unit
) {

    var serviceName by remember {
        mutableStateOf(existingSubscription?.name ?: "")
    }

    var price by remember {
        mutableStateOf(existingSubscription?.price?.toString() ?: "")
    }

    var billingCycle by remember {
        mutableStateOf(existingSubscription?.billingCycle ?: "Monthly")
    }

    var startDate by remember {
        mutableStateOf(existingSubscription?.startDate)
    }
    var currency by remember { mutableStateOf("₹") }
    val currencyOptions = listOf("₹", "$", "€")

    val billingOptions = listOf("Monthly", "Yearly")

    var showDatePicker by remember { mutableStateOf(false) }

    var category by remember { mutableStateOf("Entertainment") }
    val categoryOptions = listOf(
        "Entertainment",
        "Productivity",
        "Health",
        "Education",
        "Other"
    )

    val context = LocalContext.current

    val db = DatabaseProvider.getDatabase(context)


    val repository = remember {
        SubscriptionRepository(db.subscriptionDao())
    }

    val viewModel: AddSubscriptionViewModel = viewModel(
        factory = AddSubscriptionViewModelFactory(repository)
    )
    var reminderEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TopAppBar(
                    title = {
                        Text(
                            if (existingSubscription == null)
                                "Add Subscription"
                            else
                                "Edit Subscription"
                        )
                    }
                ) }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text("Service Name") },
                modifier = Modifier.fillMaxWidth()
            )

            PriceSection(
                price = price,
                currency = currency,
                currencyOptions = currencyOptions,
                onPriceChange = { price = it },
                onCurrencySelected = { currency = it }
            )

            DropdownField(
                label = "Billing Cycle",
                selected = billingCycle,
                options = billingOptions,
                onSelected = { billingCycle = it }
            )

            DropdownField(
                label = "Category",
                selected = category,
                options = categoryOptions,
                onSelected = { category = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {

                OutlinedTextField(
                    value = startDate?.let { formatDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Subscription Start Date") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ReminderToggle(
                enabled = reminderEnabled,
                onToggle = { reminderEnabled = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {

                    if (serviceName.isNotEmpty() &&
                        price.isNotEmpty() &&
                        startDate != null
                    ) {

                        viewModel.saveSubscription(
                            name = serviceName,
                            price = price.toDouble(),
                            currency = currency,
                            billingCycle = billingCycle,
                            category = category,
                            startDate = startDate!!,
                            reminderEnabled = reminderEnabled
                        )
                        val subscription = Subscription(
                            id = existingSubscription?.id ?: "",
                            name = serviceName,
                            price = price.toDouble(),
                            billingCycle = billingCycle,
                            startDate = startDate!!,
                            nextBillingDate = 0L,
                            currency = currency,
                            category = category,
                            reminderEnabled = reminderEnabled
                        )

                        onSave(subscription)
                    }
                }
            ) {
                Text("Save Subscription")
            }
        }
    }
    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {

                TextButton(onClick = {

                    startDate = datePickerState.selectedDateMillis
                    showDatePicker = false

                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun ReminderToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text("Enable Reminder")

            Text(
                "Get notified before renewal",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = {

                if (it) {
                    requestNotificationPermission(context)
                }

                onToggle(it)
            }
        )
    }
}
fun requestNotificationPermission(context: Context) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }
}


@Composable
fun PriceSection(
    price: String,
    currency: String,
    currencyOptions: List<String>,
    onPriceChange: (String) -> Unit,
    onCurrencySelected: (String) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        DropdownField(
            label = "Currency",
            selected = currency,
            options = currencyOptions,
            modifier = Modifier.width(100.dp),
            onSelected = onCurrencySelected
        )

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = { Text("Price") },
            modifier = Modifier.weight(1f)
        )
    }
}


fun formatDate(time: Long): String {

    val sdf = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return sdf.format(Date(time))
}

fun formatDate(time: String): String {

    val sdf = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return sdf.format(Date(time.toLong()))
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach {

                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Billing Cycle") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach {

                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach {

                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}