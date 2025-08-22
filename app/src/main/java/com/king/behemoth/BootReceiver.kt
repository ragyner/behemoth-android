package com.king.behemoth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action?.contains("BOOT") == true) {
            val prefs = context.getSharedPreferences("behemoth", Context.MODE_PRIVATE)
            val json = prefs.getString("habits", null) ?: return
            val type = object: TypeToken<MutableList<Habit>>(){}.type
            val habits: MutableList<Habit> = Gson().fromJson(json, type)
            AlarmScheduler.scheduleAll(context, habits)
        }
    }
}
