package com.example.mycalender.calender

import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.mycalender.ReminderReceiver
import com.example.mycalender.data.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.temporal.ChronoUnit

@Composable
fun AddEventScreen(
    onEventSaved: () -> Unit,
    onCancel: () -> Unit = {},
    saveEventToDatabase: (Event) -> Unit,
    existingEvent: Event? = null
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    val gmtPlus8Zone = ZoneId.of("Asia/Singapore")

    var eventName by remember { mutableStateOf(existingEvent?.title ?: "") }
    var eventNote by remember { mutableStateOf(existingEvent?.note ?: "") }
    var selectedDate by remember { mutableStateOf(existingEvent?.date ?: LocalDate.now()) }
    var startTime by remember { mutableStateOf(existingEvent?.startTime ?: LocalTime.now()) }
    var endTime by remember { mutableStateOf(existingEvent?.endTime ?: LocalTime.now().plusHours(1)) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    val reminderOptions = listOf("1 day before", "1 hour before", "5 minutes before")
    val reminderDurations = listOf(86400000L, 3600000L, 300000L)
    val methodOptions = listOf("Notification", "Ringtone", "Vibration", "Voice Reminder")

    var selectedReminders by remember { mutableStateOf(mutableListOf<Pair<Long, String>>()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> selectedDate = LocalDate.of(year, month + 1, dayOfMonth) },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            startTime = LocalTime.of(hourOfDay, minute)
        },
        startTime.hour,
        startTime.minute,
        false
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            endTime = LocalTime.of(hourOfDay, minute)
        },
        endTime.hour,
        endTime.minute,
        false
    )

    fun scheduleReminder(reminderTime: Long, reminderMethod: String, eventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val localDateTime = selectedDate.atTime(startTime)
        val utcTimeInMillis = localDateTime
            .atZone(gmtPlus8Zone)
            .minusMinutes(reminderTime / 60000)
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("eventId", eventId)
            putExtra("eventName", eventName)
            putExtra("method", reminderMethod)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, utcTimeInMillis, pendingIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (existingEvent == null) "Add Event" else "Edit Event",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = eventNote,
            onValueChange = { eventNote = it },
            label = { Text("Event Note", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
        ) {
            Text(text = "Select Date: ${selectedDate.format(dateFormatter)}", color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { startTimePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
        ) {
            Text(text = "Start Time: ${startTime.format(timeFormatter)}", color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { endTimePickerDialog.show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
        ) {
            Text(text = "End Time: ${endTime.format(timeFormatter)}", color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Reminders",
            style = MaterialTheme.typography.titleMedium,
            color = Color.DarkGray,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            reminderOptions.forEachIndexed { index, label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = selectedReminders.any { it.first == reminderDurations[index] },
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedReminders.add(Pair(reminderDurations[index], "Notification"))
                            } else {
                                selectedReminders.removeIf { it.first == reminderDurations[index] }
                            }
                        },
                        colors = CheckboxDefaults.colors(checkmarkColor = Color.DarkGray)
                    )
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        color = Color.DarkGray
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                        ) {
                            Text(
                                text = selectedReminders.find { it.first == reminderDurations[index] }?.second ?: "Select Method",
                                color = Color.DarkGray
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            methodOptions.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(text = method) },
                                    onClick = {
                                        expanded = false
                                        val reminderIndex = selectedReminders.indexOfFirst { it.first == reminderDurations[index] }
                                        if (reminderIndex != -1) {
                                            selectedReminders[reminderIndex] = selectedReminders[reminderIndex].copy(second = method)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Text(text = "Cancel", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (eventName.isBlank()) {
                        Toast.makeText(context, "Event name cannot be empty", Toast.LENGTH_SHORT).show()
                    } else if (endTime <= startTime) {
                        Toast.makeText(context, "End time must be later than start time", Toast.LENGTH_SHORT).show()
                    } else if (selectedReminders.isEmpty()) {
                        Toast.makeText(context, "Please add at least one reminder", Toast.LENGTH_SHORT).show()
                    } else {
                        val newEvent = existingEvent?.copy(
                            title = eventName,
                            note = eventNote,
                            date = selectedDate,
                            startTime = startTime.truncatedTo(ChronoUnit.MINUTES),
                            endTime = endTime.truncatedTo(ChronoUnit.MINUTES),
                            reminderTimes = selectedReminders.map { it.first },
                            reminderMethods = selectedReminders.map { it.second }
                        ) ?: Event(
                            title = eventName,
                            note = eventNote,
                            date = selectedDate,
                            startTime = startTime.truncatedTo(ChronoUnit.MINUTES),
                            endTime = endTime.truncatedTo(ChronoUnit.MINUTES),
                            reminderTimes = selectedReminders.map { it.first },
                            reminderMethods = selectedReminders.map { it.second }
                        )
                        saveEventToDatabase(newEvent)
                        Toast.makeText(
                            context,
                            if (existingEvent == null) "Event added successfully" else "Event updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        newEvent.reminderTimes.forEachIndexed { index, reminderTime ->
                            scheduleReminder(reminderTime, newEvent.reminderMethods[index], newEvent.id)
                        }
                        onEventSaved()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Text(text = if (existingEvent == null) "Submit" else "Save", color = Color.DarkGray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventScreenPreview() {
    AddEventScreen(
        onEventSaved = {},
        saveEventToDatabase = {},
        existingEvent = Event(
            id = 1,
            title = "Meeting",
            note = "Discuss project",
            date = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 0),
            reminderTimes = listOf(86400000),
            reminderMethods = listOf("Notification")
        )
    )
}
