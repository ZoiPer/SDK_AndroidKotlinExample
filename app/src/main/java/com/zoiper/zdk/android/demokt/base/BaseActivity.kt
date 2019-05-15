package com.zoiper.zdk.android.demokt.base

import android.content.Context
import android.media.AudioManager
import android.support.v7.app.AppCompatActivity
import com.zoiper.zdk.Account
import com.zoiper.zdk.android.demokt.ZDKDemoApplication
import com.zoiper.zdk.android.demokt.util.PermissionHelper

/**
 *BaseActivity
 *
 *@since 15/03/2019
 */
abstract class BaseActivity : AppCompatActivity(){
    // ------------ Properties ---------------------------------
    protected val zdkApplication by lazy {
        val application = application
        when (application) {
            is ZDKDemoApplication -> application
            else -> throw RuntimeException("You forgot to register your application class in the manifest, didn't you?")
        }
    }

    protected val mainHandler by lazy { zdkApplication.mainHandler }
    protected val zdkContext by lazy { zdkApplication.zdkContext }

    private val permissionHelper by lazy { PermissionHelper(this) }

    // ------------ OnWhatever callbacks -----------------------

    /**
     * Called when the ZDK context fails to activate and intentFilter in time.
     * This is where you would notify the user that something has gone horribly wrong.
     *
     * You can find out more about what exactly went wrong
     * upon the initialization of zdk at [ZDKDemoApplication.initializeZoiperContext]
     */
    open fun onZoiperTimeout(){}

    /**
     * Called when the ZDK context is loaded and ready for use
     */
    open fun onZoiperLoaded(){}

    override fun onStart(){
        super.onStart()
        // Check for the permissions and tha
        permissionHelper.assurePermissions(this::onPermissionsReady)
    }

    private fun onPermissionsReady(){
        listenForZdkContextLoaded()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.onRequestPermissionsResult(requestCode)
    }

    // ------------ Functions that actually do things ----------
    /**
     * Since you're not sure if the zdk context is activated when you might need it, we've
     * set up an example way for you to assurePermissions and receive the context only when you're
     * 100% sure that it is in fact active and usable.
     * You would ideally want this in your BaseActivity
     */
    private fun listenForZdkContextLoaded(){
        if(zdkApplication.isZdkActivated){
            // Optimization - if zoiper was already loaded, skip spawning a new thread
            this.onZoiperLoaded()
            return
        }

        // Wait for the zdk context to get activated on a background thread
        ZdkContextWaitThread().start()
    }

    // ------------ Inner classes ------------------------------
    private inner class ZdkContextWaitThread : Thread() {
        private val ACTIVATION_TIMEOUT = 5 * 1000 // 5 second timeout

        init {
            this.name = "ZdkContextWaitThread"
        }

        override fun run() {
            val startingPoint = System.currentTimeMillis()
            while (!zdkApplication.isZdkActivated) {
                try {
                    if (System.currentTimeMillis() - startingPoint > ACTIVATION_TIMEOUT) {
                        mainHandler.post { this@BaseActivity.onZoiperTimeout() }
                        return
                    }
                    sleep(100)
                } catch (ignored: InterruptedException) {
                    // In case the thread is interrupted before there is a result, notify failure
                    mainHandler.post { this@BaseActivity.onZoiperTimeout() }
                }

            }
            // Execute successful callback
            mainHandler.post { this@BaseActivity.onZoiperLoaded() }
        }
    }

    fun setAudioMode(mode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = mode
    }

    /**
     * Gets the account associated with the ID.
     *
     * @return The current account associated with the ID
     */
    fun getAccount(accountID: Long): Account? {
        val accountProvider = zdkContext.accountProvider()
        return accountProvider?.getAccount(accountID)
    }
}