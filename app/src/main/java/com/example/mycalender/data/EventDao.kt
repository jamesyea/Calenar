package com.example.mycalender.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Query("SELECT * FROM events WHERE date = :date ORDER BY StartTime")
    fun getEventsByDate(date: LocalDate): Flow<List<Event>>

    // 新增：取得所有事件
    @Query("SELECT * FROM events ORDER BY date")
    fun getAllEvents(): Flow<List<Event>>
}
