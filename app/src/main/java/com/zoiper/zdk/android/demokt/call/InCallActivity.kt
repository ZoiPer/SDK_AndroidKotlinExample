package com.zoiper.zdk.android.demokt.call

import android.app.AlertDialog
import android.media.AudioManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import com.zoiper.zdk.Call
import com.zoiper.zdk.CallStatus
import com.zoiper.zdk.EventHandlers.CallEventsHandler
import com.zoiper.zdk.ExtendedError
import com.zoiper.zdk.Types.CallLineStatus
import com.zoiper.zdk.Types.CallMediaChannel
import com.zoiper.zdk.Types.CallSecurityLevel
import com.zoiper.zdk.Types.ResultCode
import com.zoiper.zdk.Types.Zrtp.*
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_NUMBER
import com.zoiper.zdk.android.demokt.ZDKTESTING
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.databinding.ActivityInCallBinding
import com.zoiper.zdk.android.demokt.util.DensityPixelUtils
import com.zoiper.zdk.android.demokt.util.TextViewSelectionUtils
import com.zoiper.zdk.android.demokt.util.TimerDuration

class InCallActivity : BaseActivity(), CallEventsHandler {

    private lateinit var viewBinding: ActivityInCallBinding
    private var call: Call? = null

    private val recordingFilePath by lazy {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    private val timerDuration: TimerDuration by lazy {
        TimerDuration(
            mainHandler,
            viewBinding.inCallTvTimer
        )
    }

    private val account by lazy{
        val longExtra = intent.getLongExtra(INTENT_EXTRA_ACCOUNT_ID, -1)
        getAccount(longExtra)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityInCallBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.inCallToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "In call"

        setupViews()
    }

    override fun onZoiperLoaded() {
        intent.getStringExtra(INTENT_EXTRA_NUMBER)
            ?.let{ account?.createCall(it, true, false) }
            ?.apply { setAudioMode(AudioManager.MODE_IN_COMMUNICATION) }
            ?.apply { setCallStatusListener(this@InCallActivity) }
            ?.also { call = it }
    }

    private fun setupViews() {
        viewBinding.inCallFab.setOnClickListener { call?.hangUp() }

        viewBinding.inCallBtnLayout.inCallBtnMute.setOnClickListener { toggleMute(it as TextView, it.isSelected) }
        viewBinding.inCallBtnLayout.inCallBtnHold.setOnClickListener { toggleHold(it as TextView, it.isSelected) }
        viewBinding.inCallBtnLayout.inCallBtnRecord.setOnClickListener { toggleRecord(it as TextView, it.isSelected) }
        viewBinding.inCallBtnLayout.inCallBtnSpeaker.setOnClickListener { toggleSpeaker(it as TextView, it.isSelected) }

        viewBinding.inCallBtnLayout.inCallBtnTransfer.setOnClickListener { transferCall() }
    }

    private fun transferCall() {
        // Edit text to enter number.
        val input = EditText(this).apply {
            hint = "Enter number"

            layoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            DensityPixelUtils
                .convertDpToPixel(20f, this@InCallActivity)
                .toInt()
                .let { setPadding(it, it, it, it) }
        }

        // Dialog self
        AlertDialog.Builder(this)
            .setMessage("Transfer call to: ")
            .setPositiveButton("Transfer") { _, _ ->
                call?.blindTransfer(input.text.toString().trim { it <= ' ' })
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create().apply{
                setView(input)
                show()
            }
    }

    private fun toggleRecord(view: View, selected: Boolean) {
        val newSelectedState = !selected

        TextViewSelectionUtils.setTextViewSelected(view as TextView, newSelectedState)

        if (newSelectedState) {
            call?.recordFileName("$recordingFilePath/zdk_record_${System.currentTimeMillis()}.wav")
            call?.startRecording()
            Toast.makeText(this, "Recording call", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "File saved in ${call?.recordFileName()}", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleHold(view: View, selected: Boolean) {
        val newSelectedState = !selected
        call?.held(newSelectedState)
        TextViewSelectionUtils.setTextViewSelected(view as TextView, newSelectedState)
    }

    private fun toggleSpeaker(view: TextView, selected: Boolean) {
        call?.onSpeaker(!selected)
        TextViewSelectionUtils.setTextViewSelected(view, !selected)
    }

    private fun toggleMute(view: TextView, selected: Boolean) {
        call?.muted(!selected)
        TextViewSelectionUtils.setTextViewSelected(view, !selected)
    }

    override fun onCallStatusChanged(call: Call?, status: CallStatus?) {
        // Set the call status with handler.
        printStatusThreadSafe(status?.lineStatus().toString())

        when(status?.lineStatus()){
            CallLineStatus.Terminated -> runOnUiThread {
                setAudioMode(AudioManager.MODE_NORMAL)
                timerDuration.cancel()
            }
            CallLineStatus.Active -> runOnUiThread {
                timerDuration.start()
            }
            CallLineStatus.NA -> Log.d("CallLineStatus", "NA ")
            CallLineStatus.Dialing -> Log.d("CallLineStatus", "Connecting ")
            CallLineStatus.Failed -> Log.d("CallLineStatus", "Failed ")
            CallLineStatus.Ringing -> Log.d("CallLineStatus", "Ringing ")
            CallLineStatus.Held -> Log.d("CallLineStatus", "Held ")
            CallLineStatus.EarlyMedia -> Log.d("CallLineStatus", "EarlyMedia ")
            null -> Log.d("CallLineStatus", "null ")
        }
    }

    override fun onCallZrtpFailed(call: Call?, error: ExtendedError?) {
        Log.d(ZDKTESTING, "onCallZrtpFailed: call= " + call!!.callHandle() + "; error= " + error?.message())
    }

    override fun onCallZrtpSuccess(call: Call?, zidHex: String?, knownPeer: Int, cacheMismatch: Int, peerKnowsUs: Int, zrtpsasEncoding: ZRTPSASEncoding?,
                                   sas: String?, zrtpHashAlgorithm: ZRTPHashAlgorithm?, zrtpCipherAlgorithm: ZRTPCipherAlgorithm?, zrtpAuthTag: ZRTPAuthTag?, zrtpKeyAgreement: ZRTPKeyAgreement? ) {
        Log.d(ZDKTESTING, "onCallZrtpSuccess: call= " + call!!.callHandle())

        if (knownPeer != 0 && cacheMismatch == 0 && peerKnowsUs != 0) {
            runOnUiThread { call!!.confirmZrtpSas(true) }
        } else {
            runOnUiThread {
                AlertDialog.Builder(this)
                        .setTitle("SAS Verification")
                        .setMessage("SAS Verification is \"$sas\". Please compare the string with your peer!")
                        .setPositiveButton("Confirm") { dialog, which -> call!!.confirmZrtpSas(true) }
                        .setNegativeButton("Reject") { dialog, which -> call!!.confirmZrtpSas(false) }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
            }
        }
    }

    override fun onCallZrtpSecondaryError(call: Call?, channel: Int, error: ExtendedError?) {
        Log.d(ZDKTESTING, "onCallZrtpFailed: call= " + call!!.callHandle() + "; error= " + error?.message())
    }

    override fun onCallSecurityLevelChanged(call: Call?, channel: CallMediaChannel, level: CallSecurityLevel) {
        Log.d(ZDKTESTING, "OnCallSecurityLevelChanged channel: $channel level: $level")
    }

    private fun printStatusThreadSafe(message: String) = runOnUiThread { viewBinding.inCallTvCallState.text = message }
}