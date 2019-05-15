@file:Suppress("RedundantCompanionReference", "ConstantConditionIf")

package com.zoiper.zdk.android.demokt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.zoiper.zdk.Account
import com.zoiper.zdk.Configurations.AccountConfig
import com.zoiper.zdk.Configurations.SIPConfig
import com.zoiper.zdk.Configurations.StunConfig
import com.zoiper.zdk.Configurations.ZRTPConfig
import com.zoiper.zdk.Context
import com.zoiper.zdk.EventHandlers.AccountEventsHandler
import com.zoiper.zdk.EventHandlers.SIPProbeEventsHandler
import com.zoiper.zdk.Providers.AccountProvider
import com.zoiper.zdk.Types.*
import com.zoiper.zdk.Types.Zrtp.*
import com.zoiper.zdk.android.demokt.base.BaseActivity
import com.zoiper.zdk.android.demokt.base.getTextOrError
import com.zoiper.zdk.android.demokt.call.InCallActivity
import com.zoiper.zdk.android.demokt.conference.ConferenceActivity
import com.zoiper.zdk.android.demokt.dtmf.DTMFActivity
import com.zoiper.zdk.android.demokt.incoming.IncomingCallActivity
import com.zoiper.zdk.android.demokt.messages.InMessagesActivity
import com.zoiper.zdk.android.demokt.probe.SipTransportProbeActivity
import com.zoiper.zdk.android.demokt.video.InVideoCallActivity
import kotlinx.android.synthetic.main.card_calls.*
import kotlinx.android.synthetic.main.card_profile.*
import java.util.*
import kotlin.reflect.KClass

/**
 *MainActivity
 *
 *@since 15/03/2019
 */

const val VESITESTING = "VesiTesting"

const val INTENT_EXTRA_NUMBER = "number"
const val INTENT_EXTRA_ACCOUNT_ID = "account_id"

class MainActivity : BaseActivity(), AccountEventsHandler, SIPProbeEventsHandler {

    override fun onAccountStatusChanged(account: Account?, status: AccountStatus?, statusCode: Int) {
//        Log.d(VESITESTING, "MainActivity.onAccountStatusChanged()")
        mainHandler.post{ this@MainActivity.printCurrentRegistrationStatus() }
    }

    private var account: Account? = null
        set(account) {
            zdkApplication.account = account
            account?.setStatusEventListener(this@MainActivity)
            field = account
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setClickListeners()
    }

    override fun onZoiperLoaded() {

    }

    private fun setClickListeners() {
        btnRegister.setOnClickListener { registerUser(zdkContext) }
        btnUnregister.setOnClickListener { account?.unRegister() }

        btnConference.setOnClickListener { startConferenceActivity() }
        btnDialVideo.setOnClickListener { startVideoCallActivity() }
        btnMessaging.setOnClickListener { startMessageActivity() }
        btnIncoming.setOnClickListener { startIncomingActivity() }
        btnProbe.setOnClickListener { startSipTransportProbe() }
        btnDial.setOnClickListener { startCallActivity() }
        btnDtmf.setOnClickListener { startDTMFActivity() }
    }

    override fun onResume() {
        super.onResume()
        account?.setStatusEventListener(this)
    }

    override fun onPause() {
        super.onPause()
        account?.dropStatusEventListener(this)
    }

    private fun getNumberFromView(): String {
        // Get the number.
        return etNumber.text.toString().trim { it <= ' ' }
    }

    private fun printError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    private fun checkNumberEntered(): Boolean {
        return if (!getNumberFromView().isEmpty()) {
            true
        } else {
            printError("Enter a number")
            false
        }
    }

    private fun checkRegistration(): Boolean {
        return if (account != null && account?.registrationStatus() == AccountStatus.Registered) {
            true
        } else {
            printError("Account not registered")
            false
        }
    }

    private fun startDTMFActivity() {
        if (checkNumberEntered()) {
            startActivity(DTMFActivity::class.java)
        }
    }

    private fun startSipTransportProbe() {
        if (checkRegistration()) {
            startActivity(SipTransportProbeActivity::class.java)
        }
    }

    private fun startConferenceActivity() {
        if (checkRegistration()) {
            startActivity(ConferenceActivity::class.java)
        }
    }

    private fun startIncomingActivity() {
        if (checkRegistration()) {
            startActivity(IncomingCallActivity::class.java)
        }
    }

    private fun startMessageActivity() {
        if (checkRegistration() && checkNumberEntered()) {
            startActivity(InMessagesActivity::class.java)
        }
    }

    private fun startCallActivity() {
        if (checkRegistration() && checkNumberEntered()) {
            startActivity(InCallActivity::class)
        }
    }


