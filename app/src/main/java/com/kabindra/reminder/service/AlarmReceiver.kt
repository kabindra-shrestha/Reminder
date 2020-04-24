package com.kabindra.reminder.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kabindra.reminder.MainActivity
import com.kabindra.reminder.R
import com.kabindra.reminder.ReminderActivity
import com.kabindra.reminder.entity.Reminder
import com.kabindra.reminder.room.DatabaseService
import java.util.*


class AlarmReceiver : BroadcastReceiver() {
    var mAlarmManager: AlarmManager? = null
    var mPendingIntent: PendingIntent? = null

    private lateinit var databaseService: DatabaseService

    companion object {
        // Constant values in milliseconds
        const val milMinute = 60000L
        const val milHour = 3600000L
        const val milDay = 86400000L
        const val milWeek = 604800000L
        const val milMonth = 2592000000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val mReceivedID = intent.getIntExtra(ReminderActivity.REMINDER_ID, 0)

        // Get notification title from Reminder Database
        databaseService = DatabaseService(context)
        val reminder: Reminder? = databaseService.getReminderByReminderId(mReceivedID)
        val reminderTitle = reminder?.reminderTitle
        val mTitle: String? = reminderTitle
        val reminderRepeat: Boolean? = reminder?.reminderRepeat
        val reminderRepeatTime: String? = reminder?.reminderRepeatTime
        val reminderRepeatType: String? = reminder?.reminderRepeatType

        // Create intent to open ReminderActivity on notification click
        val editIntent = Intent(context, MainActivity::class.java)
        /*editIntent.putExtra(
            ReminderActivity.REMINDER_ID,
            mReceivedID.toString()
        )*/
        val mClick = PendingIntent.getActivity(
            context,
            mReceivedID,
            editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        Log.e("AlarmReceiver: ", "Notification Title: $mTitle")

        // make the channel. The method has been discussed before.
        val channelId = context.resources.getString(R.string.channel_id)
        val channelName = context.resources.getString(R.string.channel_name)
        val channelDescription = context.resources.getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            makeNotificationChannel(
                context,
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }

        // the check ensures that the channel will only be made
        // if the device is running Android 8+
        val notification = NotificationCompat.Builder(context, channelId)

        // the second parameter is the channel id.
        // it should be the same as passed to the makeNotificationChannel() method
        notification
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.reminder_active_on
                )
            )
            .setSmallIcon(R.drawable.reminder_active_on)
            .setContentTitle(context.resources.getString(R.string.app_name))
            .setTicker(mTitle)
            .setContentText(mTitle)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(mClick)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

        val notificationManager =
            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)!!

        notificationManager.notify(
            (mReceivedID + Calendar.getInstance().timeInMillis).toInt(),
            notification.build()
        )
        // it is better to not use 0 as notification id, so used 1.

        if (reminderRepeat!!) {
            val mCalendar = Calendar.getInstance()
            mCalendar.set(Calendar.SECOND, 0)

            // Check repeat type
            var mRepeatTime: Long = 0
            when (reminderRepeatType) {
                "Minute" -> {
                    mRepeatTime = reminderRepeatTime.toString().toInt() * milMinute
                }
                "Hour" -> {
                    mRepeatTime = reminderRepeatTime.toString().toInt() * milHour
                }
                "Day" -> {
                    mRepeatTime = reminderRepeatTime.toString().toInt() * milDay
                }
                "Week" -> {
                    mRepeatTime = reminderRepeatTime.toString().toInt() * milWeek
                }
                "Month" -> {
                    mRepeatTime = reminderRepeatTime.toString().toInt() * milMonth
                }
            }

            val nextRepeatTime = mCalendar.timeInMillis + mRepeatTime
            mCalendar.timeInMillis = nextRepeatTime

            // Create a new notification
            if (reminder.reminderEnable!!) {
                AlarmReceiver().setAlarm(context, mCalendar, mReceivedID)
            }
        } else {
            reminder.reminderEnable = false
            databaseService.updateReminder(reminder)
            /*val success = databaseService.updateReminder(reminder)
            if (success == 1) {
                Utils.showSnackBar(
                    findViewById(android.R.id.content),
                    "Reminder updated successfully."
                )
            }*/
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun makeNotificationChannel(
        context: Context,
        id: String?,
        name: String?,
        importance: Int
    ) {
        val channel = NotificationChannel(id, name, importance)
        channel.setShowBadge(true) // set false to disable badges, Oreo exclusive
        val notificationManager =
            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)!!
        notificationManager.createNotificationChannel(channel)
    }

    fun setAlarm(
        context: Context,
        calendar: Calendar,
        ID: Int
    ) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Put Reminder ID in Intent Extra
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(ReminderActivity.REMINDER_ID, ID)
        mPendingIntent =
            PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Calculate notification time
        val c = Calendar.getInstance()
        c.set(Calendar.SECOND, 0)
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime

        // Start alarm using notification time
        mAlarmManager!!.setExact(
            AlarmManager.RTC_WAKEUP,
            c.timeInMillis + diffTime,
            mPendingIntent
        )
        if (Build.VERSION.SDK_INT >= 23) {
            mAlarmManager!!.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis + diffTime,
                mPendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= 19) {
            mAlarmManager!!.setExact(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis + diffTime,
                mPendingIntent
            )
        } else {
            mAlarmManager!!.set(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis + diffTime,
                mPendingIntent
            )
        }

        // Restart alarm if device is rebooted
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm(context: Context, ID: Int) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel Alarm using Reminder ID
        mPendingIntent =
            PendingIntent.getBroadcast(context, ID, Intent(context, AlarmReceiver::class.java), 0)
        mAlarmManager!!.cancel(mPendingIntent)

        // Disable alarm
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

}