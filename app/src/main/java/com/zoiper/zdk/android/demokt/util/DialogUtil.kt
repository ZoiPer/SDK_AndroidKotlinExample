package com.zoiper.zdk.android.demokt.util

import android.content.Context
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout

fun textPromptDialog(context: Context,
                     message :String = "Enter text",
                     positive: String = "Ok",
                     negative: String = "Cancel",
                     inputHint: String = "",
                     onResult: (result: String) -> Unit) {

    // Edit text to enter number.
    val input = EditText(context)
    input.hint = inputHint

    // Layout params
    input.layoutParams = FrameLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )

    // Padding
    val padding = DensityPixelUtils.convertDpToPixel(20f, context).toInt()
    input.setPadding(padding, padding, padding, padding)

    // Dialog self
    AlertDialog
        .Builder(context)
        .setMessage(message)
        .setPositiveButton(positive){ _, _ -> onResult.invoke(input.text.toString()) }
        .setNegativeButton(negative){ dialog, _ -> dialog.dismiss() }
        .create()
        .let {
            // Add input to the dialog
            it.setCanceledOnTouchOutside(false)
            it.setView(input)
            it.show()
        }
}