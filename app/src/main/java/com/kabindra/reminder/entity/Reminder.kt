package com.kabindra.reminder.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Reminder(

    @PrimaryKey var reminderId: Int,
    @ColumnInfo(name = "reminderTitle") var reminderTitle: String? = "",
    @ColumnInfo(name = "reminderDate") var reminderDate: String? = "",
    @ColumnInfo(name = "reminderTime") var reminderTime: String? = "",
    @ColumnInfo(name = "reminderRepeat") var reminderRepeat: Boolean? = true,
    @ColumnInfo(name = "reminderRepeatTime") var reminderRepeatTime: String? = "",
    @ColumnInfo(name = "reminderRepeatType") var reminderRepeatType: String? = "",
    @ColumnInfo(name = "reminderEnable") var reminderEnable: Boolean? = true

)