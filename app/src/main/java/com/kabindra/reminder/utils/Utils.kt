package com.kabindra.reminder.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class Utils {

    companion object {
        fun showSnackbar(rootView: View, mMessage: String) {
            Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG).show()
        }

        fun showToast(context: Context, mMessage: String) {
            Toast.makeText(context, mMessage, Toast.LENGTH_LONG).show()
        }
    }

}