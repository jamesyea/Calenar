package com.example.mycalender.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type;
import java.util.List;

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? =
        timeString?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun fromReminderTimes(reminderTimes: List<Long>?): String? {
        return reminderTimes?.let { Gson().toJson(it) }
    }

    // 將 JSON 字符串轉換為 List<Integer>
    @TypeConverter
    fun toReminderTimes(reminderTimesString: String?): List<Long>? {
        return reminderTimesString?.let {
            val type = object : TypeToken<List<Long>>() {}.type
            Gson().fromJson<List<Long>>(it, type)
        }
    }
    @TypeConverter
    fun fromReminderMethods(reminderMethods: List<String>?): String? {
        return reminderMethods?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toReminderMethods(reminderMethodsString: String?): List<String>? {
        return reminderMethodsString?.let {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(it, type)
        }
    }
}
