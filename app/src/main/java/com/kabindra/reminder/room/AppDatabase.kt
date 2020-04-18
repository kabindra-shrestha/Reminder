package com.kabindra.reminder.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kabindra.reminder.entity.Reminder

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun repoDao(): RepoDao

    companion object {

        private var appDatabase: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (appDatabase == null) {
                appDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reminder.db"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return appDatabase!!
        }

    }

}