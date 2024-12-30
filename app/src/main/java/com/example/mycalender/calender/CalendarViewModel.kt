// CalendarViewModel.kt
package com.example.mycalender.calender

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycalender.NotificationReceiver
import com.example.mycalender.data.Event
import com.example.mycalender.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth


class CalendarViewModel(private val repository: EventRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _eventsForSelectedDate = MutableStateFlow<List<Event>>(emptyList())
    val eventsForSelectedDate: StateFlow<List<Event>> = _eventsForSelectedDate

    private val _currentMonthYear = MutableStateFlow(YearMonth.now())
    val currentMonthYear: StateFlow<YearMonth> = _currentMonthYear

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val allEvents: StateFlow<List<Event>> = _allEvents

    init {
        loadEventsForDate(LocalDate.now())
        loadAllEvents()
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        loadEventsForDate(date)
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _currentMonthYear.value = yearMonth
    }

    private fun loadEventsForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.getEventsByDate(date).collect { events ->
                _eventsForSelectedDate.value = events
            }
        }
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            repository.getAllEvents().collect { events ->
                _allEvents.value = events
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event)
            loadEventsForDate(_selectedDate.value)
            loadAllEvents()
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            loadEventsForDate(_selectedDate.value)
            loadAllEvents()
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
            loadEventsForDate(_selectedDate.value)
            loadAllEvents()
        }
    }

    fun setEventReminder(context: Context, eventTimeInMillis: Long, eventTitle: String, eventDescription: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("eventTitle", eventTitle)
            putExtra("eventDescription", eventDescription)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val triggerAtMillis = eventTimeInMillis - 300000 // 提前5分鐘
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
