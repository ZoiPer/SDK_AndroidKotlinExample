package com.zoiper.zdk.android.demokt.dtmf

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import com.zoiper.zdk.*
import com.zoiper.zdk.Configurations.AccountConfig
import com.zoiper.zdk.Configurations.SIPConfig
import com.zoiper.zdk.EventHandlers.AccountEventsHandler
import com.zoiper.zdk.EventHandlers.CallEventsHandler
import com.zoiper.zdk.Providers.AccountProvider
import com.zoiper.zdk.Types.*
import com.zoiper.zdk.Types.Zrtp.*
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_NUMBER
import com.zoiper.zdk.android.demokt.ZDKTESTING
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.databinding.ActivityDtmfBinding
import java.lang.IllegalArgumentException

class DTMFActivity : BaseActivity(), CallEventsHandler, AccountEventsHandler {
    private lateinit var account: Account
    private lateinit var viewBinding: ActivityDtmfBinding

    private val number by lazy { intent.getStringExtra(INTENT_EXTRA_NUMBER) ?: "" }

    private lateinit var call: Call
    private var callStarted = false

    /**
     * Returns a List with all the Audio codecs.
     *
     * @return List with audio codecs.
     */
    private val audioCodecs = arrayListOf(
        AudioVideoCodecs.OPUS_WIDE,
        AudioVideoCodecs.PCMU,
        AudioVideoCodecs.vp8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDtmfBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.dtmfToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "DTMF"

        setListeners()
    }

    private fun setListeners() {
        listOf(
            viewBinding.content.dtmfBtn1, viewBinding.content.dtmfBtn2, viewBinding.content.dtmfBtn3,
            viewBinding.content.dtmfBtn4, viewBinding.content.dtmfBtn5, viewBinding.content.dtmfBtn6,
            viewBinding.content.dtmfBtn7, viewBinding.content.dtmfBtn8, viewBinding.content.dtmfBtn9,
            viewBinding.content.dtmfBtnStar, viewBinding.content.dtmfBtn0, viewBinding.content.dtmfBtnHash
        ).forEach { it.setOnClickListener(this::onDialerClick) }

        viewBinding.dtmfFabHangup.setOnClickListener { hangup() }
        viewBinding.content.dtmfBtnDelete.setOnClickListener { deleteDigit() }
    }

    override fun onZoiperLoaded() = initAccount()

    @SuppressLint("SetTextI18n")
    private fun startCall() {
        if (callStarted || number.isEmpty()) return
        callStarted = true

        call = account.createCall(number, true, false)

        viewBinding.dtmfFabHangup.show()
        viewBinding.content.dtmfTvCallStatus.text = "Created"

        call.setCallStatusListener(this@DTMFActivity)
        setAudioMode(AudioManager.MODE_IN_COMMUNICATION)
    }

    private fun onCallActive(){
        viewBinding.content.dtmfClRinging.visibility = View.GONE
        viewBinding.content.dtmfClDialer.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun initAccount() {
        val accountProvider = zdkContext.accountProvider()
        val sipConfig = createSipConfig(accountProvider)
        val accConfig = createAccountConfig(accountProvider, sipConfig)

        viewBinding.content.dtmfTvAccountStatus.text = "Creating account"
        account = accountProvider.createUserAccount()

        viewBinding.content.dtmfTvAccountStatus.text = "Configuring"
        account.setStatusEventListener(this@DTMFActivity)

        account.accountName(USERNAME)
        account.mediaCodecs(audioCodecs)

        // Always set acc configuration like this, acc.configuration() returns copy of the object
        // and everything you set in it, wont matter.
        account.configuration(accConfig)

        viewBinding.content.dtmfTvAccountStatus.text = "Creating user"
        account.createUser()

        viewBinding.content.dtmfTvAccountStatus.text = "Registering"
        account.registerAccount()
    }

    private fun createAccountConfig(
        accountProvider: AccountProvider,
        sipConfig: SIPConfig
    ): AccountConfig{
        val accountConfig = accountProvider.createAccountConfiguration()

        accountConfig.userName(USERNAME)
        accountConfig.password(PASSWORD)
        accountConfig.type(ProtocolType.SIP)
        accountConfig.reregistrationTime(60)
        accountConfig.dtmfBand(DTMF_TYPE)
        accountConfig.dtmfAutoplayDevice(AudioOutputDeviceType.Normal)

        accountConfig.sip(sipConfig)

        return accountConfig
    }

    private fun createSipConfig(accountProvider: AccountProvider): SIPConfig {
        val sipConfig = accountProvider.createSIPConfiguration()

        sipConfig.transport(TransportType.UDP)
        sipConfig.domain(DOMAIN)
        sipConfig.rPort(RPortType.SignalingAndMedia)

        return sipConfig
    }

    /**
     * Hangup the call. If the call is already terminated, finish the activity.
     */
    private fun hangup() {
        if (call.status().lineStatus() != CallLineStatus.Terminated) {
            call.hangUp()
        }
    }

    private fun deleteDigit() {
        val length = viewBinding.content.dtmfEtNumber.text.length
        if (length > 0) {
            viewBinding.content.dtmfEtNumber.text.delete(length - 1, length)
        }
    }

    private fun enterDigit(digit: String) {
        try {
            val dtmfCode = DTMFCodes.fromInt(Integer.valueOf(digit))
            call.sendDTMF(dtmfCode)

            viewBinding.content.dtmfEtNumber.append(digit)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun onDialerClick(view: View) {
        if(view is Button){
            enterDigit(view.text.toString())
        }
    }

    override fun onCallStatusChanged(call: Call?, status: CallStatus?) {
        val lineStatus = status?.lineStatus()

        when(status?.lineStatus()){
            CallLineStatus.Terminated -> runOnUiThread {
                setAudioMode(AudioManager.MODE_NORMAL)
            }
            CallLineStatus.Active -> runOnUiThread(this::onCallActive)
            else -> {}
        }

        runOnUiThread{
            viewBinding.content.dtmfTvCallStatus.text = lineStatus.toString()
            viewBinding.content.dtmfTvAccountStatus.text = lineStatus.toString()
        }
    }

    override fun onCallExtendedError(call: Call?, extendedError: ExtendedError?) {

    }

    override fun onCallNetworkStatistics(call: Call?, networkStatistics: NetworkStatistics?) {

    }

    override fun onCallNetworkQualityLevel(call: Call?, i: Int, i1: NetworkQualityLevel) {

    }

    override fun onCallTransferSucceeded(call: Call?) {

    }

    override fun onCallTransferFailure(call: Call?, extendedError: ExtendedError?) {

    }

    override fun onCallTransferStarted(call: Call?, s: String?, s1: String?, s2: String?) {

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

    override fun onAccountStatusChanged(account: Account?, accountStatus: AccountStatus?, i: Int) {
        if(accountStatus == AccountStatus.Registered) runOnUiThread(this::startCall)

        runOnUiThread {
            viewBinding.content.dtmfTvAccountStatus.text = accountStatus.toString()
            viewBinding.content.dtmfTvAccountCode.text = "$i"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onAccountRetryingRegistration(account: Account?, i: Int, i1: Int) {
        runOnUiThread{
            viewBinding.content.dtmfTvAccountStatus.text = "Retrying registration"
        }
    }

    override fun onAccountIncomingCall(account: Account?, call: Call?) {

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

    companion object {
        private const val USERNAME = "dtmfauto"
        private const val PASSWORD = "mnbv"
        private const val DOMAIN = "pbx.securax.net"
        private val DTMF_TYPE = DTMFType.MediaOutband
    }
}
