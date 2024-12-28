package com.example.mycalender.calender

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mycalender.data.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddEventScreen(
    onEventSaved: () -> Unit,
    onCancel: () -> Unit = {},
    saveEventToDatabase: (Event) -> Unit,
    existingEvent: Event? = null
) {
    val context = LocalContext.current

    // 預填充表單數據
    var eventName by remember { mutableStateOf(existingEvent?.title ?: "") }
    var eventNote by remember { mutableStateOf(existingEvent?.note ?: "") }
    var selectedDate by remember { mutableStateOf(existingEvent?.date ?: LocalDate.now()) }
    var startTime by remember { mutableStateOf(existingEvent?.startTime ?: LocalTime.now()) }
    var endTime by remember { mutableStateOf(existingEvent?.endTime ?: LocalTime.now().plusHours(1)) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    // 日期與時間選擇器
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
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
        Button(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Date: ${selectedDate.format(dateFormatter)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 開始時間選擇
        Button(
            onClick = { startTimePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Start Time: ${startTime.format(timeFormatter)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 結束時間選擇
        Button(
            onClick = { endTimePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "End Time: ${endTime.format(timeFormatter)}")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 動作按鈕
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    } else {
                        // 保存行程到資料庫
                        val newEvent = existingEvent?.copy(
                            title = eventName,
                            note = eventNote,
                            date = selectedDate,
                            startTime = startTime,
                            endTime = endTime
                        ) ?: Event(
                            title = eventName,
                            note = eventNote,
                            date = selectedDate,
                            startTime = startTime,
                            endTime = endTime
                        )
                        saveEventToDatabase(newEvent)
                        Toast.makeText(
                            context,
                            if (existingEvent == null) "Event added successfully" else "Event updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
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
            endTime = LocalTime.of(11, 0)
        )
    )
}
