package com.example.rios.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(
    private val currentChatMessageList: ArrayList<ChatMessage>
) :
    FirestoreRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder>(
        FirestoreRecyclerOptions.Builder<ChatMessage>()
            .setQuery(
                FirebaseFirestore.getInstance().collection("messages").orderBy("createdAt"),
                ChatMessage::class.java
            )
            .build()
    ) {

    companion object {
        private const val ITEM_SEND = 1
        private const val ITEM_RECEIVE = 2
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.senndertext)
        val messageTime: TextView = itemView.findViewById(R.id.timeofmessages)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.receivertxt)
        val messageTime: TextView = itemView.findViewById(R.id.timeofmessages)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SEND) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.senderchatlayout, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.receiverchatlayout, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        model: ChatMessage
    ) {
        if (holder is SenderViewHolder) {
            holder.messageText.text = currentChatMessageList[position].message//model.message
            holder.messageTime.text =
                currentChatMessageList[position].currenttime//model.currenttime.toString()
        } else if (holder is ReceiverViewHolder) {
            holder.messageText.text = currentChatMessageList[position].message//model.message
            holder.messageTime.text =
                currentChatMessageList[position].currenttime//model.currenttime.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val model = getItem(position)
        return if (model.senderid == currentChatMessageList[position].senderid) {
            ITEM_SEND
        } else {
            ITEM_RECEIVE
        }
    }

    fun addMessage(currentMessage: ChatMessage) {

    }


    override fun onDataChanged() {
        // Scroll to the bottom when new data is added
        if (itemCount > 0) {
            // TODO: Implement scroll to bottom
        }
    }
}
