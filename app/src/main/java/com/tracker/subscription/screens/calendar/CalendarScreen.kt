package com.tracker.subscription.screens.calendar

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory
import com.tracker.subscription.screens.home.DashboardUiState
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

@Composable
fun CalendarScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController
) {
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
            val eventsByDay = remember(subscriptions, year, monthIndex) {
                SubscriptionRenewalCalendar.renewalEventsForMonth(
                    subscriptions,
                    year,
                    monthIndex
                )
            }
            val cells = remember(year, monthIndex, eventsByDay) {
                buildCalendarCells(year, monthIndex, eventsByDay)
            }
            val weekdayLabels = remember { weekdayRowLabels() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(55.dp))

                Text(
                    text = "Subscription Calendar",
                    fontFamily = manropeExtraBold,
                    fontSize = 24.sp,
                    color = colorResource(R.color.dark_blue),
                    modifier = Modifier.padding(start = 25.dp)
                )
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { monthOffset-- }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = Color(0xFF1565C0)
                        )
                    }
                    Text(
                        text = monthTitle.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        fontFamily = manropeBold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A237E)
                    )
                    IconButton(onClick = { monthOffset++ }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = Color(0xFF1565C0)
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        cells,
                        key = { index, cell ->
                            when (cell) {
                                is CalendarCell.Blank -> "blank-$index"
                                is CalendarCell.Day -> "day-${cell.startOfDayMillis}"
                            }
                        }
                    ) { _, cell ->
                        when (cell) {
                            is CalendarCell.Blank -> {
                                Spacer(modifier = Modifier.aspectRatio(1f))
                            }

                            is CalendarCell.Day -> {
                                CalendarDayCell(
                                    cell = cell,
                                    isToday = isToday(year, monthIndex, cell.dayOfMonth),
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
            }

            selectedDay?.let { day ->
                AlertDialog(
                    onDismissRequest = { selectedDay = null },
                    confirmButton = {
                        TextButton(onClick = { selectedDay = null }) {
                            Text("Close")
                        }
                    },
                    title = {
                        Text(
                            text = "Renewals on ${day.dayOfMonth} ${
                                SimpleDateFormat("MMMM", Locale.getDefault())
                                    .format(visibleCal.time)
                            }",
                            fontFamily = manropeBold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            day.events.forEach { ev ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(
                                            ev.logoResId ?: R.drawable.empty
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Text(
                                        text = ev.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: CalendarCell.Day,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isToday) colorResource(R.color.lime) else Color(0xFFE0E0E0)
    val bg = if (isToday) Color(0xFFFFF877) else Color.White

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
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
                color = if (isToday) Color(0xFF1565C0) else Color(0xFF424242),
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
                        Image(
                            painter = painterResource(ev.logoResId ?: R.drawable.empty),
                            contentDescription = ev.name,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                    }
                    if (cell.events.size > 4) {
                        Text(
                            text = "+${cell.events.size - 4}",
                            fontSize = 10.sp,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
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
