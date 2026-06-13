package com.tracker.subscription.screens.addSub

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.tracker.subscription.Utility
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.Utility.getLocalizedPrice
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
import com.tracker.subscription.presentation.Widgets.SubTypeChip
import com.tracker.subscription.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    existingSubscription: Subscription? = null,
    isDarkTheme: Boolean,
    onSave: (Subscription) -> Unit,
    onBack: () -> Unit
) {
    var serviceName by remember {
        mutableStateOf(existingSubscription?.name ?: "")
    }
    val country = Locale.getDefault().country


    var price by remember {
        mutableStateOf(existingSubscription?.price?.toString() ?: "")
    }

    var billingCycle by remember {
        mutableStateOf(existingSubscription?.billingCycle ?: "Monthly")
    }

    var freeTrialPeriod by remember {
        mutableStateOf(existingSubscription?.freeTrialPeriod ?: "7 days")
    }

    var startDate by remember {
        mutableStateOf(existingSubscription?.startDate ?: System.currentTimeMillis())
    }
    var currency by remember { mutableStateOf(existingSubscription?.currency?:"₹") }

    var subscriptionType by remember { mutableStateOf(existingSubscription?.subscriptionType?: CommonOptions.subscriptionType.last().name) }

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
    var reminderDaysBefore by remember { mutableStateOf(existingSubscription?.reminderDaysBefore ?: 1) }
    var selectedPackage by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var buttonEnabled by remember { mutableStateOf(price.isNotEmpty()) }


    LaunchedEffect(existingSubscription) {

        existingSubscription?.let {
            serviceName = it.name
            price = it.price.toString()
            billingCycle = it.billingCycle
            freeTrialPeriod = it.freeTrialPeriod
            category = it.category
            startDate = it.startDate
            reminderEnabled = it.reminderEnabled
            reminderDaysBefore = it.reminderDaysBefore
            serviceLogo = it.logoResId ?: R.drawable.empty
        }
        if (existingSubscription == null) {
            showSheet = true
        } else{
            showSheet = false
        }
    }
    val service = viewModel.getServiceLogo(serviceName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ThemeColors.getBackgroundColor(isDarkTheme))
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Box{

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 50.dp)
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
                        color = ThemeColors.getHeaderColor(isDarkTheme),
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
                        text = "Service Name",
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontFamily = manropeBold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {
                        SelectedServiceCard(
                            serviceName = serviceName.ifEmpty { "Ex: Netflix" }, // default or empty state
                            category = category, // you can map this
                            logoRes = service?.logo,
                            isDarkTheme = isDarkTheme,
                            onClick = { showSheet = true }
                        )
                    }
                }


            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                PriceSection(
                    isDarkTheme = isDarkTheme,
                    price = price,
                    currency = currency,
                    currencyOptions = currencyOptions,
                    onPriceChange = { price = it
                       },
                    onCurrencySelected = { currency = it },

                )
            }
            item {
                SubTypeChip(
                    label = "Subscription Type",
                    selected = subscriptionType,
                    options = CommonOptions.subscriptionType,
                    isEmojiShow = false,
                    isDarkTheme = isDarkTheme,
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
                            isFreeTrial = false,
                        options = CommonOptions.billing,
                            isDarkTheme = isDarkTheme,
                        onSelected = { billingCycle = it }
                    )
                } else {
                        BillingChips(
                            label = "FreeTrial ends in",
                            selected = freeTrialPeriod,
                            isFreeTrial = true,
                            options = CommonOptions.freeTrial,
                            isDarkTheme = isDarkTheme,
                            onSelected = { freeTrialPeriod = it }
                            )
                    }

            }

            }
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    if ((subscriptionType == SubscriptionType.FREE_TRIAL.value && freeTrialPeriod == "custom") || (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value )){
                        val title = if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value)
                            "Subscription start date"
                        else
                            "Billing date (After free trial)"
                        Text(title, fontFamily = manropeBold, fontSize = 18.sp)
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
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ReminderToggle(
                        enabled = reminderEnabled,
                        isDarkTheme = isDarkTheme,
                        onToggle = { reminderEnabled = it }
                    )

                    AnimatedVisibility(visible = reminderEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ReminderDaysBeforePicker(
                            selectedDays = reminderDaysBefore,
                            onSelected = { reminderDaysBefore = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                ServicePickerBottomSheet(
                    show = showSheet ,
                    onDismiss = { showSheet = false },
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onSelect = { service ->
                        serviceName = service.name
                        serviceLogo = service.logo
                        selectedPackage = service.packageName
                        showSheet = false
                        category = service.category
                        key = service.key
                        getLocalizedPrice(service, currency)?.monthlyPrice?.let { prc ->
                        price = prc.toString()
                    }
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
                            nextBillingDate = calculateNextBillingDate(startDate, billingCycle, freeTrialPeriod, subscriptionType),
                            currency = currency,
                            category = category,
                            reminderEnabled = reminderEnabled,
                            reminderDaysBefore = reminderDaysBefore,
                            subscriptionType = subscriptionType,
                            logoResId = serviceLogo,
                            key = key,
                            freeTrialPeriod = freeTrialPeriod
                        )
                        onSave(subscription)
                    } else {
                        if (price.isEmpty()) {
                            Toast.makeText(context, "Please enter the Price", Toast.LENGTH_SHORT).show()
                        } else if ( serviceName.isEmpty()){
                            Toast.makeText(context, "Please select the Service", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            ) {
                Text("Save Subscription", color = ThemeColors.getTextColor(!isDarkTheme), fontSize = 20.sp, fontFamily = manropeBold)
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
    isDarkTheme: Boolean,
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
        containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
    )

    {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.95f)
        ) {
            ServicePickerContent(viewModel,isDarkTheme, onSelect, onDismiss)
        }
    }
}

@Composable
fun SelectedServiceCard(
    serviceName: String,
    category: String,
    logoRes: Int?,
    isDarkTheme: Boolean,
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
            .background(ThemeColors.getCardBackgroundColor(isDarkTheme))
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
                    .background( if (logoRes != null) ThemeColors.getBackgroundColor(isDarkTheme = isDarkTheme) else Utility.randomColor()) , // fallback bg
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
                        text = serviceName.take(1).uppercase(),
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontFamily = manropeBold,
                        fontSize = 30.sp
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
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(ThemeColors.getBackgroundColor(isDarkTheme = isDarkTheme)).padding(vertical = 4.dp, horizontal = 8.dp).clickable { onClick() }
            )
        }
    }
}

