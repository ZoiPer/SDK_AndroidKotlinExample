package com.zoiper.zdk.android.demokt.messages

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.zoiper.zdk.Account
import com.zoiper.zdk.EventHandlers.AccountEventsHandler
import com.zoiper.zdk.EventHandlers.MessageEventsHandler
import com.zoiper.zdk.Result
import com.zoiper.zdk.Types.MessageType
import com.zoiper.zdk.Types.ResultCode
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_ACCOUNT_ID
import com.zoiper.zdk.android.demokt.INTENT_EXTRA_NUMBER
import com.zoiper.zdk.android.demokt.R
import com.zoiper.zdk.android.demokt.ZDKTESTING
import com.zoiper.zdk.android.demokt.base.BaseActivity
import kotlinx.android.synthetic.main.activity_in_messages.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *InMessagesActivity
 *
 *@since 4/04/2019
 */
class InMessagesActivity : BaseActivity(), MessageEventsHandler, AccountEventsHandler {

    private val account by lazy {
        val accountId = intent.getLongExtra(INTENT_EXTRA_ACCOUNT_ID, -1)
        getAccount(accountId)
    }

    private val messagesAdapter by lazy { MessagesAdapter{
        inMessagesRvMessages.scrollToPosition(it)
    }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_messages)
        setSupportActionBar(inMessagesToolbar)

        val number = intent.getStringExtra(INTENT_EXTRA_NUMBER)

        supportActionBar?.title = "Messaging: $number"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initRecycler()

        inMessagesFab.setOnClickListener { _ ->
            inMessagesEtMessage
                .text
                .toString()
                .trim()
                .ifBlank {
                    inMessagesEtMessage.error = "Please enter message"
                    null
                }
                ?.apply { inMessagesEtMessage.error = null }
                ?.let{
                    val result = sendMessage(it, number)

                    if(result?.code() == ResultCode.Ok){
                        messagesAdapter.addMessage(MessagesAdapter.BaseMessage(it, getCurrentTimeUsingCalendar()))

                        inMessagesEtMessage.setText("")
                        inMessagesEtMessage.clearFocus()
                    }else{
                        inMessagesEtMessage.error = "Error sending message"
                    }
                }
        }
    }

    override fun onZoiperLoaded() {
        account?.setStatusEventListener(this)
    }

    override fun onPause() {
        super.onPause()
        account?.dropStatusEventListener(this)
    }

    private fun sendMessage(content: String, number: String): Result? {
        Log.d(ZDKTESTING, "account.createMessage(MessageType.Simple)")
        val message = account?.createMessage(MessageType.Simple)

        Log.d(ZDKTESTING, "if(message == null) return;")
        if(message == null) return null

        message.setMessageEventListener(this)
        Log.d(ZDKTESTING, "message.setMessageEventListener(this)")

        message.peer(number)
        Log.d(ZDKTESTING, "message.peer($number)")

        message.content(content)
        Log.d(ZDKTESTING, "message.content($content)")

        return account
            ?.createMessage(MessageType.Simple)
            ?.apply {
                setMessageEventListener(this@InMessagesActivity)
                peer(number)
                content(content)
            }
            ?.sendMessage()
            ?.also{
                Log.d(ZDKTESTING, "message.sendMessage() = ${it.text()}")
            }
    }

    private fun initRecycler() {
        inMessagesRvMessages.adapter = messagesAdapter

        inMessagesRvMessages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            true
        )
    }

    private fun getCurrentTimeUsingCalendar(): String {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
        return dateFormat.format(date)
    }

    override fun onResume() {
        super.onResume()
        account?.setStatusEventListener(this)
    }

    override fun onAccountChatMessageReceived(account: Account?, peer: String?, message: String?) {
        runOnUiThread {
            Log.d(ZDKTESTING, "onAccountChatMessageReceived($account, $peer, $message)")

            messagesAdapter.addMessage(MessagesAdapter.ReceivedMessage(
                message ?: "",
                getCurrentTimeUsingCalendar(),
                peer ?: ""
            ))
        }
    }
}