package com.zoiper.zdk.android.demokt

import android.app.Application
import android.os.Handler
import android.util.Log
import com.github.anrwatchdog.ANRWatchDog
import com.zoiper.zdk.Account
import com.zoiper.zdk.ActivationResult
import com.zoiper.zdk.Context
import com.zoiper.zdk.EventHandlers.ContextEventsHandler
import com.zoiper.zdk.SecureCertData
import com.zoiper.zdk.Types.LoggingLevel
import com.zoiper.zdk.Types.ResultCode
import com.zoiper.zdk.Types.TLSSecureSuiteType
import com.zoiper.zdk.android.demokt.network.NetworkChangeReceiver
import com.zoiper.zdk.android.demokt.util.Credentials
import java.io.File

/**
 *ZDKDemoApplication
 *
 *@since 15/03/2019
 */
@Suppress("ConstantConditionIf")
class ZDKDemoApplication : Application(), ContextEventsHandler{

    @Volatile var isZdkActivated = false

    val zdkContext: Context by lazy( this::makeZoiperContext )
    val mainHandler: Handler by lazy { Handler(mainLooper) }

    var account: Account? = null

    private val zrtpCacheFile by lazy { File(filesDir, "cache_zrtp") }
    private val certCacheFile by lazy { File(filesDir, "cache_cert") }

    override fun onCreate() {
        super.onCreate()

        ANRWatchDog().start()

        initializeZoiperContext()
    }

    /**
     * This method will get called when you have an error with your ZDK instance activation
     * and this is where you will get the reason for the error.
     *
     * !!!!!!!!!!!!!!!!DANGER!!!!!!!!!!!!!!
     * !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
     * This method is CALLED ON THE ZDK THREAD.
     * Consider using mainHandler.post{} inside
     * it to execute code on the main thread
     */
    override fun onContextSecureCertError(context: Context?, secureCert: SecureCertData?) {
        TODO("PLEASE IMPLEMENT ME ADEQUATELY!")
    }

    /**
     * !!!!!!!!!!!!!!!!DANGER!!!!!!!!!!!!!!
     * !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
     *
     * This method is CALLED ON THE ZDK THREAD.
     * Consider using mainHandler.post{} inside
     * it to execute code on the main thread
     */
    override fun onContextActivationCompleted(context: Context?, activationResult: ActivationResult?) {

        Log.d("VesiTesting", "I am still alive 1 - ${activationResult!!.status()}")

        Thread {

            Log.d("VesiTesting", "I am still alive 2")

            activationResult.status() // This is where it crashes

            Log.d("VesiTesting", "I am still alive 3")

        }.start()

        Log.d("VesiTesting", "I am still alive 4")
    }

//        {
//            val opalanka = activationResult
//
//            Log.d("VesiTesting", "I am still alive 4")
//
//            opalanka?.status()
//
//            Log.d("VesiTesting", "I am still alive 5")
//        }
//        Log.d("VesiTesting", "I am still alive 6")

//        when(activationResult?.status()){
//            // !!!!!!!!!!!!!!!!DANGER!!!!!!!!!!!!!!
//            // !!!!!!!!!!!SERIOUS NOTICE!!!!!!!!!!!
//            // Notice how we call mainHandler.post, thus transferring the callback to the main thread
//            // because onContextActivationCompleted() is called ON THE ZDK THREAD!!!!
//            ActivationStatus.Success -> mainHandler.post{ activationSuccess() }
//
//            ActivationStatus.Unparsable -> TODO("Implement me!!")
//            ActivationStatus.FailedDecrypt -> TODO("Implement me!!")
//            ActivationStatus.Failed -> TODO("Implement me!!")
//            ActivationStatus.FailedDeadline -> TODO("Implement me!!")
//            ActivationStatus.FailedChecksum -> TODO("Implement me!!")
//            ActivationStatus.FailedId -> TODO("Implement me!!")
//            ActivationStatus.FailedCache -> TODO("Implement me!!")
//            ActivationStatus.FailedHttp -> TODO("Implement me!!")
//            ActivationStatus.FailedCurl -> TODO("Implement me!!")
//            ActivationStatus.FailedSignCheck -> TODO("Implement me!!")
//            ActivationStatus.Expired -> TODO("Implement me!!")
//        }
//    }

    private fun activationSuccess() {
        val startingResult = zdkContext.startContext()

        // If the ZDK Context activation process went ok,
        // we set isZdkActivated to true thus allowing all the waiting activities
        // use the context
        isZdkActivated = startingResult.code() == ResultCode.Ok

        if(isZdkActivated){
            // After activation is complete, start notifying the ZDK context about network changes
            this.registerReceiver(
                NetworkChangeReceiver(zdkContext),
                NetworkChangeReceiver.intentFilter()
            )
        }
    }

    private fun initializeZoiperContext(){
        zdkContext.setStatusListener(this)
        ActivatorThread().start()
    }

    private fun makeZoiperContext(): Context {
        try {
            val zdkContext = Context(applicationContext)
            Log.d(VESITESTING, "zdkContext = new Context(applicationContext)")

            if (ENABLE_LIB_DEBUG_LOG) {
                zdkContext.logger().logOpen(
                    File(filesDir, "logs.txt").absolutePath,
                    null,
                    LoggingLevel.Debug,
                    0
                )
            }

            // Make sure you have both
            // ACCESS_NETWORK_STATE
            // and
            // INTERNET
            // permissions!!!!!!!!!!!
            zdkContext.configuration().sipPort(5060)
            //zdkContext.configuration().iaxPort();
            //zdkContext.configuration().rtpPort();

            zdkContext.configuration().enableSIPReliableProvisioning(false)
            zdkContext.encryptionConfiguration().tlsConfig().secureSuite(TLSSecureSuiteType.SSLv2_v3)
            zdkContext.encryptionConfiguration().globalZrtpCache(zrtpCacheFile.absolutePath)

            return zdkContext
        } catch (e: UnsatisfiedLinkError) {
            throw RuntimeException(e)
        }
    }

    /**
     * The activation process takes a bit more time,
     * thus we need a separate thread for it
     */
    private inner class ActivatorThread internal constructor() : Thread() {
        init {
            this.name = "ActivatorThread" + this.id
        }

        override fun run() {
            // Cert cache file, put it where ever you please.
            // For our example we will put it in our app's data directory
            // Load the credentials.
            val credentials = Credentials.load(applicationContext)

            // It is wise to offload that to a background thread because it takes some time
            val startSDK = zdkContext.activation().startSDK(
                certCacheFile.absolutePath,
                credentials.username,
                credentials.password
            )
            Log.d(VESITESTING, "zdkContext.activation().startSDK() = "+startSDK.text())
        }
    }

    companion object {
        private const val ENABLE_LIB_DEBUG_LOG = true
    }
}