@Composable
fun ServicePickerContent(
    viewModel: AddSubscriptionViewModel,
    isDarkTheme: Boolean,
    onSelect: (Service) -> Unit,
    onDismiss: () -> Unit,
    ) {
    var query by remember { mutableStateOf("") }

    var showCreateSheet by remember {
        mutableStateOf(false)
    }
    var currentScreen by remember {
        mutableStateOf(ServiceSheetScreen.LIST)
    }


                ServiceListContent(
                viewModel = viewModel,
                currentScreen,
                initialName = query,
                    isDarkTheme = isDarkTheme,
                onCreateService = {
                    query = it
                    currentScreen = ServiceSheetScreen.CREATE
                },
                onSelect = onSelect,
                onDismiss = {
                    showCreateSheet = false
                    onDismiss()
                },
                onSave = { service ->

                    showCreateSheet = false

                    onSelect(service)

                    onDismiss()
                }

            )

}


@Composable
fun ServiceListContent (
    viewModel: AddSubscriptionViewModel,
    currentScreen: ServiceSheetScreen,
    initialName: String,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Service) -> Unit,
    onSave: (Service) -> Unit,
    onCreateService: (String) -> Unit,
){
    var selectedCategory by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }

    val categories = listOf(
        "All", "OTT", "Music", "Productivity", "Shopping", "Fitness", "AI"
    )
    val manropeRegular = FontFamily( Font(R.font.manrope_semi_bold) )
    val manropesemiBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val haptic = LocalHapticFeedback.current
    var initialSearhed =  initialName

    var serviceName by remember {
        mutableStateOf(initialName)
    }

    var category by remember {
        mutableStateOf("Entertainment")
    }

    var colorCode by remember {
        mutableStateOf("Entertainment")
    }

    val colors = listOf(
        Color(0xFFEF5350),
        Color(0xFFAB47BC),
        Color(0xFF42A5F5),
        Color(0xFF26A69A),
        Color(0xFFFFA726)
    )

    val randomColor = remember {
        colors.random()
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(500)
            ) + scaleIn(
                initialScale = 0.8f
            ) togetherWith
                    fadeOut(
                        animationSpec = tween(500)
                    )
        },
        label = "sheet_transition"
    ) { currentScreen ->

        when(currentScreen) {
            ServiceSheetScreen.LIST -> {
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
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
                                        label = { Text(category, fontFamily = manropesemiBold) },
                                        shape = RoundedCornerShape(50), // 👈 MORE ROUNDED
                                        modifier = Modifier.padding(end = 15.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ThemeColors.getOrangeColor(isDarkTheme),
                                            selectedLabelColor = ThemeColors.getTextColor(isDarkTheme)
                                        ),
                                        border = BorderStroke(0.7.dp, Color(0xFFD9D8D8))
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {

                                // ✅ SERVICES AVAILABLE
                                if (viewModel.suggestions.isNotEmpty()) {

                                    items(viewModel.suggestions) { service ->

                                        Card(
                                            onClick = {
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.VirtualKey
                                                )

                                                onSelect(service)
                                            },
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 4.dp
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {

                                                Box(
                                                    modifier = Modifier
                                                        .size(45.dp)
                                                        .clip(CircleShape)
                                                        .background(ThemeColors.getCardBackgroundColor(isDarkTheme)),
                                                    contentAlignment = Alignment.Center
                                                ) {

                                                    Image(
                                                        painter = painterResource(service.logo),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(45.dp)
                                                    )
                                                }

                                                Spacer(Modifier.height(8.dp))

                                                Text(
                                                    text = service.name,
                                                    maxLines = 2,
                                                    minLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontFamily = manropeRegular,
                                                    fontSize = 10.sp,
                                                    color = ThemeColors.getTextColor(isDarkTheme),
                                                    modifier = Modifier.padding(horizontal = 5.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                // ❌ NO RESULT FOUND
                                else {

                                    item(span = {
                                        GridItemSpan(maxLineSpan)
                                    }) {

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {

                                            Icon(
                                                painterResource(R.drawable.highest),
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                tint = Color.Unspecified
                                            )

                                            Spacer(Modifier.height(16.dp))

                                            Text(
                                                text = "No Result Found",
                                                fontFamily = manropeExtraBold,
                                                fontSize = 14.sp
                                            )

                                            Spacer(Modifier.height(8.dp))

                                            Text(
                                                text = "Create your own custom service",
                                                color = Color.Gray,
                                                fontFamily = manropeRegular
                                            )

                                            Spacer(Modifier.height(24.dp))

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                Color(0xFF2979FF),
                                                                Color(0xFF2979FF)
                                                            )
                                                        ),
                                                        RoundedCornerShape(25.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ){
                                                Button(
                                                    onClick = {

                                                        serviceName = query
                                                        onCreateService(query)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.Transparent
                                                    )) {
                                                    Text("Create Service", fontFamily = manropeBold, fontSize = 18.sp)
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
            ServiceSheetScreen.CREATE -> {

                val manropeBold = FontFamily( Font(R.font.manrope_bold) )
                val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
                val manropeMedium = FontFamily( Font(R.font.manrope_medium) )

                Column(
                    modifier = Modifier
                        .padding(30.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .border(0.5.dp, colorResource(R.color.blue_bg_light), RoundedCornerShape(25.dp))
                        .fillMaxWidth()
                       ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Create Service",
                        fontSize = 22.sp,
                        fontFamily = manropeBold
                    )

                    Spacer(Modifier.height(15.dp))

                    // LOGO
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(randomColor),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = initialName
                                .firstOrNull()
                                ?.uppercase() ?: "S",
                            color = ThemeColors.getTextColor(isDarkTheme),
                            fontSize = 34.sp,
                            fontFamily = manropeExtraBold
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // SERVICE NAME
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = {
                            serviceName = it
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        label = {
                            Text("Service Name", fontFamily = manropeMedium, fontSize = 14.sp)
                        },
                        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    )

                    Spacer(Modifier.height(16.dp))

                    // CATEGORY
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        label = {
                            Text("Category", fontFamily = manropeMedium, fontSize = 14.sp)
                        },
                        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    )

                    Spacer(Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .padding(start = 50.dp, end = 50.dp)
                            .height(56.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF2979FF),
                                        Color(0xFF2979FF)
                                    )
                                ),
                                RoundedCornerShape(25.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        Button(
                            onClick = {

                                val service = Service(
                                    name = serviceName,
                                    logo = -1,
                                    packageName = "",
                                    category = category,
                                    key = serviceName,
                                    prices = emptyList()
                                )

                                onSave(service)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Text("Save", fontSize = 20.sp, fontFamily = manropeBold )
                        }
                    }



                    Spacer(Modifier.height(20.dp))
                }
            }
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
                        key = "key",
                        prices = emptyList()
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceBottomSheet(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (Service) -> Unit
) {



    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        )
    ) {

    }
}


@Composable
fun PriceSection(
    isDarkTheme: Boolean,
    price: String,
    currency: String,
    currencyOptions: List<String>,
    onPriceChange: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
) {

     val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

//    LaunchedEffect(requestFocus) {
//        if (requestFocus) {
//            focusRequester.requestFocus()
//        }
//    }
    Column {
        Text(
            text = "Price & Currency",
            fontFamily = manropeBold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.clip(
                RoundedCornerShape(25.dp)
            ).border(1.dp, ThemeColors.getBackgroundColor(isDarkTheme), RoundedCornerShape(25.dp))
        ) {
            DropdownField(
                label = "Currency",
                selected = currency,
                options = currencyOptions,
                modifier = Modifier.width(95.dp),
                onSelected = onCurrencySelected,
                isDarkTheme = isDarkTheme
            )

            TextField(
                value = price,
                onValueChange = onPriceChange,
                modifier = Modifier
                    .weight(0.6f)
                    .focusRequester(focusRequester),  // 👈 attach
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    // Background inside the text field
                    focusedContainerColor = ThemeColors.getCardBackgroundColor(isDarkTheme),
                    unfocusedContainerColor = ThemeColors.getCardBackgroundColor(isDarkTheme),

                    // Border colors
                    focusedBorderColor = ThemeColors.getLightGreyColor(isDarkTheme),
                    unfocusedBorderColor = ThemeColors.getLightGreyColor(isDarkTheme),

                    // Cursor
                    cursorColor = ThemeColors.getTextColor(isDarkTheme)
                ),
                textStyle = TextStyle(fontFamily = manropeExtraBold, fontSize = 25.sp),
                placeholder = { Text("Ex 399.00", fontSize = 25.sp, fontFamily = manropeExtraBold, color = colorResource(R.color.grey)) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

}

@Composable
fun ReminderToggle(
    enabled: Boolean,
    isDarkTheme: Boolean,
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
                checkedThumbColor = ThemeColors.getBackgroundColor(isDarkTheme),
                checkedTrackColor = Color(0xFF1976D2),

                // When switch is OFF
                uncheckedThumbColor = ThemeColors.getBackgroundColor(isDarkTheme),
                uncheckedTrackColor = ThemeColors.getLightGreyColor(isDarkTheme),
                uncheckedBorderColor = ThemeColors.getBackgroundColor(isDarkTheme)
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
private fun ReminderDaysBeforePicker(
    selectedDays: Int,
    onSelected: (Int) -> Unit
) {
    val options = remember { listOf(1, 2, 3, 5, 7) }
    val selectedIndex = options.indexOf(selectedDays).takeIf { it >= 0 } ?: 0

    val manropeRegular = FontFamily(Font(R.font.manrope_semi_bold))
    val manropeBold = FontFamily(Font(R.font.manrope_bold))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 12.dp) // align under text (icon on the left)
    ) {
        Text(
            text = "Notify me ${options[selectedIndex]} days before",
            fontFamily = manropeBold,
            fontSize = 15.sp,
            color = Color(0xFF263238)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { raw ->
                val idx = raw.toInt().coerceIn(0, options.lastIndex)
                onSelected(options[idx])
            },
            valueRange = 0f..options.lastIndex.toFloat(),
            steps = (options.size - 2).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFFFFD600),
                activeTickColor = Color.Black,
                inactiveTickColor = colorResource(R.color.dark_grey),
                inactiveTrackColor = colorResource(R.color.grey),
                thumbColor = Color(0xFFFFD600)
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            options.forEach { d ->
                Text(
                    text = "${d}d",
                    fontFamily = manropeRegular,
                    fontSize = 11.sp,
                    color = if (d == options[selectedIndex]) Color(0xFF1976D2) else Color.Gray
                )
            }
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
    onSelected: (String) -> Unit,
    isDarkTheme: Boolean
) {

    var expanded by remember { mutableStateOf(false) }
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

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
                focusedContainerColor = ThemeColors.getCardBackgroundColor(isDarkTheme),
                unfocusedContainerColor = ThemeColors.getCardBackgroundColor(isDarkTheme),

                // Border colors
                focusedBorderColor = ThemeColors.getDarkGreyColor(isDarkTheme),
                unfocusedBorderColor = ThemeColors.getLightGreyColor(isDarkTheme),

                // Cursor
                cursorColor = Color(0xFF1976D2)
            ),
            textStyle = TextStyle(fontFamily = manropeExtraBold, fontSize = 25.sp),
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

@Composable
fun CreateServiceDialog(
    initialName: String,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onCreate: (Service) -> Unit
) {

    var serviceName by remember {
        mutableStateOf(initialName)
    }

    var category by remember {
        mutableStateOf("Entertainment")
    }

    val randomColor = remember {

        listOf(
            Color(0xFFEF5350),
            Color(0xFFAB47BC),
            Color(0xFF42A5F5),
            Color(0xFF26A69A),
            Color(0xFFFFA726)
        ).random()
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            Button(
                onClick = {

                    val service = Service(
                        key = serviceName,
                        name = serviceName,
                        category = category,
                        packageName = "",
                        prices = emptyList(),
                        logo = 0
                    )

                    onCreate(service)
                }
            ) {
                Text("Create")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },

        title = {
            Text("Create Service")
        },

        text = {

            Column {

                // LETTER ICON
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(randomColor),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = serviceName
                            .firstOrNull()
                            ?.uppercase() ?: "?",
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = serviceName,
                    onValueChange = {
                        serviceName = it
                    },
                    label = {
                        Text("Service Name")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = {
                        Text("Category")
                    }
                )
            }
        }
    )
}
