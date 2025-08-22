package com.king.behemoth

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

object AlarmScheduler {

    fun scheduleAll(ctx: Context, habits: List<Habit>) {
        cancelAll(ctx, habits) // clear previous
        habits.forEachIndexed { idx, h ->
            scheduleHabit(ctx, idx, h)
        }
    }

    private fun cancelAll(ctx: Context, habits: List<Habit>) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        habits.forEachIndexed { idx, _ ->
            val pi = makePI(ctx, idx, "Cancel")
            am.cancel(pi)
        }
    }

    private fun scheduleHabit(ctx: Context, id: Int, h: Habit) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = nextTrigger(h)
        val pi = makePI(ctx, id, h.name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, next, pi)
        }
    }

    private fun makePI(ctx: Context, id:Int, title:String): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("title", title)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        return PendingIntent.getBroadcast(ctx, id, i, flags)
    }

    private fun nextTrigger(h: Habit): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, h.hour)
        cal.set(Calendar.MINUTE, h.minute)

        val daysMask = parseByDay(h.byday)

        if (daysMask.isEmpty()) {
            // daily
            if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DATE, 1)
            return cal.timeInMillis
        } else {
            // weekly BYDAY
            val todayDow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun..7=Sat
            val order = listOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)
            // find next day in daysMask starting today
            for (i in 0..7) {
                val tryCal = cal.clone() as Calendar
                tryCal.add(Calendar.DATE, i)
                val dow = tryCal.get(Calendar.DAY_OF_WEEK)
                if (daysMask.contains(dow)) {
                    if (i==0 && tryCal.timeInMillis <= System.currentTimeMillis()) continue
                    tryCal.set(Calendar.HOUR_OF_DAY, h.hour)
                    tryCal.set(Calendar.MINUTE, h.minute)
                    tryCal.set(Calendar.SECOND, 0)
                    tryCal.set(Calendar.MILLISECOND, 0)
                    return tryCal.timeInMillis
                }
            }
            // fallback tomorrow
            cal.add(Calendar.DATE, 1)
            return cal.timeInMillis
        }
    }

    private fun parseByDay(byday:String): Set<Int> {
        val map = mapOf(
            "SU" to Calendar.SUNDAY,
            "MO" to Calendar.MONDAY,
            "TU" to Calendar.TUESDAY,
            "WE" to Calendar.WEDNESDAY,
            "TH" to Calendar.THURSDAY,
            "FR" to Calendar.FRIDAY,
            "SA" to Calendar.SATURDAY
        )
        val s = byday.trim().uppercase()
        if (s.isBlank()) return emptySet()
        return s.split(",").mapNotNull { map[it.trim()] }.toSet()
    }
}