    private fun startActivity(kClass: KClass<InCallActivity>) = startActivity(kClass.java)
    /**
     * Starts an Activity by its class.
     *
     * @param activityClass Activity class (for instance MyCallActivity.class).
     */
    private fun startActivity(activityClass: Class<out Activity>) {
        val intent = Intent(this@MainActivity, activityClass)

        val number = getNumberFromView()
        val accountId = account?.accountID() ?: -1L

        intent.putExtra(INTENT_EXTRA_NUMBER, number)
        intent.putExtra(INTENT_EXTRA_ACCOUNT_ID, accountId)

        this@MainActivity.startActivity(intent)
    }

    private fun startVideoCallActivity() {
        if (checkRegistration() && checkNumberEntered()) {
            startActivity(InVideoCallActivity::class.java)
        }
    }

    /**
     * Creates the user, register and make it as default account.
     *
     * @param zdkContext
     * The initialized ZDK context.
     */
    private fun registerUser(zdkContext: Context) {
        val hostname = etHostname.getTextOrError()
        val username = etUsername.getTextOrError()
        val password = etPassword.getTextOrError()

        if (hostname == null
            || username == null
            || password == null) {
            return
        }

        if (account == null) {
            account = createAccount(zdkContext, username, username, hostname, password)
        }

        account?.apply {
            Log.d(VESITESTING, "account.createUser() = ${createUser().text()}")
            Log.d(VESITESTING, "account.registerAccount() = ${registerAccount().text()}")

            zdkContext.accountProvider().setAsDefaultAccount(account)
            Log.d(VESITESTING, "zdkContext.accountProvider().setAsDefaultAccount(account)")
        }

        printCurrentRegistrationStatus()
    }

    private fun printCurrentRegistrationStatus() {
//        Log.d(VESITESTING, "MainActivity.printCurrentRegistrationStatus()")

        val accountStatus = account?.registrationStatus()

//        Log.d(VESITESTING, "account?.registrationStatus() = ${accountStatus.toString()}")

        if (accountStatus != null) {
            printStatus(accountStatus.toString())
        }
    }

    private fun printStatus(status: String) {
        if (tvStatus != null) {
            tvStatus.text = status
        }
    }

    /**
     * Create new account.
     *
     * @param zdkContext
     * The initialized ZDK context.
     * @param accountName
     * The account name.
     */
    private fun createAccount(
        zdkContext: Context,
        accountName: String,
        hostname: String,
        username: String,
        password: String
    ): Account {
        val accountProvider = zdkContext.accountProvider()
        val account = accountProvider.createUserAccount()
        Log.d(VESITESTING, "accountProvider.createUserAccount()")

        // Set listeners on the account
        account.setProbeEventListener(this)
        Log.d(VESITESTING, "account.setProbeEventListener(this)")

        account.setStatusEventListener(this)
        Log.d(VESITESTING, "account.setStatusEventListener(this)")

        // Account name - not to be confused with username
        account.accountName(accountName)
        Log.d(VESITESTING, "account.accountName($accountName)")

        // Configurations
        getAudioCodecs().let {
            account.mediaCodecs(it)
            Log.d(VESITESTING, "account.mediaCodecs($it)")
        }
        account.configuration(createAccountConfig(accountProvider, username, hostname, password))
        Log.d(VESITESTING, "account.configuration(accountConfig)")

        return account
    }

    private fun createAccountConfig(
        ap: AccountProvider,
        hostname: String,
        username: String,
        password: String
    ): AccountConfig {
        val accountConfig = ap.createAccountConfiguration()
        Log.d(VESITESTING, "ap.createAccountConfiguration()")

        accountConfig.userName(username)
        Log.d(VESITESTING, "accountConfig.userName($username)")

        accountConfig.password(password)
        Log.d(VESITESTING, "accountConfig.password($password)")

        accountConfig.type(ProtocolType.SIP)
        Log.d(VESITESTING, "accountConfig.type(ProtocolType.SIP)")

        accountConfig.sip(createSIPConfig(ap, hostname))
        Log.d(VESITESTING, "accountConfig.sip(sipConfig)")

        accountConfig.reregistrationTime(60)
        Log.d(VESITESTING, "accountConfig.reregistrationTime(60)")

        return accountConfig
    }

    /**
     * Returns a List with all the Audio codecs.
     *
     * @return List with audio codecs.
     */
    private fun getAudioCodecs(): List<AudioVideoCodecs> {
        val codecs = ArrayList<AudioVideoCodecs>()
        codecs.add(AudioVideoCodecs.OPUS_WIDE)
        codecs.add(AudioVideoCodecs.PCMU)
        codecs.add(AudioVideoCodecs.vp8) // This is for the videocall
        return codecs
    }

