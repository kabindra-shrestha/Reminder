package com.kabindra.reminder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kabindra.reminder.adapter.ReminderRecyclerViewAdapter
import com.kabindra.reminder.entity.Reminder
import com.kabindra.reminder.room.DatabaseService
import com.kabindra.reminder.service.AlarmReceiver
import com.kabindra.reminder.service.AlarmReceiver.Companion.milDay
import com.kabindra.reminder.service.AlarmReceiver.Companion.milHour
import com.kabindra.reminder.service.AlarmReceiver.Companion.milMinute
import com.kabindra.reminder.service.AlarmReceiver.Companion.milMonth
import com.kabindra.reminder.service.AlarmReceiver.Companion.milWeek
import com.kabindra.reminder.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.adapter_reminder_recycler_view_items.view.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var databaseService: DatabaseService

    private lateinit var reminderRecyclerViewAdapter: ReminderRecyclerViewAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Reminder"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        databaseService = DatabaseService(this)

        showList(main_list, false)
        showError(main_error, false)

        linearLayoutManager = LinearLayoutManager(this)
        main_list.layoutManager = linearLayoutManager
        main_list.addItemDecoration(
            DividerItemDecoration(
                main_list.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_add_reminder -> {
                ReminderActivity.start(this@MainActivity, null)
                true
            }
            R.id.action_about_reminder -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        val reminders = databaseService.getAllReminders()

        if (reminders!!.isEmpty()) {
            showList(main_list, false)
            showError(main_error, true)
        } else {
            showList(main_list, true)
            showError(main_error, false)

            reminderRecyclerViewAdapter = ReminderRecyclerViewAdapter(this, reminders)
            main_list.adapter = reminderRecyclerViewAdapter

            reminderRecyclerViewAdapter.setNotifyData(reminders)

            reminderRecyclerViewAdapter.setOnItemClickListener(object :
                ReminderRecyclerViewAdapter.OnItemClickListener {
                override fun onClick(view: View, position: Int) {
                    ReminderActivity.start(this@MainActivity, reminders[position].reminderId)
                }

                override fun onSwitchClick(
                    view: View,
                    reminder: Reminder,
                    position: Int
                ) {
                    if (reminder.reminderRepeat!!) {
                        updateReminder(reminder, position, reminders)
                    } else {
                        // Set up calender for creating the notification
                        val mCalendar = Calendar.getInstance()
                        val mCalendarNow = Calendar.getInstance()
                        mCalendar.set(
                            Calendar.MONTH,
                            Utils.getMonth(reminder.reminderDate.toString()) - 1
                        )
                        mCalendar.set(
                            Calendar.YEAR,
                            Utils.getYear(reminder.reminderDate.toString())
                        )
                        mCalendar.set(
                            Calendar.DAY_OF_MONTH,
                            Utils.getDate(reminder.reminderDate.toString())
                        )
                        mCalendar.set(
                            Calendar.HOUR_OF_DAY,
                            Utils.getHour(reminder.reminderTime.toString())
                        )
                        mCalendar.set(
                            Calendar.MINUTE,
                            Utils.getMinute(reminder.reminderTime.toString())
                        )
                        mCalendar.set(Calendar.SECOND, 0)

                        if (mCalendarNow.timeInMillis > mCalendar.timeInMillis) {
                            if (/*view.reminder_repeat_switch.isChecked*/reminder.reminderEnable!!) {
                                var reminderNew = Reminder(
                                    reminder.reminderId,
                                    reminder.reminderTitle,
                                    reminder.reminderDate,
                                    reminder.reminderTime,
                                    reminder.reminderRepeat,
                                    reminder.reminderRepeatTime,
                                    reminder.reminderRepeatType,
                                    false
                                )
                                updateReminder(reminderNew, position, reminders)
                            }

                            view.reminder_repeat_switch.isChecked = false

                            Utils.showSnackBar(
                                findViewById(android.R.id.content),
                                "The reminder time has been expired. You can update the reminder or delete if not needed."
                            )
                        } else {
                            updateReminder(reminder, position, reminders)
                        }
                    }
                }
            })
        }
    }

    private fun updateReminder(
        reminder: Reminder,
        position: Int,
        reminders: List<Reminder>
    ) {
        reminders[position].reminderId = reminder.reminderId
        reminders[position].reminderTitle = reminder.reminderTitle
        reminders[position].reminderDate = reminder.reminderDate
        reminders[position].reminderTime = reminder.reminderTime
        reminders[position].reminderRepeat = reminder.reminderRepeat
        reminders[position].reminderRepeatTime = reminder.reminderRepeatTime
        reminders[position].reminderRepeatType = reminder.reminderRepeatType
        reminders[position].reminderEnable = reminder.reminderEnable

        reminderRecyclerViewAdapter.setNotifyData(reminders)

        val success = databaseService.updateReminder(reminder)

        // Cancel existing notification of the reminder by using its ID
        AlarmReceiver().cancelAlarm(applicationContext, reminder.reminderId)

        val reminderRepeat: Boolean? = reminder?.reminderRepeat
        val reminderRepeatTime: String? = reminder?.reminderRepeatTime
        val reminderRepeatType: String? = reminder?.reminderRepeatType
        if (reminderRepeat!!) {
            val mCalendar = Calendar.getInstance()
            mCalendar.set(Calendar.SECOND, 0)

            // Check repeat type
            var mRepeatTime: Long = 0
            if (reminderRepeatType == "Minute") {
                mRepeatTime = reminderRepeatTime.toString().toInt() * milMinute
            } else if (reminderRepeatType == "Hour") {
                mRepeatTime = reminderRepeatTime.toString().toInt() * milHour
            } else if (reminderRepeatType == "Day") {
                mRepeatTime = reminderRepeatTime.toString().toInt() * milDay
            } else if (reminderRepeatType == "Week") {
                mRepeatTime = reminderRepeatTime.toString().toInt() * milWeek
            } else if (reminderRepeatType == "Month") {
                mRepeatTime = reminderRepeatTime.toString().toInt() * milMonth
            }

            val nextRepeatTime = mCalendar.timeInMillis + mRepeatTime
            mCalendar.timeInMillis = nextRepeatTime

            // Create a new notification
            if (reminder.reminderEnable!!) {
                AlarmReceiver().setAlarm(applicationContext, mCalendar, reminder.reminderId)
            }
        } else {

            // Set up calender for creating the notification
            val mCalendar = Calendar.getInstance()
            mCalendar.set(Calendar.MONTH, Utils.getMonth(reminder.reminderDate.toString()) - 1)
            mCalendar.set(Calendar.YEAR, Utils.getYear(reminder.reminderDate.toString()))
            mCalendar.set(Calendar.DAY_OF_MONTH, Utils.getDate(reminder.reminderDate.toString()))
            mCalendar.set(Calendar.HOUR_OF_DAY, Utils.getHour(reminder.reminderTime.toString()))
            mCalendar.set(Calendar.MINUTE, Utils.getMinute(reminder.reminderTime.toString()))
            mCalendar.set(Calendar.SECOND, 0)

            // Create a new notification
            if (reminder.reminderEnable!!) {
                AlarmReceiver().setAlarm(applicationContext, mCalendar, reminder.reminderId)
            }

            if (success == 1) {
                Utils.showSnackBar(
                    findViewById(android.R.id.content),
                    "Reminder updated successfully."
                )
            }
        }
    }

    /*private fun showSoftKeyboard(view: View, show: Boolean) {
        if (show) {
            if (view.requestFocus()) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showProgress(progressBar: ProgressBar, show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            progressBar.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }*/

    private fun showList(main_list: RecyclerView, show: Boolean) {
        if (show) {
            main_list.visibility = View.VISIBLE
        } else {
            main_list.visibility = View.GONE
        }
    }

    private fun showError(main_error: TextView, show: Boolean) {
        if (show) {
            main_error.visibility = View.VISIBLE
        } else {
            main_error.visibility = View.GONE
        }
    }

}
