package com.zoiper.zdk.android.demokt.messages

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zoiper.zdk.android.demokt.R
import kotlinx.android.synthetic.main.messages_incoming_layout.view.*
import kotlinx.android.synthetic.main.messages_layout.view.*

/**
 *MessagesAdapter
 *
 *@since 4/04/2019
 */
class MessagesAdapter(private val scrollTo: (position: Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType(val value: Int) {
        VIEW_TYPE_MESSAGE_SENT(1),
        VIEW_TYPE_MESSAGE_RECEIVED(2)
    }

    private var messagesList = mutableListOf<Message>()

    fun addMessage(message: Message) {
        messagesList.add(0, message)
        notifyItemInserted(0)
        scrollTo.invoke(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.VIEW_TYPE_MESSAGE_SENT.value -> MessageHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.messages_layout,
                    parent,
                    false
                )
            )
            ViewType.VIEW_TYPE_MESSAGE_RECEIVED.value -> ReceivedMessageHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.messages_incoming_layout,
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("ViewType $viewType is invalid.")
        }
    }

    override fun getItemCount(): Int = messagesList.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, positon: Int) {
        val message = messagesList[positon]

        if (viewHolder is ReceivedMessageHolder && message is ReceivedMessage) {
            viewHolder.bind(message)
        } else if (viewHolder is MessageHolder && message is BaseMessage) {
            viewHolder.bind(message)
        } else throw IllegalArgumentException("Unrecognized type for either viewHolder or message")
    }

    override fun getItemViewType(position: Int): Int = when (messagesList[position]) {
        is BaseMessage -> ViewType.VIEW_TYPE_MESSAGE_SENT.value
        is ReceivedMessage -> ViewType.VIEW_TYPE_MESSAGE_RECEIVED.value
        else -> throw IllegalArgumentException("Unknown message type at position $position")
    }

    inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: ReceivedMessage) {
            itemView.messagesIncomingLayoutTvUsername.text = message.username
            itemView.messagesIncomingLayoutTvBody.text = message.message
            itemView.messagesIncomingLayoutTvTime.text = message.time
        }
    }

    inner class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: BaseMessage) {
            itemView.messagesLayoutTvBody.text = message.message
            itemView.messagesLayoutTvTime.text = message.time
        }
    }

    interface Message
    data class BaseMessage(val message: String, val time: String) : Message
    data class ReceivedMessage(val message: String, val time: String, val username: String) : Message
}