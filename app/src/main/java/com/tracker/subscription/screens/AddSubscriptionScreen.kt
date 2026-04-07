package com.tracker.subscription.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.subscription.R
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.AddSubscriptionViewModel
import com.tracker.subscription.presentation.AddSubscriptionViewModelFactory
import com.tracker.subscription.presentation.CommonOptions
import com.tracker.subscription.presentation.Widgets.BillingChips
import com.tracker.subscription.presentation.Widgets.SingleSelectChips
import com.tracker.subscription.presentation.Widgets.SubTypeChip
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
    var key by remember { mutableStateOf("key") }


    val context = LocalContext.current

    val db = DatabaseProvider.getDatabase(context)


    val smsDataSource = SmsDataSource(context)
    val repository = remember {
        SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
    }

    val viewModel: AddSubscriptionViewModel = viewModel(
        factory = AddSubscriptionViewModelFactory(repository)
    )
    var reminderEnabled by remember { mutableStateOf(existingSubscription?.reminderEnabled ?: false) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(true) }
    var buttonEnabled by remember { mutableStateOf(price.isNotEmpty()) }

    LaunchedEffect(existingSubscription) {

        existingSubscription?.let {
            serviceName = it.name
            price = it.price.toString()
            billingCycle = it.billingCycle
            category = it.category
            startDate = it.startDate
            reminderEnabled = it.reminderEnabled
            serviceLogo = it.logoResId ?: R.drawable.empty
        }
    }
    val service = viewModel.getServiceLogo(serviceName)

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
        val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
        val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

        val interactionSource = remember { MutableInteractionSource() }
        LazyColumn (
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Column {
                    Text(
                        text = "Select your Service",
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontFamily = manropeExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSheet = true }
                    ) {
                        SelectedServiceCard(
                            serviceName = serviceName.ifEmpty { "Ex: Netflix" }, // default or empty state
                            category = category, // you can map this
                            logoRes = service?.logo,
                            onClick = { showSheet = true }
                        )
                    }
                }


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
            item {
                SubTypeChip(
                    label = "Subscription Type",
                    selected = subscriptionType,
                    options = CommonOptions.subscriptionType,
                    isEmojiShow = false,
                    onSelected = { subscriptionType = it }
                )
            }
            item {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value) {
                        BillingChips(
                        label = "Billing Cycle",
                        selected = billingCycle,
                        isEmojiShow = false,
                        options = CommonOptions.billing,
                        onSelected = { billingCycle = it }
                    )
                }

            }

            }
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val title = if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value)
                        "Subscription start date"
                    else
                        "Billing start date (after free trial)"
                    Text(title, fontFamily = manropeExtraBold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
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

//                    Text("When does the billing start?", fontFamily = manropeBold, fontSize = 18.sp)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { showDatePicker = true }
//                    ) {
//                        OutlinedTextField(
//                            value = startDate?.let { formatDate(it) } ?: "",
//                            onValueChange = {},
//                            readOnly = true,
//                            enabled = false,
//                            textStyle = TextStyle(color = Color.Black, fontSize = 16.sp, fontFamily = manropeMedium),
//                            colors = OutlinedTextFieldDefaults.colors(
//
//                                // Background inside the text field
//                                focusedContainerColor = Color(0xFFFFFFFF),
//                                unfocusedContainerColor = Color(0xFFFFFFFF),
//
//                                // Border colors
//                                focusedBorderColor = Color(0xFF1976D2),
//                                unfocusedBorderColor = Color(0xFFB0BEC5),
//
//                                // Cursor
//                                cursorColor = Color(0xFF1976D2)
//                            ),
//                            trailingIcon = {Icon(painterResource(R.drawable.calender_pick),"", tint = Color.Black)},
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(20.dp)
//                        )
//                    }

                    ReminderToggle(
                        enabled = reminderEnabled,
                        onToggle = { reminderEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                ServicePickerBottomSheet(
                    show = showSheet,
                    onDismiss = { showSheet = false },
                    viewModel = viewModel,
                    onSelect = { service ->
                        serviceName = service.name
                        serviceLogo = service.logo
                        selectedPackage = service.packageName
                        showSheet = false
                        category = service.category
                        key = service.key
                    }
                ) }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF2979FF), // blue
                            Color(0xFF2979FF)  // light blue
                        )
                    ),
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
                            logoResId = serviceLogo,
                            key = key
                        )
                        onSave(subscription)
                    }
                }
            ) {
                Text("Save Subscription", color = Color.White, fontSize = 20.sp, fontFamily = manropeBold)
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

@OptIn(ExperimentalMaterial3Api::class) @Composable
fun ServicePickerBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    viewModel: AddSubscriptionViewModel,
    onSelect: (Service) -> Unit ) {
    if (!show)
        return
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden // ❌ block swipe dismiss
        }
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFFFFCFC)
    )
    {
        ServicePickerContent(viewModel, onSelect, onDismiss)
    }
}

