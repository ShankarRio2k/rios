package com.example.rios.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val context: Context,
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.itenmessage, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text)
        private val senderTextView: TextView = itemView.findViewById(R.id.sender_text)
//        private val timeTextView: TextView = itemView.findViewById(R.id.time_text)

        fun bind(message: ChatMessage) {
            messageTextView.text = message.message.toString()

//            timeTextView.text = message.createdAt?.let {
//                SimpleDateFormat("HH:mm", Locale.getDefault()).format(
//                    it
//                )
//            }

            if (message.senderId == currentUserId) {
                senderTextView.text = context.getString(R.string.you)
            } else {
                senderTextView.text = message.senderName
            }
        }
    }
}
