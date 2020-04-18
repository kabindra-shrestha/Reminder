package com.kabindra.reminder

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.kabindra.reminder.entity.Reminder
import com.kabindra.reminder.room.DatabaseService
import com.kabindra.reminder.utils.Utils
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_reminder.*
import java.util.*

class ReminderActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var databaseService: DatabaseService
    private var reminderId: Int = 0

    private var mYear = 0
    private var mMonth: Int = 0
    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mDay: Int = 0

    private var mTitle: String? = null
    private var mTime: String? = null
    private var mDate: String? = null
    private var mRepeat: Boolean? = null
    private var mRepeatNo: String? = null
    private var mRepeatType: String? = null
    private var mActive: Boolean? = null

    companion object {
        private var REMINDER_ID: String = "reminderId"

        fun start(context: Context, reminderId: Int?) {
            val intent = Intent(context, ReminderActivity::class.java)
            /*intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK*/
            intent.putExtra(REMINDER_ID, reminderId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        databaseService = DatabaseService(this)

        reminderId = intent.getIntExtra(REMINDER_ID, 0)

        if (reminderId == 0) {
            title = "Add Reminder"

            // Initialize default values
            val mCalendar = Calendar.getInstance()
            mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
            mMinute = mCalendar.get(Calendar.MINUTE)
            mYear = mCalendar.get(Calendar.YEAR)
            mMonth = mCalendar.get(Calendar.MONTH) + 1
            mDay = mCalendar.get(Calendar.DATE)

            mTitle = ""
            mDate = "$mDay/$mMonth/$mYear"
            mTime = "$mHour:$mMinute"
            mActive = true
            mRepeat = true
            mRepeatNo = 1.toString()
            mRepeatType = "Hour"
        } else {
            title = "Edit Reminder"

            // Initialize values
            val reminder = databaseService.getReminderByReminderId(reminderId)

            mTitle = reminder?.reminderTitle
            mDate = reminder?.reminderDate
            mTime = reminder?.reminderTime
            mActive = reminder?.reminderEnable
            mRepeat = reminder?.reminderRepeat
            mRepeatNo = reminder?.reminderRepeatTime
            mRepeatType = reminder?.reminderRepeatType
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        // Setup Reminder Title EditText
        /*reminder_title_set.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mTitle = s.toString().trim { it <= ' ' }
                reminder_title_set.error = null
            }

            override fun afterTextChanged(s: Editable) {}
        })*/

        // Setup TextViews using reminder values
        reminder_title_set!!.setText(mTitle)
        reminder_date_set!!.text = mDate
        reminder_time_set!!.text = mTime
        reminder_repeat_switch!!.isChecked = mRepeat!!
        reminder_repeat_set!!.text = "Every $mRepeatNo $mRepeatType(s)"
        reminder_repeat_interval_set!!.text = mRepeatNo
        reminder_repeat_type_set!!.text = mRepeatType
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_reminder, menu)

        val saveItem = menu?.findItem(R.id.action_save_reminder)
        val updateItem = menu?.findItem(R.id.action_update_reminder)
        val deleteItem = menu?.findItem(R.id.action_delete_reminder)

        if (reminderId == 0) {
            saveItem!!.isVisible = true
            updateItem!!.isVisible = false
            deleteItem!!.isVisible = false
        } else {
            saveItem!!.isVisible = false
            updateItem!!.isVisible = true
            deleteItem!!.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save_reminder -> {
                saveReminder()
                true
            }
            R.id.action_update_reminder -> {
                updateReminder()
                true
            }
            R.id.action_delete_reminder -> {
                deleteReminder()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveReminder() {
        if (reminder_title_set.text.isEmpty())
            reminder_title_set.error = "Reminder title cannot be empty."
        else {
            val reminder = databaseService.getLastReminder()

            var reminderId = reminder?.reminderId ?: 0

            val totalCount = reminderId + 1

            val success = databaseService.insertReminder(
                Reminder(
                    totalCount,
                    reminder_title_set.text.toString(),
                    reminder_date_set.text.toString(),
                    reminder_time_set.text.toString(),
                    reminder_repeat_switch.isChecked,
                    reminder_repeat_interval_set.text.toString(),
                    reminder_repeat_type_set.text.toString(),
                    mActive
                )
            )

            if (success == 1) {
                Utils.showSnackbar(
                    findViewById(android.R.id.content),
                    "Reminder inserted successfully."
                )
            }

            onBackPressed()
        }

        /*val rb = ReminderDatabase(this)

        // Creating Reminder
        val ID: Int =
            rb.addReminder(Reminder(mTitle, mDate, mTime, mRepeat, mRepeatNo, mRepeatType, mActive))

        // Set up calender for creating the notification
        mCalendar.set(Calendar.MONTH, --mMonth)
        mCalendar.set(Calendar.YEAR, mYear)
        mCalendar.set(Calendar.DAY_OF_MONTH, mDay)
        mCalendar.set(Calendar.HOUR_OF_DAY, mHour)
        mCalendar.set(Calendar.MINUTE, mMinute)
        mCalendar.set(Calendar.SECOND, 0)

        // Check repeat type
        if (mRepeatType == "Minute") {
            mRepeatTime = mRepeatNo.toInt() * ReminderActivity.milMinute
        } else if (mRepeatType == "Hour") {
            mRepeatTime = mRepeatNo.toInt() * ReminderActivity.milHour
        } else if (mRepeatType == "Day") {
            mRepeatTime = mRepeatNo.toInt() * ReminderActivity.milDay
        } else if (mRepeatType == "Week") {
            mRepeatTime = mRepeatNo.toInt() * ReminderActivity.milWeek
        } else if (mRepeatType == "Month") {
            mRepeatTime = mRepeatNo.toInt() * ReminderActivity.milMonth
        }

        // Create a new notification
        if (mActive == "true") {
            if (mRepeat == "true") {
                AlarmReceiver().setRepeatAlarm(applicationContext, mCalendar, ID, mRepeatTime)
            } else if (mRepeat == "false") {
                AlarmReceiver().setAlarm(applicationContext, mCalendar, ID)
            }
        }

        // Create toast to confirm new reminder
        Toast.makeText(
            applicationContext, "Saved",
            Toast.LENGTH_SHORT
        ).show()*/
    }

    private fun updateReminder() {
        if (reminder_title_set.text.isEmpty())
            reminder_title_set.error = "Reminder title cannot be empty."
        else {
            val success = databaseService.updateReminder(
                Reminder(
                    reminderId,
                    reminder_title_set.text.toString(),
                    reminder_date_set.text.toString(),
                    reminder_time_set.text.toString(),
                    reminder_repeat_switch.isChecked,
                    reminder_repeat_interval_set.text.toString(),
                    reminder_repeat_type_set.text.toString(),
                    mActive
                )
            )

            if (success == 1) {
                Utils.showSnackbar(
                    findViewById(android.R.id.content),
                    "Reminder updated successfully."
                )
            }

            onBackPressed()
        }
    }

    private fun deleteReminder() {
        val reminder = databaseService.getReminderByReminderId(reminderId)

        val success = databaseService.deleteReminder(reminder!!)

        if (success == 1) {
            Utils.showSnackbar(
                findViewById(android.R.id.content),
                "Reminder deleted successfully."
            )
        }

        onBackPressed()
    }

    fun setDate(v: View?) {
        val now = Calendar.getInstance()
        val dpd: DatePickerDialog = DatePickerDialog.newInstance(
            this,
            now[Calendar.YEAR],
            now[Calendar.MONTH],
            now[Calendar.DAY_OF_MONTH]
        )
        dpd.show(fragmentManager, "Datepickerdialog")
    }

    fun setTime(v: View?) {
        val now = Calendar.getInstance()
        val tpd =
            TimePickerDialog.newInstance(
                this,
                now[Calendar.HOUR_OF_DAY],
                now[Calendar.MINUTE],
                false
            )
        tpd.isThemeDark = false
        tpd.show(fragmentManager, "Timepickerdialog")
    }

    fun onSwitchRepeat(view: View) {
        val on = (view as Switch).isChecked
        if (on) {
            mRepeat = true
            reminder_repeat_set?.text = "Every $mRepeatNo $mRepeatType(s)"
        } else {
            mRepeat = false
            reminder_repeat_set?.setText(R.string.repeat_off)
        }
    }

    fun setRepeatNo(v: View?) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Enter Number")

        // Create EditText box to input repeat number
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        alert.setView(input)
        alert.setPositiveButton(
            "Ok"
        ) { dialog, whichButton ->
            if (input.text.toString().length == 0) {
                mRepeatNo = Integer.toString(1)
                // mRepeatNoText?.text = mRepeatNo
                reminder_repeat_interval_set?.text = mRepeatNo
                // mRepeatText?.text = "Every $mRepeatNo $mRepeatType(s)"
                reminder_repeat_set?.text = "Every $mRepeatNo $mRepeatType(s)"
            } else {
                mRepeatNo = input.text.toString().trim { it <= ' ' }
                // mRepeatNoText?.text = mRepeatNo
                reminder_repeat_interval_set?.text = mRepeatNo
                // mRepeatText?.text = "Every $mRepeatNo $mRepeatType(s)"
                reminder_repeat_set?.text = "Every $mRepeatNo $mRepeatType(s)"
            }
        }
        alert.setNegativeButton(
            "Cancel"
        ) { dialog, whichButton ->
            // Do nothing
        }
        alert.show()
    }

    fun selectRepeatType(v: View?) {
        val items = arrayOfNulls<String>(5)
        items[0] = "Minute"
        items[1] = "Hour"
        items[2] = "Day"
        items[3] = "Week"
        items[4] = "Month"

        // Create List Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Type")
        builder.setItems(items) { dialog, item ->
            mRepeatType = items[item]
            reminder_repeat_type_set?.text = mRepeatType
            reminder_repeat_set?.text = "Every $mRepeatNo $mRepeatType(s)"
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        var monthOfYearNew = monthOfYear
        monthOfYearNew++
        mDay = dayOfMonth
        mMonth = monthOfYearNew
        mYear = year
        mDate = "$dayOfMonth/$monthOfYearNew/$year"

        reminder_date_set.text = mDate
    }

    override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
        mHour = hourOfDay
        mMinute = minute
        if (minute < 10) {
            mTime = "$hourOfDay:0$minute"
        } else {
            mTime = "$hourOfDay:$minute"
        }

        reminder_time_set.text = mTime
    }

}
