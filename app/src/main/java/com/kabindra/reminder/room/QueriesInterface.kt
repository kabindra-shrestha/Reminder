package com.kabindra.reminder.room

import com.kabindra.reminder.entity.Reminder

interface QueriesInterface {

    fun insertReminder(vararg reminder: Reminder): Int

    fun updateReminder(vararg reminder: Reminder): Int

    fun deleteReminder(vararg reminder: Reminder): Int

    fun checkReminder(reminderId: Int): Boolean

    fun getAllReminders(): List<Reminder>?

    fun getReminderByReminderId(reminderId: Int): Reminder?

    fun getLastReminder(): Reminder?

}
