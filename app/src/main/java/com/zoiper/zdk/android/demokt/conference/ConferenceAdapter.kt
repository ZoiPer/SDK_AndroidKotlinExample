package com.zoiper.zdk.android.demokt.conference

import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zoiper.zdk.Call
import com.zoiper.zdk.CallStatus
import com.zoiper.zdk.Conference
import com.zoiper.zdk.EventHandlers.CallEventsHandler
import com.zoiper.zdk.EventHandlers.ConferenceEventsHandler
import com.zoiper.zdk.EventHandlers.ConferenceProviderEventsHandler
import com.zoiper.zdk.Providers.ConferenceProvider
import com.zoiper.zdk.Types.CallLineStatus
import com.zoiper.zdk.android.demokt.R
import kotlinx.android.synthetic.main.conference_item.view.*

/**
 * ConferenceAdapter
 *
 * @since 4.2.2019 г.
 */
class ConferenceAdapter(
    private val conferenceProvider: ConferenceProvider,
    private val promptCreateCall: ((Call) -> Unit) -> Unit
) : RecyclerView.Adapter<ConferenceAdapter.ConferenceHolder>(),
    ConferenceProviderEventsHandler,
    CallEventsHandler, ConferenceEventsHandler {

    var conferenceList = mutableListOf<Conference>()
        set(new) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean{
                    return new.getOrNull(newItemPos) == field.getOrNull(oldItemPos)
                }
                override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean{
                    return new.getOrNull(newItemPos) == field.getOrNull(oldItemPos)
                }
                override fun getOldListSize() = field.size
                override fun getNewListSize() = new.size
            }).dispatchUpdatesTo(this)

            field = new
        }

    override fun onConferenceAdded(confProvider: ConferenceProvider?, conference: Conference?) {
//        confProvider
//            ?.listConferences()
//            ?.also { conferenceList = it }
        conference
            ?.also { conferenceList.add(it) }
            ?.let { notifyItemInserted(conferenceList.indexOf(it)) }
    }

    override fun onConferenceRemoved(confProvider: ConferenceProvider?, conference: Conference?) {
        conferenceList = conferenceProvider.listConferences()
//        val listConferences =
//        conference
//            ?.let{ conferenceList.indexOf(it) }
//            ?.also { conferenceList.removeAt(it) }
//            ?.also { notifyItemRemoved(it) }
    }


    override fun onConferenceParticipantRemoved(conf: Conference?, call: Call?) {

    }

    override fun onConferenceParticipantJoined(conf: Conference?, call: Call?) {
        conf?.calls()
    }

    override fun onConferenceExtendedError(conf: Conference?, message: String?) {

    }

    override fun onCallStatusChanged(call: Call?, status: CallStatus?) {
        status
            ?.takeIf { it.lineStatus() == CallLineStatus.Terminated }
            ?.let { call }
            ?.let {
                conferenceProvider
                    .conferenceContainingCall(it)
                    ?.removeCall(it, false)
            }
    }

    override fun onBindViewHolder(viewHolder: ConferenceHolder, position: Int) = viewHolder.bind(conferenceList[position])

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ConferenceHolder {
        return ConferenceHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.conference_item,
            parent,
            false
        ))
    }

    override fun getItemCount() = conferenceList.size



//    private fun addCall(conference: Conference): Unit = addCallListener(conference)
//    fun addCallClickListener(conference: Conference, newCall: Call) = getConferenceCallAdapter(conference)?.addCallClickListener(newCall)

//    /**
//     * Add a new conference to the adapter.
//     *
//     * @param newConference
//     * The new conference.
//     */
//    fun addNewItem(newConference: Conference) {
//        conferenceList.add(newConference)
//        val indexOfNewItem = conferenceList.indexOf(newConference)
//        notifyItemInserted(indexOfNewItem)
//        conferenceRecycler.scrollToPosition(indexOfNewItem)
//        DiffUtil.calculateDiff()
//    }
//
//    private fun removeItem(position: Int) {
//        conferenceList.removeAt(position)
//        notifyItemRemoved(position)
//    }

//    fun removeCallFromConference(call: Call) {
//        for (conference in conferenceList) {
//            val callItemAdapter = getConferenceCallAdapter(conference)
//            if (callItemAdapter != null && callItemAdapter.containsCall(call)) {
//                callItemAdapter.removeCall(call)
//                break
//            }
//        }
//    }

//    fun addCallToConference(call: Call) {
//        for (conference in conferenceList) {
//            val callItemAdapter = getConferenceCallAdapter(conference)
//            if (callItemAdapter != null && adapterContainsCall(call, callItemAdapter)) {
//                conference.addCall(call)
//            }
//        }
//    }

//    private fun getConferenceCallAdapter(conference: Conference): CallAdapter? {
//        for (currentConference in conferenceList) {
//            // Check for conference similarity by its conference handle.
//            if (currentConference.conferenceHandle() == conference.conferenceHandle()) {
//                val conferenceIndex = conferenceList.indexOf(currentConference)
//                // Get the call item base layout recycler from
//                val childAt = conferenceRecycler.getChildAt(conferenceIndex)
//                val conferenceRecycler = childAt.conferenceItemRvCalls
//                return conferenceRecycler.adapter as CallAdapter?
//            }
//        }
//        return null
//    }

//    private fun adapterContainsCall(call: Call, callAdapter: CallAdapter): Boolean {
//        for (currentCall in callAdapter.callList) {
//            if (currentCall.callHandle() == call.callHandle()) {
//                return true
//            }
//        }
//        return false
//    }

//    fun addCallStatus(call: Call) {
//        for (conference in conferenceList) {
//            val callItemAdapter = getConferenceCallAdapter(conference)
//            if (callItemAdapter != null && adapterContainsCall(call, callItemAdapter)) {
//                callItemAdapter.setCallStatus(call)
//            }
//        }
//    }

    inner class ConferenceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(conference: Conference){

            val callAdapter = CallAdapter()

//            conference.setConferenceEventsListener(callAdapter)

            itemView.conferenceItemTvCount.text = itemCount.toString()

            itemView.conferenceItemTvAddCall.setOnClickListener { addCall(conference) }
            itemView.conferenceItemTvRemove.setOnClickListener { conference.hangUp() }

            itemView.conferenceItemRvCalls.adapter = callAdapter
            itemView.conferenceItemRvCalls.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    private fun addCall(conference: Conference) = promptCreateCall{ conference.addCall(it) }

//    private fun addCall(conference: Conference) {
//        val call = addCallClickListener(conference)
//
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    private fun removeItem(conference: Conference) {
//        if(conference.hangUp().code() == ResultCode.Ok){
//            conferenceList
//                .toTypedArray()
//                .let { mutableListOf(*it) }
//                .apply { removeAt(conferenceList.indexOf(conference)) }
//                .let { conferenceList = it }
//        }
//    }
}
