// CalendarScreen.kt
@file:OptIn(ExperimentalFoundationApi::class)
package com.example.mycalender.calender

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.example.mycalender.data.Event
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape


@Composable
fun CalendarScreen(viewModel: CalendarViewModel, onEditEvent: (Event) -> Unit) {
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusMonths(12)
    val endMonth = currentMonth.plusMonths(12)

    val selectedDate by viewModel.selectedDate.collectAsState()
    val eventsForSelectedDate by viewModel.eventsForSelectedDate.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEventDetailDialog by remember { mutableStateOf(false) }
    var eventSelected by remember { mutableStateOf<Event?>(null) }

    Column {
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val hasEvents = allEvents.any { it.date == day.date }
                DayCell(
                    date = day.date,
                    hasEvents = hasEvents
                ) {
                    viewModel.onDateSelected(day.date)
                }
            },
            monthContainer = { month, content ->
                Column(modifier = Modifier.padding(8.dp)) {
                    val monthName = "${month.yearMonth.month.name.capitalize(Locale.ROOT)} ${month.yearMonth.year}"
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(8.dp)
                    )
                    content()
                }
            }
        )

        Text(
            text = "Events for ${selectedDate}:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        if (eventsForSelectedDate.isEmpty()) {
            Text(
                text = "No itinerary planned today",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(eventsForSelectedDate) { event ->
                    Text(
                        text = "${event.startTime} - ${event.endTime}: ${event.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .clickable {
                                showEventDetailDialog = true
                                eventSelected = event
                            }
                    )
                }
            }
        }

        if (showDeleteDialog && eventSelected != null) {
            DeleteEventDialog(
                eventTitle = eventSelected!!.title,
                onConfirmDelete = {
                    viewModel.deleteEvent(eventSelected!!)
                    showDeleteDialog = false
                },
                onCancel = {
                    showDeleteDialog = false
                }
            )
        }

        if (showEventDetailDialog && eventSelected != null) {
            EventDetailDialog(
                event = eventSelected!!,
                onClose = { showEventDetailDialog = false },
                onEdit = {
                    showEventDetailDialog = false
                    onEditEvent(eventSelected!!)
                }
            )
        }
    }
}

@Composable
fun DayCell(date: LocalDate, hasEvents: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (hasEvents) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
        )
        if (hasEvents) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
        }
    }
}
