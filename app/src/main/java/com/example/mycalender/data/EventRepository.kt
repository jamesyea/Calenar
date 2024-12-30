package com.example.mycalender.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    fun getEventsByDate(date: LocalDate): Flow<List<Event>> {
        return eventDao.getEventsByDate(date)
    }

    // 新增：取得所有事件
    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }
}
