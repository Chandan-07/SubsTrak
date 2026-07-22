package com.tracker.subscription.screens.calendar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import java.util.Date
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.Utility.formatCurrency
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory
import com.tracker.subscription.screens.home.DashboardUiState
import com.tracker.subscription.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private sealed class CalendarCell {
    data object Blank : CalendarCell()
    data class Day(
        val dayOfMonth: Int,
        val startOfDayMillis: Long,
        val events: List<SubscriptionRenewalCalendar.DayEvent>
    ) : CalendarCell()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    isDarkTheme: Boolean
) {

    val colors = listOf(
        Color(0xFFCE93D8),
    )
    val randomColor = colors.random()
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
    val manropeBold = FontFamily(Font(R.font.manrope_bold))
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )

    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableStateOf<CalendarCell.Day?>(null) }

    val visibleCal = remember(monthOffset) {
        Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
    }
    val sheetState = rememberModalBottomSheetState()

    val year = visibleCal.get(Calendar.YEAR)
    val monthIndex = visibleCal.get(Calendar.MONTH)

    val monthTitle = remember(year, monthIndex) {
        SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(visibleCal.time)
    }

    when (state) {
        DashboardUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DashboardUiState.Success -> {
            val subscriptions = (state as DashboardUiState.Success).data.subscriptions
            val upcomingSubs = remember(subscriptions) {
                subscriptions
                    .filter { it.nextBillingDate >= System.currentTimeMillis() }
                    .sortedBy { it.nextBillingDate }
            }
            val eventsByDay = remember(
                subscriptions,
                year,
                monthIndex
            ) {
                SubscriptionRenewalCalendar
                    .renewalEventsForMonth(
                        subscriptions,
                        year,
                        monthIndex
                    )
            }
            val cells = remember(year, monthIndex, eventsByDay) {
                buildCalendarCells(year, monthIndex, eventsByDay)
            }
            val weekdayLabels = remember { weekdayRowLabels() }


            val monthEvents = eventsByDay.values.flatten()

            val totalRenewals = monthEvents.size

            val totalAmount = monthEvents.sumOf {
                it.price
            }

            val highestSubscription = monthEvents.maxByOrNull {
                it.price
            }
            Column {
                Column {
                    Spacer(modifier = Modifier.height(55.dp))

                    Text(
                        text = "My Calendar",
                        fontFamily = manropeExtraBold,
                        fontSize = 24.sp,
                        color = ThemeColors.getHeaderColor(isDarkTheme),
                        modifier = Modifier.padding(start = 25.dp)
                    )

                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    item {
                        Column(modifier = Modifier.clip(RoundedCornerShape(15.dp))
                            .background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                            .border(1.dp, ThemeColors.getLightGreyColor(isDarkTheme), RoundedCornerShape(15.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { monthOffset-- }) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous month",
                                        tint = ThemeColors.getHeaderBlueColor(isDarkTheme)
                                    )
                                }
                                Text(
                                    text = monthTitle.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    },
                                    fontFamily = manropeBold,
                                    fontSize = 18.sp,
                                    color = ThemeColors.getHeaderBlueColor(isDarkTheme)
                                )
                                IconButton(onClick = { monthOffset++ }) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Next month",
                                        tint = ThemeColors.getHeaderBlueColor(isDarkTheme)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                weekdayLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            cells.chunked(7).forEach { weekCells ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    weekCells.forEach { cell ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            when (cell) {
                                                is CalendarCell.Blank -> {
                                                    Spacer(modifier = Modifier.aspectRatio(1f))
                                                }

                                                is CalendarCell.Day -> {
                                                    CalendarDayCell(
                                                        cell = cell,
                                                        isToday = isToday(year, monthIndex, cell.dayOfMonth),
                                                        isDarkTheme = isDarkTheme,
                                                        onClick = {
                                                            if (cell.events.isNotEmpty()) {
                                                                selectedDay = cell
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (weekCells.size < 7) {
                                        Spacer(modifier = Modifier.weight((7 - weekCells.size).toFloat()))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                    }
                    item {
                        if (subscriptions.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Monthly Insights",
                                    fontSize = 20.sp,
                                    fontFamily = manropeBold,
                                    color = ThemeColors.getHeaderColor(isDarkTheme)
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 2.dp
                                    )
                                ) {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {

                                        InsightItem(
                                            isDarkTheme,
                                            modifier = Modifier.weight(1f),
                                            emoji = R.drawable.timer,
                                            title = "Renewals",
                                            value = totalRenewals.toString()
                                        )

                                        InsightItem(
                                            isDarkTheme,
                                            modifier = Modifier.weight(1f),
                                            emoji = R.drawable.money,
                                            title = "Total",
                                            value = formatCurrency(totalAmount, highestSubscription?.currency?:"$")
                                        )

                                        InsightItem(
                                            isDarkTheme,
                                            modifier = Modifier.weight(1f),
                                            emoji = R.drawable.crown,
                                            title = "Highest",
                                            value = highestSubscription?.name.toString()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        if (upcomingSubs.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Upcoming Renewals",
                                    fontSize = 20.sp,
                                    fontFamily = manropeBold,
                                    color = ThemeColors.getHeaderColor(isDarkTheme)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                                        .border(1.dp, ThemeColors.getLightGreyColor(isDarkTheme), RoundedCornerShape(15.dp))
                                ) {
                                    // Header Row
                                    HorizontalDivider(
                                        color = ThemeColors.getLightGreyColor(isDarkTheme),
                                        thickness = 1.dp
                                    )

                                    upcomingSubs.forEachIndexed { index, sub ->
                                        val fallbackColor = remember(sub.key, sub.name) {
                                            val colors = listOf(
                                                Color(0xFF3D5AFE), Color(0xFF00C853), Color(0xFFFF6D00),
                                                Color(0xFFD500F9), Color(0xFFFF4081), Color(0xFF00BFA5), Color(0xFFFFAB00)
                                            )
                                            val seed = sub.key.ifBlank { sub.name }
                                            colors[Math.abs(seed.hashCode()) % colors.size]
                                        }

                                        val formattedDate = remember(sub.nextBillingDate) {
                                            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sub.nextBillingDate))
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Service Logo & Name
                                            Row(
                                                modifier = Modifier.weight(1.5f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val service = viewModel.getServiceByKey(sub.key)
                                                val logoId = service?.logo ?: sub.logoResId
                                                val painter = runCatching {
                                                    if (logoId != null && logoId != 0 && logoId != R.drawable.empty) {
                                                        painterResource(id = logoId)
                                                    } else null
                                                }.getOrNull()

                                                if (painter != null) {
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = sub.name,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFF3F3F3)),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(fallbackColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = sub.name.firstOrNull()?.uppercase() ?: "",
                                                            color = Color.White,
                                                            fontSize = 12.sp,
                                                            fontFamily = manropeBold,
                                                            style = TextStyle(
                                                                platformStyle = PlatformTextStyle(
                                                                    includeFontPadding = false
                                                                )
                                                            ),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Text(
                                                    text = sub.name,
                                                    fontFamily = manropeBold,
                                                    fontSize = 14.sp,
                                                    color = ThemeColors.getTextColor(isDarkTheme),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Date
                                            Text(
                                                text = formattedDate,
                                                fontFamily = manropeBold,
                                                fontSize = 13.sp,
                                                color = ThemeColors.getDarkGreyColor(isDarkTheme),
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Price
                                            Text(
                                                text = formatCurrency(sub.price, sub.currency),
                                                fontFamily = manropeExtraBold,
                                                fontSize = 14.sp,
                                                color = ThemeColors.getTextColor(isDarkTheme),
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (index < upcomingSubs.lastIndex) {
                                            HorizontalDivider(
                                                color = ThemeColors.getLightGreyColor(isDarkTheme),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }



                }

            }

            selectedDay?.let { day ->

                    ModalBottomSheet(
                        onDismissRequest = {
                            selectedDay = null
                        },
                        sheetState = sheetState,
                        containerColor = ThemeColors.getBackgroundColor(isDarkTheme)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {

                            Text(
                                text = "Renewals on ${day.dayOfMonth} May",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "${day.events.size} renewals",
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            day.events.forEach { ev ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val service = viewModel.getServiceByKey(ev.key)
                                    val logoId = service?.logo ?: ev.logoResId
                                    val isDrawable = logoId != null &&
                                            logoId != 0 &&
                                            logoId != -1 &&
                                            logoId != R.drawable.empty &&
                                            runCatching {
                                                context.resources.getResourceTypeName(logoId)
                                            }.getOrNull() == "drawable"

                                    if (isDrawable) {
                                        val painter = runCatching {
                                            painterResource(id = logoId)
                                        }.getOrNull()
                                        if (painter != null) {
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(randomColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ev.name.firstOrNull()?.uppercase() ?: "",
                                                    color = ThemeColors.getTextColor(isDarkTheme),
                                                    fontSize = 20.sp,
                                                    fontFamily = manropeBold
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(randomColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ev.name.firstOrNull()?.uppercase() ?: "",
                                                color = ThemeColors.getTextColor(isDarkTheme),
                                                fontSize = 20.sp,
                                                fontFamily = manropeBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text = ev.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )

                                        Text(
                                            text = "Recurring payment",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Text(
                                        text = formatCurrency(ev.price, ev.currency),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                }
            }
        }
    }
}
@Composable
fun InsightItem(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    emoji: Int,
    title: String,
    value: String,
) {

    val manropeMedium =
        FontFamily(Font(R.font.manrope_medium))

    val manropeExtraBold =
        FontFamily(Font(R.font.manrope_extra_bold))

    Column(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .padding(vertical = 14.dp, horizontal = 10.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            painter = painterResource(emoji),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray,
            fontFamily = manropeMedium,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            color = ThemeColors.getTextColor(isDarkTheme),
            fontFamily = manropeExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell.Day,
    isToday: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val hasEvents = cell.events.isNotEmpty()

    val bg = when {
        isToday -> if (isDarkTheme) Color(0xFF495067) else  Color(0xFFCBD5FD)
        hasEvents -> if (isDarkTheme)  Color(0xFF624E3B) else  Color(0xFFFFF0D6)
        else -> ThemeColors.getBackgroundColor(isDarkTheme)
    }

    val borderColor = when {
        isToday -> if (isDarkTheme) Color(0xFF64B5F6)  else Color(0xFF1565C0)
        hasEvents -> if (isDarkTheme) Color(0xFF8A784A) else  Color(0xFFFFD54F)
        else -> if (isDarkTheme) Color(0xFF424242) else Color(0xFFE0E0E0)
    }

    val colors = listOf(
        Color(0xFFCE93D8),
        Color(0xFFFFAB40),
        Color(0xFF536DFE),
        Color(0xFFFF5252),
    )
    val randomColor = colors.random()
    val manropeBold = FontFamily(Font(R.font.manrope_bold))
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cell.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) ThemeColors.getTextColor(isDarkTheme) else ThemeColors.getTextGreyColor(isDarkTheme),
                modifier = Modifier.align(Alignment.Start)
            )
            if (cell.events.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val show = cell.events.take(4)
                    show.forEach { ev ->

                        Log.d("KUSHGDF", "CalendarDayCell: ${ev.logoResId}")

                        val isDrawable = ev.logoResId != null &&
                                ev.logoResId != -1 &&
                                runCatching {
                                    context.resources.getResourceTypeName(ev.logoResId)
                                }.getOrNull() == "drawable"

                        if (isDrawable) {

                            val painter = runCatching {
                                painterResource(id = ev.logoResId!!)
                            }.getOrNull()

                            if (painter != null) {

                                Icon(
                                    painter = painter,
                                    contentDescription = ev.name,
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                )

                            } else {

                                FallbackAvatar(
                                    name = ev.name,
                                    randomColor = randomColor,
                                    manropeBold = manropeBold,
                                    isDarkTheme= isDarkTheme
                                )
                            }

                        } else {

                            FallbackAvatar(
                                name = ev.name,
                                randomColor = randomColor,
                                manropeBold = manropeBold,
                                isDarkTheme= isDarkTheme
                            )
                        }
                    }
                    if (cell.events.size > 4) {
                        Text(
                            text = "+${cell.events.size - 4}",
                            fontSize = 12.sp,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun FallbackAvatar(
    name: String,
    randomColor: Color,
    manropeBold: FontFamily,
    isDarkTheme: Boolean
) {

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(15.dp)
            .background(randomColor),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = name.firstOrNull()?.uppercase() ?: "",
            color = Color.White,
            fontSize = 8.sp,
            fontFamily = manropeBold,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeight = 8.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
private fun buildCalendarCells(
    year: Int,
    monthIndex: Int,
    eventsByDay: Map<Long, List<SubscriptionRenewalCalendar.DayEvent>>
): List<CalendarCell> {
    val first = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
        clearTime()
    }
    val offset = (first.get(Calendar.DAY_OF_WEEK) - first.firstDayOfWeek + 7) % 7
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    return buildList {
        repeat(offset) { add(CalendarCell.Blank) }
        for (day in 1..daysInMonth) {
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, day)
                clearTime()
            }
            val ms = dayCal.timeInMillis
            add(
                CalendarCell.Day(
                    dayOfMonth = day,
                    startOfDayMillis = ms,
                    events = eventsByDay[ms].orEmpty()
                )
            )
        }
        while (size % 7 != 0) {
            add(CalendarCell.Blank)
        }
    }
}

private fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun isToday(year: Int, monthIndex: Int, day: Int): Boolean {
    val now = Calendar.getInstance()
    return now.get(Calendar.YEAR) == year &&
        now.get(Calendar.MONTH) == monthIndex &&
        now.get(Calendar.DAY_OF_MONTH) == day
}

private fun weekdayRowLabels(): List<String> {
    val cal = Calendar.getInstance()
    val out = mutableListOf<String>()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    repeat(7) {
        out.add(
            cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
                ?: ""
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return out
}


