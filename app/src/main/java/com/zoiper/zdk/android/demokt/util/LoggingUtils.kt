package com.zoiper.zdk.android.demokt.util

import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * LoggingUtiils
 *
 * @since 16.1.2020 г.
 */

private const val DEBUG_LOG_EDITOR_KEY = "key_debug_log_filename";
private const val FILE_NAME = "logfile";

fun generateDebugLogFilename(context: Context): String {
    val date = Date();
    val sdf = SimpleDateFormat("_yyyyMMdd_HHmmss")
    val sb = StringBuilder(sdf.format(date))
    val homeDirExternal = getHomeDirExternal(context)
    if (homeDirExternal.isEmpty()) return ""
    val filename = "$homeDirExternal/$FILE_NAME$sb.txt"
    saveDebugLogFilename(context, filename)
    return filename
}

/**
 * Application external home folder where various logs are stored.
 * It can be accessed via pc, from other apps and the user
 */
private fun getHomeDirExternal(context: Context): String {
    return context.getExternalFilesDir(null)?.path ?: ""
}

private fun saveDebugLogFilename(context : Context, filename : String) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = preferences.edit()
    editor.putString(DEBUG_LOG_EDITOR_KEY, filename)
    editor.apply()
}