@Composable
fun SelectedServiceCard(
    serviceName: String,
    category: String,
    logoRes: Int?,
    onClick: () -> Unit
) {
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE9EFFC))
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔴 Logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background( if (logoRes != null) Color.White else Color.Gray) , // fallback bg
                contentAlignment = Alignment.Center
            ) {
                if (logoRes != null) {
                    Image(
                        painter = painterResource(logoRes),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    Text(
                        text = serviceName.take(1),
                        color = Color.White,
                        fontFamily = manropeBold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 📝 Name + Category
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = serviceName.ifEmpty { "Select Service" },
                    fontSize = 16.sp,
                    fontFamily = manropeExtraBold
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = category,
                    color = Color.Gray,
                    fontFamily = manropeRegular,
                    fontSize = 12.sp
                )
            }

            // 🔵 Change Button
            Text(
                text = "Change",
                color = Color(0xFF2979FF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(vertical = 4.dp, horizontal = 8.dp).clickable { onClick() }
            )
        }
    }
}

@Composable
fun ServicePickerContent(
    viewModel: AddSubscriptionViewModel,
    onSelect: (Service) -> Unit,
    onDismiss: () -> Unit,
    ) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All", "OTT", "Music", "Productivity", "Shopping", "Fitness", "AI"
    )
    val manropeRegular = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropesemiBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.96f)
            .padding(start = 18.dp, end = 18.dp)
    ) {

        // 🔝 Header with close
        Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(18.dp)
    ) {

//        // Drag handle
//        Box(
//            modifier = Modifier
//                .size(width = 40.dp, height = 4.dp)
//                .background(Color.Gray.copy(0.3f), RoundedCornerShape(50))
//                .align(Alignment.CenterHorizontally)
//        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Select your Service ✨",
            fontFamily = manropeExtraBold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Spacer(Modifier.height(20.dp))

        // 🔍 Search
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchServices(it)
            },
            placeholder = { Text("Search services...", fontFamily = manropeRegular) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // 🧩 Category chips
        LazyRow {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                        viewModel.filterByCategory(category)
                    },
                    label = { Text(category, fontFamily = manropesemiBold) },
                    shape = RoundedCornerShape(50), // 👈 MORE ROUNDED
                    modifier = Modifier.padding(end = 15.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3D5AFE),
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(0.7.dp, Color(0xFFD9D8D8))
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔥 GRID (key improvement)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            items(viewModel.suggestions) { service ->

                Card(
                    onClick = { onSelect(service) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.light_grey)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth() // 👈 IMPORTANT
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // 👈 CENTER
                    ) {

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(service.logo),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = service.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = manropeRegular,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center // 👈 CENTER TEXT
                        )
                    }
                }
            }


            // ➕ Add Custom Service (always visible)
            item {
                AddCustomServiceCard(query, onSelect)
            }
        } }
        }
    }
}

@Composable
fun AddCustomServiceCard(
    query: String,
    onSelect: (Service) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onSelect(
                    Service(
                        name = if (query.isBlank()) "Custom Service" else query,
                        logo = -1,
                        packageName = query,
                        category = "Custom",
                        key = "key"
                    )
                )
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(65.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = if (query.isBlank()) "Add Custom" else "Add \"$query\"",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ReminderToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {

    val context = LocalContext.current

    val manropeRegular = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropesemiBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row {
            Icon(painterResource(R.drawable.notification),"", tint = Color.Unspecified)
            Column (modifier = Modifier.padding(start = 10.dp), verticalArrangement = Arrangement.Center){

                Text("Enable Reminder ", fontFamily = manropesemiBold, fontSize = 14.sp)

                Spacer(Modifier.height(2.dp))
                Text(
                    "Get notified before renewal",
                    fontFamily = manropeRegular,
                    fontSize = 12.sp
                )
            }
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
                uncheckedTrackColor = Color(0xFFCFD8DC),
                uncheckedBorderColor = Color(0xFFA3CEFC)
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
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

    Column {
        Text(
            text = "Price & Currency",
            fontFamily = manropeExtraBold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
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
