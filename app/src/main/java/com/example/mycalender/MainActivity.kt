package com.example.mycalender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
//import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalender.calender.AddEventScreen
import com.example.mycalender.calender.CalendarViewModel
import com.example.mycalender.calender.CalendarViewModelFactory
import com.example.mycalender.ui.theme.MyCalenderTheme
import com.example.mycalender.calender.CalenderScreen
import com.example.mycalender.data.AppDatabase
import com.example.mycalender.data.EventRepository

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
    // 用來控制是否顯示 AddEventScreen
    var showAddEventScreen by remember { mutableStateOf(false) }



    // Scaffold 提供 FAB 和內容區域
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddEventScreen = true }) {
                Text(text = "+") // FAB 的 "+" 按鈕
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (showAddEventScreen) {
                // 新增事件畫面
                AddEventScreen(
                    onEventAdded = { showAddEventScreen = false },
                    addEventToDatabase = { event ->
                        viewModel.addEvent(event)
                    }
                )
            } else {
                // 顯示行事曆畫面
                CalenderScreen(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    MyCalenderTheme {
    }
}
