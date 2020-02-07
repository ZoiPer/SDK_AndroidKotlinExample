package com.zoiper.zdk.android.demokt.incoming

import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.View
import com.zoiper.zdk.*
import com.zoiper.zdk.EventHandlers.AccountEventsHandler
import com.zoiper.zdk.EventHandlers.CallEventsHandler
import com.zoiper.zdk.Types.AccountStatus
import com.zoiper.zdk.Types.CallLineStatus
import com.zoiper.zdk.Types.OwnershipChange
import com.zoiper.zdk.Types.Zrtp.*
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.ZDKTESTING
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.util.TextViewSelectionUtils
import kotlinx.android.synthetic.main.activity_incoming_call.*
import kotlinx.android.synthetic.main.content_incoming_call.*

/**
 * IncomingCallActivity
 *
 * @since 1.2.2019 г.
 */
class IncomingCallActivity : BaseActivity(), AccountEventsHandler, CallEventsHandler {
    private val account by lazy{
        val longExtra = intent.getLongExtra(INTENT_EXTRA_ACCOUNT_ID, -1)
        getAccount(longExtra)
    }

    private var call: Call? = null

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        setSupportActionBar(incomingToolbar)

        supportActionBar?.apply{
            title = "Incoming call"
            setDisplayHomeAsUpEnabled(true)
        }

        incomingTvSpeaker.isSelected = false
        incomingTvMute.isSelected = false
    }

    override fun onZoiperLoaded() {
        account?.setStatusEventListener(this)
    }

    private fun setupIncomingCallView() {
        incomingTvWaiting.visibility = View.GONE
        incomingRlBase.visibility = View.VISIBLE

        incomingTvSpeaker.setOnClickListener(this::onSpeakerClicked)
        incomingTvMute.setOnClickListener (this::onMuteClicked)
        incomingBtnAnswer.setOnClickListener { onAnswerButtonClicked() }
        incomingBtnHangup.setOnClickListener { onHangupButtonClicked() }
    }

    private fun onHangupButtonClicked() {
        call?.hangUp()
        setAudioMode(AudioManager.MODE_NORMAL)
    }

    private fun onAnswerButtonClicked() {
        call?.acceptCall()
    }

    private fun onMuteClicked(view: View) {
        call?.let {
            if (view is AppCompatTextView) {
                val newSelectedState = !view.isSelected
                it.muted(newSelectedState)
                TextViewSelectionUtils.setTextViewSelected(view, newSelectedState)
            }
        }
    }

    private fun onSpeakerClicked(view: View) {
        Log.d(ZDKTESTING, "speaker clicked")

        call?.let {
            if (view is AppCompatTextView) {
                val newSelectedState = !view.isSelected
                Log.d(ZDKTESTING, "onSpeaker() = ${it.onSpeaker()}")
                Log.d(ZDKTESTING, "onSpeaker($newSelectedState)")
                it.onSpeaker(newSelectedState)
                TextViewSelectionUtils.setTextViewSelected(view, newSelectedState)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        account?.dropStatusEventListener(this)
    }

    override fun onResume() {
        super.onResume()
        account?.setStatusEventListener(this)
    }

    /**
     * @param call The incoming call.
     */
    private fun onIncomingCall(call: Call?) {
        call?.muted()

        this.call = call

        ring()
        incomingTvStatus.text = getString(R.string.ringing)
        this.call?.setCallStatusListener(this)
        setupIncomingCallView()
        // Set caller name.
        val incomingName = getString(
            R.string.incoming_call_from,
            "\n${call?.calleeName()}(${call?.calleeNumber()})"
        )
        incomingTvFrom.text = incomingName
        setAudioMode(AudioManager.MODE_IN_COMMUNICATION)
    }

    private fun ring() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        RingtoneManager
            .getRingtone(this, uri)
            ?.play()
    }

    override fun onAccountStatusChanged(account: Account?, accountStatus: AccountStatus?, i: Int) {

    }

    override fun onAccountRetryingRegistration(account: Account?, i: Int, i1: Int) {

    }

    override fun onAccountIncomingCall(account: Account?, call: Call?) {
        call?.muted()
        runOnUiThread { onIncomingCall(call) }
    }

    override fun onAccountChatMessageReceived(account: Account?, s: String?, s1: String?) {

    }

    override fun onAccountExtendedError(account: Account?, extendedError: ExtendedError?) {

    }

    override fun onAccountUserSipOutboundMissing(account: Account?) {

    }

    override fun onAccountCallOwnershipChanged(
        account: Account?,
        call: Call?,
        ownershipChange: OwnershipChange?
    ) {

    }

    override fun onCallStatusChanged(call: Call?, callStatus: CallStatus?) {
        if (callStatus?.lineStatus() != CallLineStatus.Ringing) {
            runOnUiThread { ringtone?.stop() }
        }

        runOnUiThread { incomingTvStatus.text = callStatus?.lineStatus().toString() }
    }

    override fun onCallExtendedError(call: Call?, extendedError: ExtendedError?) {

    }

    override fun onCallNetworkStatistics(call: Call?, networkStatistics: NetworkStatistics?) {

    }

    override fun onCallNetworkQualityLevel(call: Call?, i: Int, i1: Int) {

    }

    override fun onCallTransferSucceeded(call: Call?) {

    }

    override fun onCallTransferFailure(call: Call?, extendedError: ExtendedError?) {

    }

    override fun onCallTransferStarted(call: Call?, s: String?, s1: String?, s2: String?) {

    }

    override fun onCallZrtpFailed(call: Call?, extendedError: ExtendedError?) {

    }

    override fun onCallZrtpSuccess(
        call: Call?,
        s: String?,
        i: Int,
        i1: Int,
        i2: Int,
        zrtpsasEncoding: ZRTPSASEncoding?,
        s1: String?,
        zrtpHashAlgorithm: ZRTPHashAlgorithm?,
        zrtpCipherAlgorithm: ZRTPCipherAlgorithm?,
        zrtpAuthTag: ZRTPAuthTag?,
        zrtpKeyAgreement: ZRTPKeyAgreement?
    ) {

    }

    override fun onCallZrtpSecondaryError(call: Call?, i: Int, extendedError: ExtendedError?) {

    }
}
