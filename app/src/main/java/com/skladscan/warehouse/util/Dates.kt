package com.skladscan.warehouse.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Dates {

    fun startOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun lastNDayStarts(now: Long, days: Int): List<Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startOfDay(now)
            add(Calendar.DAY_OF_YEAR, -(days - 1))
        }
        return buildList {
            repeat(days) {
                add(calendar.timeInMillis)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    fun weekdayShort(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Пн"
            Calendar.TUESDAY -> "Вт"
            Calendar.WEDNESDAY -> "Ср"
            Calendar.THURSDAY -> "Чт"
            Calendar.FRIDAY -> "Пт"
            Calendar.SATURDAY -> "Сб"
            else -> "Вс"
        }
    }

    fun relative(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        if (timestamp <= 0L) return "ещё не было"
        val diff = now - timestamp
        if (diff < 60_000L) return "только что"
        if (diff < 3_600_000L) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            return "$minutes ${plural(minutes.toInt(), "минуту", "минуты", "минут")} назад"
        }
        val start = startOfDay(now)
        val time = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(timestamp))
        return when {
            timestamp >= start -> "сегодня, $time"
            timestamp >= start - 86_400_000L -> "вчера, $time"
            else -> {
                val day = SimpleDateFormat("d MMM", Locale("ru")).format(Date(timestamp))
                "$day, $time"
            }
        }
    }

    fun plural(value: Int, one: String, few: String, many: String): String {
        val mod100 = value % 100
        val mod10 = value % 10
        return when {
            mod100 in 11..14 -> many
            mod10 == 1 -> one
            mod10 in 2..4 -> few
            else -> many
        }
    }
}
