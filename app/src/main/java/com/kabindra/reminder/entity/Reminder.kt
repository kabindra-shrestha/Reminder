package com.kabindra.reminder.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reminder(

    @PrimaryKey val reminderId: Int,
    @ColumnInfo(name = "reminderTitle") val reminderTitle: String? = "",
    @ColumnInfo(name = "reminderDate") val reminderDate: String? = "",
    @ColumnInfo(name = "reminderTime") val reminderTime: String? = "",
    @ColumnInfo(name = "reminderRepeat") val reminderRepeat: Boolean? = true,
    @ColumnInfo(name = "reminderRepeatTime") val reminderRepeatTime: String? = "",
    @ColumnInfo(name = "reminderRepeatType") val reminderRepeatType: String? = "",
    @ColumnInfo(name = "reminderEnable") val reminderEnable: Boolean? = true

)