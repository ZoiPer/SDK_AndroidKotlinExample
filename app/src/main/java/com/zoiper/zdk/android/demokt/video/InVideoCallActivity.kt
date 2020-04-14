package com.zoiper.zdk.android.demokt.video

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import com.zoiper.zdk.Account
import com.zoiper.zdk.Call
import com.zoiper.zdk.EventHandlers.VideoRendererEventsHandler
import com.zoiper.zdk.Types.VideoFrameFormat
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_NUMBER
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.ZDKTESTING
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.video.out.I420Helper
import kotlinx.android.synthetic.main.activity_in_video_call.*
import java.util.*


/**
 * InVideoCallActivity
 *
 * @since 31/01/2019
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class InVideoCallActivity : BaseActivity() {

    private val captureDimensions by lazy {
        I420Helper.ImageDimensions(640, 480)
    }

    // Multithreading
    private val bgHandler by lazy {
        val background = HandlerThread("Background")
        background.start()
        Handler(background.looper)
    }

    // ZDK stuff
    private var call: Call? = null

    // Camera stuff
    private val cameraManager: CameraManager by lazy {
        getSystemService(CAMERA_SERVICE) as CameraManager
    }

    private val imageReader: ImageReader by lazy {
        ImageReader.newInstance(
            captureDimensions.width,
            captureDimensions.height,
            ImageFormat.YUV_420_888,
            2
        )
    }

    // Image pre-processing
    private var i420Helper: I420Helper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_in_video_call)

        initLifecycleObservers()
        initImageReader()
    }

    override fun onZoiperLoaded() {
        try {
            chooseCamera()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun onImageAvailable(reader: ImageReader) {
        call?.let {call ->
            reader.acquireLatestImage()?.let { image ->
                i420Helper
                    ?.straightenFrame(I420Helper.imageToI420ByteArray(image))
                    ?.let { bytes -> call.sendVideoFrame(bytes, bytes.size, VideoFrameFormat.YUV420p) }

                image.close()
            }
        }
    }

    private fun initImageReader() {
        imageReader.setOnImageAvailableListener(this::onImageAvailable, bgHandler)
    }

    /**
     * Looks for number, accountID in the intent and if found,
     * get the account, create a call or find it and BIND listeners on it
     */
    private fun bindCall() {
        if (intent.hasExtra(INTENT_EXTRA_ACCOUNT_ID) && intent.hasExtra(INTENT_EXTRA_NUMBER)) {
            val accountId = intent.getLongExtra(INTENT_EXTRA_ACCOUNT_ID, -1)
            val number = intent.getStringExtra(INTENT_EXTRA_NUMBER)

            getAccount(accountId)?.let {account ->
                val calls = queryActiveCalls(account)

                call = createOrGetCall(account, calls, number)

                call?.setCallStatusListener(CallEventsHandler(this))
                call?.setVideoCallNotificiationsListener(CallEventsHandler(this))

                call?.setVideoRendererNotificationsListener(object : VideoRendererEventsHandler {
                    override fun onVideoFrameReceived(pBuffer: ByteArray?, length: Int, width: Int, height: Int) {
                        pBuffer?.let {
                            videoCallSvIn.renderI420YUV(it, width, height)
                        }
                    }
                })
            }
        }
    }

    private fun queryActiveCalls(account: Account) = account.activeCalls

    private fun createOrGetCall(account: Account, calls: List<Call>, number: String): Call {
        return if (calls.isNotEmpty()) calls[0] else account.createCall(number, true, true)
    }

    private fun deviceOrientation(): Int {
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val rotation = display.rotation
        if (rotation == Surface.ROTATION_0) return 0
        if (rotation == Surface.ROTATION_90) return 90
        if (rotation == Surface.ROTATION_180) return 180
        return if (rotation == Surface.ROTATION_270) 270 else -1
    }

    private fun initLifecycleObservers() {
        lifecycle.addObserver(videoCallSvIn)
    }

    internal fun printStatusThreadSafe(status: String) = mainHandler.post { videoCallTvStatus?.text = status }
    internal fun printNetworkThreadSafe(status: String) = mainHandler.post { videoCallTvNetwork.text = status }

    internal fun printGeneralThreadSafe(text: String) = mainHandler.post{ Log.i(ZDKTESTING, text) }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun chooseCamera() {
        cameraManager
            .cameraIdList
            .let { cameraIdList ->
                for (cid in cameraIdList) {
                    val cameraCharacteristics = cameraManager.getCameraCharacteristics(cid)

                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                        if(sensorOrientation != null) initCamera(cid, sensorOrientation)
                    }
                }
            }
    }

    private fun initI420Helper(sensorOrientation: Int) {
        I420Helper(sensorOrientation, deviceOrientation(), captureDimensions).let {
            zdkContext
                .videoControls()
                .setFormat(it.postRotateDimensions.width, it.postRotateDimensions.height, 30f)

            i420Helper = it
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(CameraAccessException::class)
    private fun initCamera(cameraId: String, sensorOrientation: Int) {
        initI420Helper(sensorOrientation)

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                try {
                    this@InVideoCallActivity.cameraStarted(camera)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

            override fun onDisconnected(camera: CameraDevice) {
                Toast.makeText(this@InVideoCallActivity, "Camera disconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Toast.makeText(this@InVideoCallActivity, "Camera error: $error", Toast.LENGTH_SHORT).show()
            }
        }, null)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun cameraStarted(camera: CameraDevice) {
        val surfaces = ArrayList<Surface>()

        surfaces.add(videoCallSvOut.holder.surface)
        imageReader.surface?.let { surfaces.add(it) }

        camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    captureSessionCreated(camera, session)
                } catch (e: CameraAccessException) {
                    Toast.makeText(this@InVideoCallActivity, "Capture session error", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                return
            }
        }, bgHandler)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun captureSessionCreated(camera: CameraDevice, session: CameraCaptureSession) {
        val crb = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)

        crb.addTarget(videoCallSvOut.holder.surface)
        imageReader.surface?.let { crb.addTarget(it) }

        val captureRequest = crb.build()

        session.setRepeatingRequest(captureRequest, null, bgHandler)

        bindCall()
    }

    override fun onDestroy() {
        super.onDestroy()
        call?.let {
            if(isFinishing){
                it.hangUp()
            }
            it.dropAllEventListeners()
        }
    }
}
