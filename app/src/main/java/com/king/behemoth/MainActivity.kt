package com.king.behemoth

import android.app.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

data class Habit(var name:String, var hour:Int, var minute:Int, var byday:String="")

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("behemoth", MODE_PRIVATE) }
    private val gson = Gson()
    private val listContainer by lazy { findViewById<LinearLayout>(R.id.listContainer) }
    private val statusText by lazy { findViewById<TextView>(R.id.statusText) }

    private var habits: MutableList<Habit> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createChannel()

        loadHabits()
        renderHabits()

        findViewById<Button>(R.id.addHabitBtn).setOnClickListener {
            habits.add(Habit("New habit", 9, 0, ""))
            renderHabits()
        }
        findViewById<Button>(R.id.saveBtn).setOnClickListener {
            saveHabits()
            Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.scheduleBtn).setOnClickListener {
            saveHabits()
            AlarmScheduler.scheduleAll(this, habits)
            statusText.text = "Scheduled notifications for ${habits.size} habits."
        }
    }

    private fun inflateRow(habit: Habit): View {
        val row = layoutInflater.inflate(R.layout.row_habit, listContainer, false)
        val name = row.findViewById<EditText>(R.id.nameInput)
        val tp = row.findViewById<TimePicker>(R.id.timePicker)
        val byday = row.findViewById<EditText>(R.id.bydayInput)

        name.setText(habit.name)
        tp.setIs24HourView(true)
        tp.hour = habit.hour
        tp.minute = habit.minute
        byday.setText(habit.byday)

        name.addTextChangedListener(SimpleWatcher { habit.name = it })
        tp.setOnTimeChangedListener { _, h, m ->
            habit.hour = h; habit.minute = m
        }
        byday.addTextChangedListener(SimpleWatcher { habit.byday = it })

        return row
    }

    private fun renderHabits() {
        listContainer.removeAllViews()
        habits.forEach { listContainer.addView(inflateRow(it)) }
    }

    private fun loadHabits() {
        val json = prefs.getString("habits", null)
        if (json != null) {
            val type = object: TypeToken<MutableList<Habit>>(){}.type
            habits = gson.fromJson(json, type)
        } else {
            habits = mutableListOf(
                Habit("Wake", 6, 0, ""),
                Habit("Sleep", 22, 30, ""),
                Habit("Shower", 7, 30, ""),
                Habit("Wash clothes", 9, 0, "SA"),
                Habit("Buy groceries", 11, 0, "SU"),
                Habit("Wash car", 14, 0, "SA"),
                Habit("Cut hair", 15, 0, "SA"),
                Habit("Gym (Fri)", 15, 30, "FR"),
                Habit("Gym (Sat)", 15, 30, "SA")
            )
        }
    }

    private fun saveHabits() {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits", json).apply()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Behemoth Reminders"
            val desc = "Habit notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("behemoth_reminders", name, importance)
            channel.description = desc
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}

class SimpleWatcher(val cb:(String)->Unit): android.text.TextWatcher {
    override fun afterTextChanged(s: android.text.Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { cb(s?.toString() ?: "") }
}
