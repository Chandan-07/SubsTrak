package com.tracker.subscription.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.subscription.R
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.Option
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.AddSubscriptionViewModel
import com.tracker.subscription.presentation.AddSubscriptionViewModelFactory
import com.tracker.subscription.presentation.CommonOptions
import com.tracker.subscription.presentation.Widgets.SingleSelectChips
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

    var subscriptionType by remember { mutableStateOf(existingSubscription?.subscriptionType?: CommonOptions.subscriptionType.first().name) }

    val currencyOptions = listOf(
        "₹", // INR
        "$", // USD
        "€", // EUR
        "£", // GBP
        "¥", // JPY / CNY
        "₩", // KRW
        "₽", // RUB
        "₺", // TRY
        "₫", // VND
        "₱", // PHP
        "₪", // ILS
        "₦", // NGN
        "₴", // UAH
        "₡", // CRC
        "₲"  // PYG
    )

    var showDatePicker by remember { mutableStateOf(false) }


    var category by remember { mutableStateOf(existingSubscription?.category?:CommonOptions.categoryList.first().name) }


    var serviceLogo by remember { mutableStateOf(R.drawable.netflix) }


    val context = LocalContext.current

    val db = DatabaseProvider.getDatabase(context)


    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context)
    }

    val viewModel: AddSubscriptionViewModel = viewModel(
        factory = AddSubscriptionViewModelFactory(repository)
    )
    var reminderEnabled by remember { mutableStateOf(existingSubscription?.reminderEnabled ?: false) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var buttonEnabled by remember { mutableStateOf(price.isNotEmpty()) }

    LaunchedEffect(existingSubscription) {

        existingSubscription?.let {
            serviceName = it.name
            price = it.price.toString()
            billingCycle = it.billingCycle
            category = it.category
            startDate = it.startDate
            reminderEnabled = it.reminderEnabled
            serviceLogo = it.logoResId!!
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.white))
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Box{

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 5.dp)
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

        val interactionSource = remember { MutableInteractionSource() }
        LazyColumn (
            modifier = Modifier
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
            val manropeBold = FontFamily( Font(R.font.manrope_bold) )
            val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

            item {
                Column {
                    Text(
                        text = "Select your Service",
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontFamily = manropeBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSheet = true }
                    ) {
                        OutlinedTextField(
                            value = serviceName,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Service Name") },
                            placeholder = { Text("e.g. Netflix, Spotify") },

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    Log.d("Sheet", "Clicked")
                                    showSheet = true
                                },

                            trailingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            },

                            leadingIcon = {

                                val service = viewModel.getServiceLogo(serviceName)

                                when {
                                    service != null -> {
                                        Image(
                                            painter = painterResource(service.logo),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                        )
                                    }

                                    serviceName.isNotEmpty() -> {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF90CAF9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = serviceName.first().uppercase(),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    else -> {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp)

                        )
                    }
                }


            }



            item {
                SingleSelectChips(
                    label = "Subscription Type",
                    selected = subscriptionType,
                    options = CommonOptions.subscriptionType,
                    onSelected = { subscriptionType = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                PriceSection(
                    price = price,
                    currency = currency,
                    currencyOptions = currencyOptions,
                    onPriceChange = { price = it
                        buttonEnabled = price.isNotEmpty()},
                    onCurrencySelected = { currency = it }
                )
            }
            item { if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    SingleSelectChips(
                        label = "Billing Cycle",
                        selected = billingCycle,
                        options = CommonOptions.billing,
                        onSelected = { billingCycle = it }
                    )

                    SingleSelectChips(
                        label = "Category",
                        selected = category,
                        options = CommonOptions.categoryList,
                        onSelected = { category = it }
                    )
                }

            }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("When does the subscription start?", fontFamily = manropeBold, fontSize = 18.sp)
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
                            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp, fontFamily = manropeMedium),
                            colors = OutlinedTextFieldDefaults.colors(

                                // Background inside the text field
                                focusedContainerColor = Color(0xFFFFFFFF),
                                unfocusedContainerColor = Color(0xFFFFFFFF),

                                // Border colors
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFB0BEC5),

                                // Cursor
                                cursorColor = Color(0xFF1976D2)
                            ),
                            trailingIcon = {Icon(painterResource(R.drawable.calender_pick),"", tint = Color.Black)},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("When does the billing start?", fontFamily = manropeBold, fontSize = 18.sp)
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
                            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp, fontFamily = manropeMedium),
                            colors = OutlinedTextFieldDefaults.colors(

                                // Background inside the text field
                                focusedContainerColor = Color(0xFFFFFFFF),
                                unfocusedContainerColor = Color(0xFFFFFFFF),

                                // Border colors
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFB0BEC5),

                                // Cursor
                                cursorColor = Color(0xFF1976D2)
                            ),
                            trailingIcon = {Icon(painterResource(R.drawable.calender_pick),"", tint = Color.Black)},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

