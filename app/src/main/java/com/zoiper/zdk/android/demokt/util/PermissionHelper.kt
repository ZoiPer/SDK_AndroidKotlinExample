package com.zoiper.zdk.android.demokt.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

/**
 *PermissionHelper
 *
 *@since 15/03/2019
 */
class PermissionHelper(private val activity: Activity) {

    private var readyCallback : (() -> Unit)? = null

    fun assurePermissions(readyCallback: () -> Unit){
        this.readyCallback = readyCallback

        when( hasAllPermissions() ){
            false -> askForPermissions()
            true -> readyCallback.invoke()
        }
    }

    fun onRequestPermissionsResult(requestId: Int) {
        if (requestId != PERMISSIONS_REQUEST_ID) return

        when( hasAllPermissions() ){
            true -> this.readyCallback?.invoke()
            false -> Toast.makeText(activity, "You did not provide permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        val status = ContextCompat.checkSelfPermission(activity, permission)
        return status == PackageManager.PERMISSION_GRANTED
    }

    private fun askForPermissions() {
        ActivityCompat.requestPermissions(activity, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
        ), PERMISSIONS_REQUEST_ID)
    }

    private fun hasAllPermissions() = hasPermission(Manifest.permission.CAMERA) &&
                                        hasPermission(Manifest.permission.CALL_PHONE) &&
                                        hasPermission(Manifest.permission.RECORD_AUDIO)

    companion object {
        private const val PERMISSIONS_REQUEST_ID = 1
    }
}