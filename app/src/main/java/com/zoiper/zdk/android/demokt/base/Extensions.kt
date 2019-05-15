package com.zoiper.zdk.android.demokt.base

import android.widget.EditText
import java.lang.RuntimeException


/**
 * @throws RuntimeException - It cries when there is nothing typed in the field
 */
fun EditText.getTextOrError(): String? {
    val result = this.text.toString().trim { it <= ' ' }

    return if (result.isNotEmpty()) {
        result
    } else {
        this.error = "Required"
        null
    }
}