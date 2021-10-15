package com.zoiper.zdk.android.demokt.video

import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.zoiper.zdk.Call
import com.zoiper.zdk.CallStatus
import com.zoiper.zdk.ExtendedError
import com.zoiper.zdk.NetworkStatistics
import com.zoiper.zdk.Types.CallMediaChannel
import com.zoiper.zdk.Types.CallSecurityLevel
import com.zoiper.zdk.Types.NetworkQualityLevel
import com.zoiper.zdk.Types.OriginType
import com.zoiper.zdk.Types.Zrtp.*
import com.zoiper.zdk.android.demokt.ZDKTESTING

/**
 * CallEventsHandler
 *
 * @since 31/01/2019
 */
class CallEventsHandler(private val activity: InVideoCallActivity) : com.zoiper.zdk.EventHandlers.CallEventsHandler {

    override fun onCallStatusChanged(call: Call?, callStatus: CallStatus?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printStatusThreadSafe(callStatus!!.lineStatus().toString())
        }
    }

    override fun onCallExtendedError(call: Call?, extendedError: ExtendedError?) {
        return
    }

    override fun onCallNetworkStatistics(call: Call?, networkStatistics: NetworkStatistics?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.printNetworkThreadSafe(
                "BitrateOut: " + networkStatistics!!.averageOutputBitrate() +
                        "\nBitrateIn: " + networkStatistics.currentInputBitrate() +
                        "\nInputLoss: " + networkStatistics.currentInputLossPermil()
            )
        }
    }

    override fun onCallNetworkQualityLevel(call: Call?, i: Int, i1: NetworkQualityLevel) {
        activity.printGeneralThreadSafe("onCallNetworkQualityLevel: call= ${call?.callHandle()}; callChannel= $i; qualityLevel= $i1")
    }

    override fun onCallTransferSucceeded(call: Call?) {
        return
    }

    override fun onCallTransferFailure(call: Call?, extendedError: ExtendedError?) {
        return
    }

    override fun onCallTransferStarted(call: Call?, s: String?, s1: String?, s2: String?) {
        return
    }

    override fun onCallZrtpFailed(call: Call?, error: ExtendedError?) {
        Log.d(ZDKTESTING, "onCallZrtpFailed: call= " + call!!.callHandle() + "; error= " + error?.message())
    }

    override fun onCallZrtpSuccess(call: Call?, zidHex: String?, knownPeer: Int, cacheMismatch: Int, peerKnowsUs: Int, zrtpsasEncoding: ZRTPSASEncoding?,
                                   sas: String?, zrtpHashAlgorithm: ZRTPHashAlgorithm?, zrtpCipherAlgorithm: ZRTPCipherAlgorithm?, zrtpAuthTag: ZRTPAuthTag?, zrtpKeyAgreement: ZRTPKeyAgreement? ) {
        Log.d(ZDKTESTING, "onCallZrtpSuccess: call= " + call!!.callHandle())

        if (knownPeer != 0 && cacheMismatch == 0 && peerKnowsUs != 0) {
            activity.runOnUiThread { call!!.confirmZrtpSas(true) }
        } else {
            activity.runOnUiThread {
                AlertDialog.Builder(activity)
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

    override fun onVideoStopped(call: Call?, origin: OriginType?) {
        activity.printGeneralThreadSafe("onVideoStopped: call= ${call?.callHandle()}; origin= ${origin.toString()}")
    }

    override fun onVideoFormatSelected(call: Call?, dir: OriginType?, width: Int, height: Int, fps: Float) {
        activity.printGeneralThreadSafe("onVideoFormatSelected: call= ${call?.callHandle()}; dir= ${dir?.toString()}; res= ${width}x${height}@${fps}")
    }

    override fun onVideoStarted(call: Call?, origin: OriginType?) {
        activity.printGeneralThreadSafe("onVideoStarted: call= ${call?.callHandle()}; origin= ${origin.toString()}")
    }

    override fun onVideoCameraChanged(call: Call?) {
        activity.printGeneralThreadSafe("onVideoCameraChanged: call= ${call?.callHandle()}")
    }
}
