package com.zoiper.zdk.android.demokt.dtmf

import android.annotation.SuppressLint
import android.media.AudioManager
import android.os.Bundle
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
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.base.BaseActivity
import kotlinx.android.synthetic.main.activity_dtmf.*
import kotlinx.android.synthetic.main.content_dtmf.*
import java.lang.IllegalArgumentException

class DTMFActivity : BaseActivity(), CallEventsHandler, AccountEventsHandler {
    private lateinit var account: Account

    private val number by lazy { intent.getStringExtra(INTENT_EXTRA_NUMBER) }

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
        setContentView(R.layout.activity_dtmf)

        setSupportActionBar(dtmfToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "DTMF"

        setListeners()
    }

    private fun setListeners() {
        listOf(
            dtmfBtn1,    dtmfBtn2, dtmfBtn3,
            dtmfBtn4,    dtmfBtn5, dtmfBtn6,
            dtmfBtn7,    dtmfBtn8, dtmfBtn9,
            dtmfBtnStar, dtmfBtn0, dtmfBtnHash
        ).forEach { it.setOnClickListener(this::onDialerClick) }

        dtmfFabHangup.setOnClickListener { hangup() }
        dtmfBtnDelete.setOnClickListener { deleteDigit() }
    }

    override fun onZoiperLoaded() = initAccount()

    @SuppressLint("SetTextI18n")
    private fun startCall() {
        if (callStarted || number.isEmpty()) return
        callStarted = true

        call = account.createCall(number, true, false)

        dtmfFabHangup.show()
        dtmfTvCallStatus.text = "Created"

        call.setCallStatusListener(this@DTMFActivity)
        setAudioMode(AudioManager.MODE_IN_COMMUNICATION)
    }

    private fun onCallActive(){
        dtmfClRinging.visibility = View.GONE
        dtmfClDialer.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun initAccount() {
        val accountProvider = zdkContext.accountProvider()
        val sipConfig = createSipConfig(accountProvider)
        val accConfig = createAccountConfig(accountProvider, sipConfig)

        dtmfTvAccountStatus.text = "Creating account"
        account = accountProvider.createUserAccount()

        dtmfTvAccountStatus.text = "Configuring"
        account.setStatusEventListener(this@DTMFActivity)

        account.accountName(USERNAME)
        account.mediaCodecs(audioCodecs)

        // Always set acc configuration like this, acc.configuration() returns copy of the object
        // and everything you set in it, wont matter.
        account.configuration(accConfig)

        dtmfTvAccountStatus.text = "Creating user"
        account.createUser()

        dtmfTvAccountStatus.text = "Registering"
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

        accountConfig.sip(sipConfig)

        return accountConfig
    }

    private fun createSipConfig(accountProvider: AccountProvider): SIPConfig {
        val sipConfig = accountProvider.createSIPConfiguration()

        sipConfig.transport(TransportType.UDP)
        sipConfig.domain(DOMAIN)
        sipConfig.dtmf(DTMF_TYPE)
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
        val length = dtmfEtNumber.text.length
        if (length > 0) {
            dtmfEtNumber.text.delete(length - 1, length)
        }
    }

    private fun enterDigit(digit: String) {
        try {
            val dtmfCode = DTMFCodes.fromInt(Integer.valueOf(digit))
            call.playDTMFSound(dtmfCode)
            call.sendDTMF(dtmfCode)

            dtmfEtNumber.append(digit)
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
            dtmfTvCallStatus.text = lineStatus.toString()
            dtmfTvAccountStatus.text = lineStatus.toString()
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

    override fun onAccountStatusChanged(account: Account?, accountStatus: AccountStatus?, i: Int) {
        if(accountStatus == AccountStatus.Registered) runOnUiThread(this::startCall)

        runOnUiThread {
            dtmfTvAccountStatus.text = accountStatus.toString()
            dtmfTvAccountCode.text = "$i"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onAccountRetryingRegistration(account: Account?, i: Int, i1: Int) {
        runOnUiThread{
            dtmfTvAccountStatus.text = "Retrying registration"
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
        private val DTMF_TYPE = DTMFTypeSIP.SIP_info_numeric
    }
}
