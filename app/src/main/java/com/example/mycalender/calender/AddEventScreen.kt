package com.example.mycalender.calender

import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mycalender.ReminderReceiver
import com.example.mycalender.data.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone

@Composable
fun AddEventScreen(
    onEventSaved: () -> Unit,
    onCancel: () -> Unit = {},
    saveEventToDatabase: (Event) -> Unit,
    existingEvent: Event? = null
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity  // 獲取 Activity 以便請求權限

    val gmtPlus8Zone = ZoneId.of("Asia/Singapore")  // 使用 GMT+8 時區

    // 預填充表單數據
    var eventName by remember { mutableStateOf(existingEvent?.title ?: "") }
    var eventNote by remember { mutableStateOf(existingEvent?.note ?: "") }
    var selectedDate by remember { mutableStateOf(existingEvent?.date ?: LocalDate.now()) }
    var startTime by remember { mutableStateOf(existingEvent?.startTime ?: LocalTime.now()) }
    var endTime by remember { mutableStateOf(existingEvent?.endTime ?: LocalTime.now().plusHours(1)) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    // 提醒時間和方式 UI
    val reminderOptions = listOf("1 day before", "1 hour before", "5 minutes before")
    val reminderDurations = listOf(86400000L, 3600000L, 300000L) // 毫秒
    val methodOptions = listOf("Notification", "Ringtone", "Vibration", "Voice Reminder")

    var selectedReminders by remember { mutableStateOf(mutableListOf<Pair<Long, String>>()) }

    // 日期與時間選擇器
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> selectedDate = LocalDate.of(year, month + 1, dayOfMonth) },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    // 開始時間選擇
    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            // 將選擇的時間轉換為 GMT+8 時區
            startTime = LocalTime.of(hourOfDay, minute)
                .atDate(selectedDate)  // 設置日期
                .atZone(gmtPlus8Zone)  // 設置為 GMT+8 時區
                .toLocalTime()  // 轉換回 LocalTime
        },
        startTime.hour,
        startTime.minute,
        false
    )

// 結束時間選擇
    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            // 將選擇的時間轉換為 GMT+8 時區
            endTime = LocalTime.of(hourOfDay, minute)
                .atDate(selectedDate)  // 設置日期
                .atZone(gmtPlus8Zone)  // 設置為 GMT+8 時區
                .toLocalTime()  // 轉換回 LocalTime
        },
        endTime.hour,
        endTime.minute,
        false
    )


    fun scheduleReminder(reminderTime: Long, reminderMethod: String, eventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 轉換為當前的開始時間並手動調整為 GMT+8
        val localDateTime = selectedDate.atTime(startTime)
        val gmtPlus8Zone = ZoneId.of("Asia/Singapore")

        // 轉換成 UTC 時間
        val utcTimeInMillis = localDateTime
            .atZone(gmtPlus8Zone)
            .minusMinutes(reminderTime / 60000)  // 提前時間
            .withZoneSameInstant(ZoneId.of("UTC")) // 轉換為 UTC
            .toInstant()
            .toEpochMilli()

        // 提醒意圖設定
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

        // 設置提醒
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, utcTimeInMillis, pendingIntent)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 標題
        Text(
            text = if (existingEvent == null) "Add Event" else "Edit Event",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 行程名稱
        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 行程備註
        OutlinedTextField(
            value = eventNote,
            onValueChange = { eventNote = it },
            label = { Text("Event Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 日期選擇
        Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Select Date: ${selectedDate.format(dateFormatter)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 開始時間選擇
        Button(onClick = { startTimePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Start Time: ${startTime.format(timeFormatter)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 結束時間選擇
        Button(onClick = { endTimePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "End Time: ${endTime.format(timeFormatter)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Reminders", style = MaterialTheme.typography.titleMedium)
        // 遍歷提醒時間選項，並處理提醒時間和方式
        reminderOptions.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = selectedReminders.any { it.first == reminderDurations[index] },
                    onCheckedChange = { checked ->
                        if (checked) {
                            selectedReminders.add(Pair(reminderDurations[index], "Notification"))
                        } else {
                            selectedReminders.removeIf { it.first == reminderDurations[index] }
                        }
                    }
                )
                Text(text = label, modifier = Modifier.weight(1f))

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    Button(onClick = { expanded = true }) {
                        Text(text = selectedReminders.find { it.first == reminderDurations[index] }?.second ?: "Select Method")
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

        Spacer(modifier = Modifier.height(16.dp))

        // 動作按鈕
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(text = "Cancel")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (eventName.isBlank()) {
                        Toast.makeText(context, "Event name cannot be empty", Toast.LENGTH_SHORT).show()
                    } else if (endTime <= startTime) {
                        Toast.makeText(context, "End time must be later than start time", Toast.LENGTH_SHORT).show()
                    } else if (selectedReminders.isEmpty()) {
                        Toast.makeText(context, "Please add at least one reminder", Toast.LENGTH_SHORT).show()
                    } else {
                        // 保存行程到資料庫
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

                        // 設置提醒
                        newEvent.reminderTimes.forEachIndexed { index, reminderTime ->
                            scheduleReminder(reminderTime.toLong(), newEvent.reminderMethods[index], newEvent.id)
                        }
                        onEventSaved()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (existingEvent == null) "Submit" else "Save")
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
            reminderTimes = listOf(86400000),  // 默認的提醒時間，例如提前1天
            reminderMethods = listOf("Notification") // 默認的提醒方式
        )
    )
}

