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
import com.zoiper.zdk.Types.CallLineStatus
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_NUMBER
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.util.DensityPixelUtils
import com.zoiper.zdk.android.demokt.util.TextViewSelectionUtils
import com.zoiper.zdk.android.demokt.util.TimerDuration
import kotlinx.android.synthetic.main.activity_in_call.*
import kotlinx.android.synthetic.main.in_call_button_layout.*

class InCallActivity : BaseActivity(), CallEventsHandler {

    private var call: Call? = null

    private val recordingFilePath by lazy {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }

    private val timerDuration: TimerDuration by lazy {
        TimerDuration(
            mainHandler,
            inCallTvTimer
        )
    }

    private val account by lazy{
        val longExtra = intent.getLongExtra(INTENT_EXTRA_ACCOUNT_ID, -1)
        getAccount(longExtra)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_call)

        setSupportActionBar(inCallToolbar)

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
        inCallFab.setOnClickListener { call?.hangUp() }

        inCallBtnMute.setOnClickListener { toggleMute(it as TextView, it.isSelected) }
        inCallBtnHold.setOnClickListener { toggleHold(it as TextView, it.isSelected) }
        inCallBtnRecord.setOnClickListener { toggleRecord(it as TextView, it.isSelected) }
        inCallBtnSpeaker.setOnClickListener { toggleSpeaker(it as TextView, it.isSelected) }

        inCallBtnTransfer.setOnClickListener { transferCall() }
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
            null -> Log.d("CallLineStatus", "null ")
        }
    }

    private fun printStatusThreadSafe(message: String) = runOnUiThread { inCallTvCallState.text = message }
}