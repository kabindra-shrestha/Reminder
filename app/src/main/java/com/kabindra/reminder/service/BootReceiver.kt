package com.kabindra.reminder.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kabindra.reminder.entity.Reminder
import com.kabindra.reminder.room.DatabaseService
import java.util.*

class BootReceiver : BroadcastReceiver() {

    private val mTitle: String? = null
    private var mTime: String? = null
    private var mDate: String? = null
    private var mRepeatInterval: String? = null
    private var mRepeatType: String? = null
    private var mActive: Boolean? = null
    private var mRepeat: Boolean? = null
    private lateinit var mDateSplit: Array<String>
    private lateinit var mTimeSplit: Array<String>
    private var mYear = 0
    private var mMonth: Int = 0
    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mDay: Int = 0
    private var mReceivedID: Int = 0
    private var mRepeatTime: Long = 0

    private var mAlarmReceiver: AlarmReceiver? = null

    // Constant values in milliseconds
    private val milMinute = 60000L
    private val milHour = 3600000L
    private val milDay = 86400000L
    private val milWeek = 604800000L
    private val milMonth = 2592000000L

    private lateinit var databaseService: DatabaseService

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            databaseService = DatabaseService(context!!)

            var mCalendar = Calendar.getInstance()
            mAlarmReceiver = AlarmReceiver()
            val reminders: List<Reminder>? = databaseService.getAllReminders()
            if (reminders != null) {
                for (rm in reminders) {
                    mReceivedID = rm.reminderId
                    mRepeat = rm.reminderRepeat
                    mRepeatInterval = rm.reminderRepeatTime
                    mRepeatType = rm.reminderRepeatType
                    mActive = rm.reminderEnable
                    mDate = rm.reminderDate
                    mTime = rm.reminderTime
                    mDateSplit = mDate!!.split("/").toTypedArray()
                    mTimeSplit = mTime!!.split(":").toTypedArray()
                    mDay = mDateSplit[0].toInt()
                    mMonth = mDateSplit[1].toInt()
                    mYear = mDateSplit[2].toInt()
                    mHour = mTimeSplit[0].toInt()
                    mMinute = mTimeSplit[1].toInt()
                    mCalendar.set(Calendar.MONTH, --mMonth)
                    mCalendar.set(Calendar.YEAR, mYear)
                    mCalendar.set(Calendar.DAY_OF_MONTH, mDay)
                    mCalendar.set(Calendar.HOUR_OF_DAY, mHour)
                    mCalendar.set(Calendar.MINUTE, mMinute)
                    mCalendar.set(Calendar.SECOND, 0)

                    // Cancel existing notification of the reminder by using its ID
                    // mAlarmReceiver.cancelAlarm(context, mReceivedID);

                    // Check repeat type
                    if (mRepeatType == "Minute") {
                        mRepeatTime = mRepeatInterval!!.toInt() * milMinute
                    } else if (mRepeatType == "Hour") {
                        mRepeatTime = mRepeatInterval!!.toInt() * milHour
                    } else if (mRepeatType == "Day") {
                        mRepeatTime = mRepeatInterval!!.toInt() * milDay
                    } else if (mRepeatType == "Week") {
                        mRepeatTime = mRepeatInterval!!.toInt() * milWeek
                    } else if (mRepeatType == "Month") {
                        mRepeatTime = mRepeatInterval!!.toInt() * milMonth
                    }

                    // Create a new notification
                    if (mActive!!) {
                        if (mRepeat!!) {
                            mAlarmReceiver!!.setRepeatAlarm(
                                context,
                                mCalendar,
                                mReceivedID,
                                mRepeatTime
                            )
                        } else {
                            mAlarmReceiver!!.setAlarm(context, mCalendar, mReceivedID)
                        }
                    }
                }
            }
        }
    }
}