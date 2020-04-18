package com.kabindra.reminder.room

import androidx.room.*
import com.kabindra.reminder.entity.Reminder

@Dao
interface RepoDao {

    @Insert
    fun insertReminder(vararg reminder: Reminder)

    @Update
    fun updateReminder(vararg reminder: Reminder)

    @Delete
    fun deleteReminder(vararg reminder: Reminder)

    @Query("Select * FROM reminder where reminderId = :reminderId")
    fun checkReminder(reminderId: Int): Boolean

    @Query("SELECT * FROM reminder")
    fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminder where reminderId = :reminderId")
    fun getReminderByReminderId(reminderId: Int): Reminder

    @Query("SELECT * FROM reminder ORDER BY reminderId DESC LIMIT 1")
    fun getLastReminder(): Reminder

}