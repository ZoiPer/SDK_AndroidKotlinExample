package com.zoiper.zdk.android.demokt.probe

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.zoiper.zdk.Account
import com.zoiper.zdk.EventHandlers.SIPProbeEventsHandler
import com.zoiper.zdk.ExtendedError
import com.zoiper.zdk.Types.ProbeState
import com.zoiper.zdk.Types.TransportType
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.base.BaseActivity
import kotlinx.android.synthetic.main.activity_sip_transport_probe.*
import kotlinx.android.synthetic.main.content_sip_transport_probe.*

/**
 *SipTransportProbeActivity
 *
 *@since 5/04/2019
 */
class SipTransportProbeActivity : BaseActivity(), SIPProbeEventsHandler {

    private val domain = "sip4.zoiper.com"
    private val password = "zdkTest"
    private val username = "zdkTest"

    private val account by lazy {
        zdkContext.accountProvider().createUserAccount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sip_transport_probe)

        setSupportActionBar(sipProbeToolbar)
        supportActionBar?.setTitle(R.string.sip_transport_probe)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sipProbeFab.setOnClickListener {
            Toast.makeText(this, "ZDK Context not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onZoiperLoaded() {
        sipProbeFab.setOnClickListener {
            account?.setProbeEventListener(this)
            account?.probeSipTransport(domain, null, username, null, password)
        }
    }

    override fun onProbeError(account: Account?, probeState: ProbeState?, extendedError: ExtendedError?) {
        Log.d(
            "Test",
            "onProbeError: " + probeState.toString() + " error: " + extendedError?.message()
        )
        runOnUiThread {
            updateProbe(true, probeState, null, extendedError)
        }
    }

    private fun updateProbe(success: Boolean,
                            probeState: ProbeState?,
                            transportType: TransportType?,
                            error: ExtendedError?) {
        updateTextView(when {
            probeState != null -> when(probeState) {
                ProbeState.Udp -> sipProbeTvUdp
                ProbeState.Tcp -> sipProbeTvTcp
                ProbeState.Tls -> sipProbeTvTls
                else -> return
            }
            transportType != null -> when(transportType) {
                TransportType.UDP -> sipProbeTvUdp
                TransportType.TCP -> sipProbeTvTcp
                TransportType.TLS -> sipProbeTvTls
                else -> return
            }
            else -> return
        }, success, error)
    }

    override fun onProbeState(account: Account?, probeState: ProbeState?) {
        Log.d("Test", "onProbeState: " + probeState?.toString())
    }

    override fun onProbeSuccess(account: Account?, transportType: TransportType?) {
        Log.d("Test", "onProbeSuccess: " + transportType?.toString())

        transportType?.let {
            runOnUiThread{
                updateProbe(true, null, it, null)
            }
        }
    }

    private fun updateTextView(tv: TextView, success:Boolean, error: ExtendedError?){
        tv.text = when {
            error != null -> error.message()
            success -> "Success"
            else -> "Failed"
        }
    }

    override fun onProbeFailed(account: Account?, extendedError: ExtendedError?) {
        Log.d("Test", "onProbeFailed: " + " error: " + extendedError.toString())
        runOnUiThread {
            Toast.makeText(
                this,
                "Probe failed: " + extendedError?.message(),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

