package com.example.mycalender

import android.os.Bundle
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
import com.example.mycalender.calender.AddEventScreen
import com.example.mycalender.calender.CalendarViewModel
import com.example.mycalender.calender.CalendarViewModelFactory
import com.example.mycalender.calender.CalenderScreen
import com.example.mycalender.data.AppDatabase
import com.example.mycalender.data.Event
import com.example.mycalender.data.EventRepository
import com.example.mycalender.ui.theme.MyCalenderTheme
import java.time.format.TextStyle
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventRepository(database.eventDao())
        val viewModel = CalendarViewModelFactory(repository).create(CalendarViewModel::class.java)
        setContent {
            MyCalenderTheme {
                MainActivityContent(viewModel)
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
                        if (eventToEdit == null) {
                            viewModel.addEvent(event) // 新增事件
                        } else {
                            viewModel.updateEvent(event) // 更新事件
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
