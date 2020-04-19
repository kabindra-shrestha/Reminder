package com.kabindra.reminder.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kabindra.reminder.R
import com.kabindra.reminder.ReminderActivity
import com.kabindra.reminder.entity.Reminder
import com.kabindra.reminder.room.DatabaseService
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    var mAlarmManager: AlarmManager? = null
    var mPendingIntent: PendingIntent? = null

    private lateinit var databaseService: DatabaseService

    override fun onReceive(context: Context, intent: Intent) {
        val mReceivedID = intent.getIntExtra(ReminderActivity.REMINDER_ID, 0)

        // Get notification title from Reminder Database
        databaseService = DatabaseService(context)
        val reminder: Reminder? = databaseService.getReminderByReminderId(mReceivedID)
        val reminderTitle = reminder?.reminderTitle
        val mTitle: String? = reminderTitle

        // Create intent to open ReminderActivity on notification click
        val editIntent = Intent(context, ReminderActivity::class.java)
        editIntent.putExtra(
            ReminderActivity.REMINDER_ID,
            mReceivedID.toString()
        )
        val mClick = PendingIntent.getActivity(
            context,
            mReceivedID,
            editIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        Log.e("AlarmReceiver: ", "Notification Title: $mTitle")

        // Create Notification
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.app_icon
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
        val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.notify(mReceivedID, mBuilder.build())
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
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime

        // Start alarm using notification time
        /*mAlarmManager!![AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + diffTime] =
            mPendingIntent*/
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

        Log.e(
            "AlarmReceiver: ",
            calendar.timeInMillis.toString() + " " + currentTime + " " + (calendar.timeInMillis - currentTime)
        )

        // Restart alarm if device is rebooted
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun setRepeatAlarm(
        context: Context,
        calendar: Calendar,
        ID: Int,
        mRepeatTime: Long
    ) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Put Reminder ID in Intent Extra
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(ReminderActivity.REMINDER_ID, ID)
        mPendingIntent =
            PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Calculate notification timein
        val c = Calendar.getInstance()
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime

        // Start alarm using initial notification time and repeat interval time
        mAlarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            c.timeInMillis + diffTime,
            mRepeatTime, mPendingIntent
        )

        Log.e(
            "AlarmReceiver: Repeat",
            calendar.timeInMillis.toString() + " " + currentTime + " " + (calendar.timeInMillis - currentTime) + " " + mRepeatTime
        )

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