//                    ReminderToggle(
//                        enabled = reminderEnabled,
//                        onToggle = { reminderEnabled = it }
//                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            if (buttonEnabled)
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0A3054), // blue
                                        Color(0xFF42A5F5)  // light blue
                                    )
                                ) else (Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFA1A1A1), // blue
                                    Color(0xFF7C7A7A)  // light blue
                                )
                            )),
                            RoundedCornerShape(25.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {

                    Button(
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        enabled = buttonEnabled,
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
                                    subscriptionType = subscriptionType,
                                    logoResId = serviceLogo
                                )
                                onSave(subscription)
                            }
                        }
                    ) {
                        Text("Create Subscription", color = Color.White, fontSize = 18.sp)
                    }
                }
                ServicePickerBottomSheet(
                    show = showSheet,
                    onDismiss = { showSheet = false },
                    viewModel = viewModel,
                    onSelect = { service ->
                        serviceName = service.name
                        serviceLogo = service.logo
                        selectedPackage = service.packageName
                        showSheet = false
                    }
                ) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicePickerBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: AddSubscriptionViewModel,
    onSelect: (Service) -> Unit
) {

    if (!show) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFFFFCFC)
    ) {

        ServicePickerContent(viewModel, onSelect)
    }
}
@Composable
fun ServicePickerContent(
    viewModel: AddSubscriptionViewModel,
    onSelect: (Service) -> Unit
) {

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        // 🔥 drag handle
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .background(Color.Gray.copy(0.3f), RoundedCornerShape(50))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        // 🔍 search
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchServices(it)
            },
            placeholder = { Text("Search services...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp)
        ) {

            items(viewModel.suggestions) { service ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSelect(service) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(service.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {

                            Text(
                                text = service.name,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Subscription",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            if (viewModel.suggestions.isEmpty() && query.isNotBlank()) {

                item {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                onSelect(
                                    Service(
                                        name = query,
                                        logo = -1 ,
                                        packageName = query
                                    )
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {

                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Add,
                                contentDescription = null
                            )

                            Spacer(Modifier.width(10.dp))

                            Text("Add \"$query\"")
                        }
                    }
                }
            }
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
    val focusManager = LocalFocusManager.current

     val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )

    Column {
        Text(
            text = "Price",
            fontFamily = manropeBold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DropdownField(
                label = "Currency",
                selected = currency,
                options = currencyOptions,
                modifier = Modifier.width(80.dp),
                onSelected = onCurrencySelected
            )

            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done // 👈 shows Done button
                ),

                keyboardActions = KeyboardActions(
                    onDone = {
                        // 👇 hide keyboard
                        focusManager.clearFocus()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    // Background inside the text field
                    focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFFFF),

                    // Border colors
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFB0BEC5),

                    // Cursor
                    cursorColor = Color(0xFF1976D2)
                ),
                placeholder = { Text("Ex 399.00", fontSize = 12.sp, color = colorResource(R.color.grey)) },
                shape = RoundedCornerShape(20.dp)
            )
        }
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
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
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
            ),
            shape = RoundedCornerShape(20.dp)
        )


        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach {

                DropdownMenuItem(
                    text = { Text(it, fontFamily = manropeRegular) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
