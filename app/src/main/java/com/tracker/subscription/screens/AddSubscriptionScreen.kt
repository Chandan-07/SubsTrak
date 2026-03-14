package com.tracker.subscription.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.subscription.R
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.AddSubscriptionViewModel
import com.tracker.subscription.presentation.AddSubscriptionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    existingSubscription: Subscription? = null,
    onSave: (Subscription) -> Unit,
    onBack: () -> Unit
) {

    Log.d("TAG", "AddSubscriptionScreen: "+existingSubscription)

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
        mutableStateOf(existingSubscription?.startDate ?: System.currentTimeMillis())
    }
    var currency by remember { mutableStateOf(existingSubscription?.currency?:"₹") }

    var subscriptionType by remember { mutableStateOf(existingSubscription?.subscriptionType?: SubscriptionType.PAID_SUBSCRIPTION.value) }

    val currencyOptions = listOf("₹", "$", "€")

    val billingOptions = listOf("Weekly", "Monthly", "Yearly")

    var showDatePicker by remember { mutableStateOf(false) }

    val typeOptions = listOf(
        "Paid Subscription",
        "Free Trial"
    )

    var category by remember { mutableStateOf(existingSubscription?.category?:"Entertainment") }
    val categoryOptions = listOf(
        "Entertainment",
        "Productivity",
        "Health",
        "Education",
        "Work",
        "Rental",
        "Other"
    )

    val context = LocalContext.current

    val db = DatabaseProvider.getDatabase(context)


    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), context)
    }

    val viewModel: AddSubscriptionViewModel = viewModel(
        factory = AddSubscriptionViewModelFactory(repository)
    )
    var reminderEnabled by remember { mutableStateOf(existingSubscription?.reminderEnabled ?: false) }

    LaunchedEffect(existingSubscription) {

        existingSubscription?.let {

            serviceName = it.name
            price = it.price.toString()
            billingCycle = it.billingCycle
            category = it.category
            startDate = it.startDate
            reminderEnabled = it.reminderEnabled
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.light_grey))
            .padding(16.dp),
    ) {
        Box{

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 30.dp)
            ) {

                IconButton(
                    onClick = { onBack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(R.color.dark_blue)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {

                    Text(
                        if (existingSubscription == null)
                            "Add Subscription"
                        else
                            "Edit Subscription",
                        color = colorResource(R.color.dark_blue),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(top = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Service Name") },
                    placeholder = { Text("e.g. Netflix, Spotify") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(

                        // Background inside the text field
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFFFFFFF),

                        // Border colors
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFB0BEC5),

                        // Cursor
                        cursorColor = Color(0xFF1976D2)
                    )
                )
                DropdownField(
                    label = "Subscription Type",
                    selected = subscriptionType,
                    options = typeOptions,
                    onSelected = { subscriptionType = it }
                )
                PriceSection(
                    price = price,
                    currency = currency,
                    currencyOptions = currencyOptions,
                    onPriceChange = { price = it },
                    onCurrencySelected = { currency = it }
                )
            }


        }

        if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value) {

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

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
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {

                    val label = if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value) "Subscription Date" else "Trial Ends on"
                    OutlinedTextField(
                        value = startDate?.let { formatDate(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ReminderToggle(
                    enabled = reminderEnabled,
                    onToggle = { reminderEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF1976D2),
                            Color(0xFF42A5F5)
                        )
                    ),
                    RoundedCornerShape(14.dp)
                )
        ) {

            Button(
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                onClick = {
                    if (serviceName.isNotEmpty() &&
                        price.isNotEmpty() &&
                        startDate != null
                    ) {

                        val subscription = Subscription(
                            id = existingSubscription?.id.toString() ?: "",
                            name = serviceName,
                            price = price.toDouble(),
                            billingCycle = billingCycle,
                            startDate = startDate,
                            nextBillingDate = calculateNextBillingDate(startDate, billingCycle, subscriptionType),
                            currency = currency,
                            category = category,
                            reminderEnabled = reminderEnabled,
                            subscriptionType = subscriptionType
                        )
                        onSave(subscription)
                    }
                }
            ) {
                Text("Save Subscription", color = Color.White, fontSize = 18.sp)
            }
        }
    }
    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {

                TextButton(onClick = {

                    startDate = datePickerState.selectedDateMillis?: System.currentTimeMillis()
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
            },
            colors = SwitchDefaults.colors(

                // When switch is ON
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF1976D2),

                // When switch is OFF
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCFD8DC)
            )
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
            label = { Text("Price", color = colorResource(R.color.dark_blue)) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(

                // Background inside the text field
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF),

                // Border colors
                focusedBorderColor = Color(0xFF1976D2),
                unfocusedBorderColor = Color(0xFF798181),

                // Cursor
                cursorColor = Color(0xFF1976D2)
            )
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
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(

                // Background inside the text field
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF),

                // Border colors
                focusedBorderColor = Color(0xFF1976D2),
                unfocusedBorderColor = Color(0xFFB0BEC5),

                // Cursor
                cursorColor = Color(0xFF1976D2)
            )
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