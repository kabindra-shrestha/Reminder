package com.kabindra.reminder.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class Utils {

    companion object {
        fun showSnackBar(rootView: View, mMessage: String) {
            Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG).show()
        }

        fun showToast(context: Context, mMessage: String) {
            Toast.makeText(context, mMessage, Toast.LENGTH_LONG).show()
        }

        fun getYear(date: String): Int {
            val parts = date.split("/").toTypedArray()
            return parts[2].toInt()
        }

        fun getMonth(date: String): Int {
            val parts = date.split("/").toTypedArray()
            return parts[1].toInt()
        }

        fun getDate(date: String): Int {
            val parts = date.split("/").toTypedArray()
            return parts[0].toInt()
        }

        fun getHour(time: String): Int {
            val parts = time.split(":").toTypedArray()
            return parts[0].toInt()
        }

        fun getMinute(time: String): Int {
            val parts = time.split(":").toTypedArray()
            return parts[1].toInt()
        }
    }

}