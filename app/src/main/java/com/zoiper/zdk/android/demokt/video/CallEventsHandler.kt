package com.zoiper.zdk.android.demokt.video

import android.os.Build
import com.zoiper.zdk.Call
import com.zoiper.zdk.CallStatus
import com.zoiper.zdk.ExtendedError
import com.zoiper.zdk.NetworkStatistics
import com.zoiper.zdk.Types.Zrtp.*

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

    override fun onCallNetworkQualityLevel(call: Call?, i: Int, i1: Int) {
        return
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

    override fun onCallZrtpFailed(call: Call?, extendedError: ExtendedError?) {
        return
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
        return
    }

    override fun onCallZrtpSecondaryError(call: Call?, i: Int, extendedError: ExtendedError?) {
        return
    }
}
