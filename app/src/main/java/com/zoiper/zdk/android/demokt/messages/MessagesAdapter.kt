package com.zoiper.zdk.android.demokt.messages

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zoiper.zdk.android.demokt.databinding.MessagesIncomingLayoutBinding
import com.zoiper.zdk.android.demokt.databinding.MessagesLayoutBinding

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
                MessagesLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ViewType.VIEW_TYPE_MESSAGE_RECEIVED.value -> ReceivedMessageHolder(
                MessagesIncomingLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
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

    inner class ReceivedMessageHolder(private val viewBinding: MessagesIncomingLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(message: ReceivedMessage) {
            viewBinding.messagesIncomingLayoutTvUsername.text = message.username
            viewBinding.messagesIncomingLayoutTvBody.text = message.message
            viewBinding.messagesIncomingLayoutTvTime.text = message.time
        }
    }

    inner class MessageHolder(private val viewBinding: MessagesLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(message: BaseMessage) {
            viewBinding.messagesLayoutTvBody.text = message.message
            viewBinding.messagesLayoutTvTime.text = message.time
        }
    }

    interface Message
    data class BaseMessage(val message: String, val time: String) : Message
    data class ReceivedMessage(val message: String, val time: String, val username: String) : Message
}