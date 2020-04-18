package com.kabindra.reminder.room

import android.content.Context
import com.kabindra.reminder.entity.Reminder

class DatabaseService(context: Context) : QueriesInterface {

    private val dao: RepoDao = AppDatabase.getInstance(context).repoDao()

    override fun insertReminder(vararg reminder: Reminder): Int {
        return try {
            dao.insertReminder(*reminder)
            1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun updateReminder(vararg reminder: Reminder): Int {
        return try {
            dao.updateReminder(*reminder)
            1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun deleteReminder(vararg reminder: Reminder): Int {
        return try {
            dao.deleteReminder(*reminder)
            1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun checkReminder(reminderId: Int): Boolean {
        return dao.checkReminder(reminderId)
    }

    override fun getAllReminders(): List<Reminder>? {
        return dao.getAllReminders()
    }

    override fun getReminderByReminderId(reminderId: Int): Reminder? {
        return dao.getReminderByReminderId(reminderId)
    }

    override fun getLastReminder(): Reminder? {
        return dao.getLastReminder()
    }

}