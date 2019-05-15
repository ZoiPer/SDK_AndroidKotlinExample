package com.zoiper.zdk.android.demokt.util

import android.content.Context
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 *Credentials
 *
 *@since 15/03/2019
 */
class Credentials private constructor(val username: String, val password: String) {
    companion object {
        private const val CREDENTIALS_FILE_NAME = "credentials.json"

        private fun loadJSONFromAsset(context: Context): String {
            try {
                val inputStream = context.assets.open(CREDENTIALS_FILE_NAME)
                val buffer = ByteArray(inputStream.available())

                inputStream.read(buffer)
                inputStream.close()

                return String(buffer, Charsets.UTF_8)
            } catch (e: IOException) {
                throw IllegalArgumentException("Unable to read json file", e)
            }
        }

        fun load(context: Context): Credentials {
            val jsonString = loadJSONFromAsset(context)
            try {
                val jsonObject = JSONObject(jsonString)
                return Credentials(
                    jsonObject.getString("username"),
                    jsonObject.getString("password")
                )
            } catch (e: JSONException) {
                throw IllegalArgumentException("Unable to parse json file :/", e)
            }
        }
    }
}