package com.example.mycalender.calender

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth


//month title
@Composable
fun CalenderScreen(){
   val currentMonth = YearMonth.now()
   val startMonth = currentMonth.minusMonths(12)
   val endMonth = currentMonth.plusMonths(12)


    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth
    )

    HorizontalCalendar(
        state = state,
        dayContent = { day ->
            DayCell(day.date)
        },
        monthHeader = { month ->
            MonthHeader(month.yearMonth)
        },
        monthContainer = { month,content ->
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Month: ${month.yearMonth}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
                content()
            }
        }
    )
}

@Composable
fun DayCell(date: LocalDate) {
    Text(
        text = date.dayOfMonth.toString(),
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
fun MonthHeader(yearMonth: YearMonth) {
    Text(
        text = "Header for: ${yearMonth}",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(16.dp)
            .clickable {}

    )
}