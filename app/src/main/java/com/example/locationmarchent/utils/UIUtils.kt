package com.example.locationmarchent.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object UIUtils {
    fun showBasicDialog(
        context: Context, title: String? = null, message: String? = null,
        positiveButton: String, negativeButton: String? = null,
        positiveClickListener: DialogInterface.OnClickListener,
        negativeClickListener: DialogInterface.OnClickListener? = null,
        isCancelable: Boolean = true
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setCancelable(isCancelable)
            .setMessage(message)
            .setPositiveButton(positiveButton, positiveClickListener)
            .setNegativeButton(negativeButton, negativeClickListener)
            .show()
    }
}