    private fun createSIPConfig(accountProvider: AccountProvider, hostname: String): SIPConfig {
        val sipConfig = accountProvider.createSIPConfiguration()
        Log.d(VESITESTING, "accountProvider.createSIPConfiguration()")

        sipConfig.transport(TransportType.TCP)
        Log.d(VESITESTING, "sipConfig.transport(TransportType.TCP)")


        sipConfig.domain(hostname)
        Log.d(VESITESTING, "sipConfig.domain($hostname)")

        sipConfig.rPort(RPortType.Signaling)
        Log.d(VESITESTING, "sipConfig.rPort(RPortType.Signaling)")


        sipConfig.enablePrivacy(Configuration.PRIVACY)
        Log.d(VESITESTING, "sipConfig.enablePrivacy(Configuration.PRIVACY)")

        sipConfig.enablePreconditions(Configuration.PRECONDITIONS)
        Log.d(VESITESTING, "sipConfig.enablePreconditions(Configuration.PRECONDITIONS)")

        sipConfig.enableSRTP(Configuration.SRTP) // Works only with TLS!
        Log.d(VESITESTING, "sipConfig.enableSRTP(Configuration.SRTP)")

        sipConfig.enableVideoFMTP(Configuration.VIDEO_FMTP)
        Log.d(VESITESTING, "sipConfig.enableVideoFMTP(Configuration.VIDEO_FMTP)")


        if (Configuration.STUN) {
            sipConfig.stun(createStunConfig(accountProvider))
            Log.d(VESITESTING, "sipConfig.stun(createStunConfig(accountProvider))")

        }
        if (Configuration.ZRTP) {
            sipConfig.zrtp(createZRTPConfig(accountProvider))
            Log.d(VESITESTING, "sipConfig.zrtp(createZRTPConfig(accountProvider))")

        }

        sipConfig.rtcpFeedback(

            if (Configuration.RTCP_FEEDBACK)
                RTCPFeedbackType.Compatibility
            else
                RTCPFeedbackType.Off
        )
        Log.d(VESITESTING, "sipConfig.rtcpFeedback(${if (Configuration.RTCP_FEEDBACK)
            RTCPFeedbackType.Compatibility
        else
            RTCPFeedbackType.Off})")

        return sipConfig
    }

    private fun createStunConfig(ap: AccountProvider): StunConfig {
        val stunConfig = ap.createStunConfiguration()
        stunConfig.stunEnabled(true)
        stunConfig.stunServer("stun.zoiper.com")
        stunConfig.stunPort(3478)
        stunConfig.stunRefresh(30000)
        return stunConfig
    }

    private fun createZRTPConfig(ap: AccountProvider): ZRTPConfig {
        val hashes = ArrayList<ZRTPHashAlgorithm>()
        hashes.add(ZRTPHashAlgorithm.s384)
        hashes.add(ZRTPHashAlgorithm.s256)

        val ciphers = ArrayList<ZRTPCipherAlgorithm>()
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes3)
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes2)
        ciphers.add(ZRTPCipherAlgorithm.cipher_aes1)

        val auths = ArrayList<ZRTPAuthTag>()
        auths.add(ZRTPAuthTag.hs80)
        auths.add(ZRTPAuthTag.hs32)

        val keyAgreements = ArrayList<ZRTPKeyAgreement>()
        keyAgreements.add(ZRTPKeyAgreement.dh3k)
        keyAgreements.add(ZRTPKeyAgreement.dh2k)
        keyAgreements.add(ZRTPKeyAgreement.ec38)
        keyAgreements.add(ZRTPKeyAgreement.ec25)

        val sasEncodings = ArrayList<ZRTPSASEncoding>()
        sasEncodings.add(ZRTPSASEncoding.sasb256)
        sasEncodings.add(ZRTPSASEncoding.sasb32)

        val zrtpConfig = ap.createZRTPConfiguration()

        zrtpConfig.enableZRTP(true)
        zrtpConfig.hash(hashes)
        zrtpConfig.cipher(ciphers)
        zrtpConfig.auth(auths)
        zrtpConfig.keyAgreement(keyAgreements)
        zrtpConfig.sasEncoding(sasEncodings)
        zrtpConfig.cacheExpiry(-1) // No expiry

        return zrtpConfig
    }

    private companion object Configuration {
        private const val PRIVACY = false
        private const val PRECONDITIONS = false
        private const val STUN = false
        private const val SRTP = false
        private const val ZRTP = false
        private const val VIDEO_FMTP = false
        private const val RTCP_FEEDBACK = false
        private const val SIP_TRANSPORT_PROBE = false
    }
}