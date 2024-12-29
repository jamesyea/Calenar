package com.example.mycalender

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.mycalender.calender.AddEventScreen
import com.example.mycalender.calender.CalendarViewModel
import com.example.mycalender.calender.CalendarViewModelFactory
import com.example.mycalender.calender.CalenderScreen
import com.example.mycalender.data.AppDatabase
import com.example.mycalender.data.Event
import com.example.mycalender.data.EventRepository
import com.example.mycalender.ui.theme.MyCalenderTheme
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

import android.widget.Toast
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.BroadcastReceiver
import android.content.pm.PackageManager

import android.media.RingtoneManager
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import java.util.Locale
import android.speech.tts.TextToSpeech
import androidx.core.app.ActivityCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventRepository(database.eventDao())
        val viewModel = CalendarViewModelFactory(repository).create(CalendarViewModel::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",  // 渠道 ID
                "Event Reminders",   // 渠道名称
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_PERMISSION)
            }
        }

        setContent {
            MyCalenderTheme {
                MainActivityContent(viewModel)
            }
        }
    }
    // 定義請求權限的常數
    val REQUEST_CODE_PERMISSION = 123

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 權限授予，開始設置提醒
            } else {
                // 權限拒絕，處理未授權的情況
            }
        }
    }

}

@Composable
fun MainActivityContent(viewModel: CalendarViewModel) {
    var showAddEventScreen by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<Event?>(null) }  // 新增事件編輯狀態



    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEventScreen = true }) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (showAddEventScreen || eventToEdit != null) {
                AddEventScreen(
                    onEventSaved = {
                        showAddEventScreen = false
                        eventToEdit = null },
                    onCancel = { showAddEventScreen = false
                               eventToEdit = null},
                    saveEventToDatabase = { event ->
                        try{
                            if (eventToEdit == null) {
                                viewModel.addEvent(event) // 新增事件
                            } else {
                                viewModel.updateEvent(event) // 更新事件
                            }
                        }catch (e: Exception) {
                            Log.e("MainActivity", "Error saving event", e)
                        }
                    },
                    existingEvent = eventToEdit
                )
            } else {
                CustomizedCalendarScreen(
                    viewModel = viewModel,
                    onEditEvent = { event ->
                        eventToEdit = event
                        showAddEventScreen = true
                    }
                )
            }
        }
    }
}

@Composable
fun CustomizedCalendarScreen(viewModel: CalendarViewModel,onEditEvent: (Event) -> Unit) {
    val currentMonthYear by viewModel.currentMonthYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val eventsForSelectedDate by viewModel.eventsForSelectedDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with artistic background and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.artistic_background),
                contentDescription = "Header Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Calendar",
                color = Color.Black,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // Increased spacing between header and calendar section

        // Calendar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp) // Adjusted padding for uniform spacing
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Calendar Content
                CalenderScreen(viewModel = viewModel,onEditEvent = onEditEvent)
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Increased spacing between calendar and events section

        // Events Section

    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    MyCalenderTheme {
        MainActivityContent(
            viewModel = CalendarViewModelFactory(
                EventRepository(
                    AppDatabase.getDatabase(LocalContext.current).eventDao()
                )
            ).create(CalendarViewModel::class.java)
        )
    }
}

val channelId = "reminder_channel"  // 統一頻道ID

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Event Reminder"
        val descriptionText = "Channel for event reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
fun scheduleNotification(context: Context, event: Event) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationId = event.id.toInt()  // 使用事件的 ID 來生成唯一的通知ID

    // 創建通知內容
    val notification = NotificationCompat.Builder(context, channelId)  // 使用統一的頻道ID
        .setContentTitle("Event Reminder")
        .setContentText("You have an upcoming event: ${event.title}")
        .setSmallIcon(R.drawable.sheep_background)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)  // 點擊通知後自動消失
        .build()

    // 設置提醒時間
    val timeInMillis = event.date.atTime(event.startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val reminderTime = event.date.atTime(event.startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - event.reminderTimes.first()

    // 使用 AlarmManager 設置提醒
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("notificationId", notificationId)
        putExtra("notification", notification)
    }

    val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    // 設置精確的提醒時間
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", -1)
        val notification = intent.getParcelableExtra<Notification>("notification")
        Log.d("NotificationReceiver", "Notification received: $notification")


        // 發送通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

    }
}

