package com.example.rios.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.rios.R
import com.google.firebase.auth.FirebaseAuth


class MessageAdapter(val context: Context, val messageList: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val ITEM_IMAGE_SENT = 3
    val ITEM_IMAGE_RECEIVE = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            ITEM_SENT -> {
                val view = inflater.inflate(R.layout.senderchatlayout, parent, false)
                SentviewHolder(view)
            }
            ITEM_RECEIVE -> {
                val view = inflater.inflate(R.layout.receiverchatlayout, parent, false)
                ReceiveviewHolder(view)
            }
            ITEM_IMAGE_SENT -> {
                val view = inflater.inflate(R.layout.senderimagelayout, parent, false)
                SentImageViewHolder(view)
            }
            ITEM_IMAGE_RECEIVE -> {
                val view = inflater.inflate(R.layout.receiverimagelayout, parent, false)
                ReceiveImageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMsg = messageList[position]
        when (holder.itemViewType) {
            ITEM_SENT -> {
                val viewHolder = holder as SentviewHolder
                viewHolder.sentmessage.text = currentMsg.message
            }
            ITEM_RECEIVE -> {
                val viewHolder = holder as ReceiveviewHolder
                viewHolder.receivemessage.text = currentMsg.message
            }
            ITEM_IMAGE_SENT -> {
                val viewHolder = holder as SentImageViewHolder
                Glide.with(context).load(currentMsg.image).into(viewHolder.sentimage)
            }
            ITEM_IMAGE_RECEIVE -> {
                val viewHolder = holder as ReceiveImageViewHolder
                Glide.with(context).load(currentMsg.image).into(viewHolder.receiveimage)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return when {
            currentMessage.senderid == FirebaseAuth.getInstance().currentUser?.uid && currentMessage.image != null -> ITEM_IMAGE_SENT
            currentMessage.senderid == FirebaseAuth.getInstance().currentUser?.uid -> ITEM_SENT
            currentMessage.image != null -> ITEM_IMAGE_RECEIVE
            else -> ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class SentviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentmessage = itemView.findViewById<TextView>(R.id.senndertext)
    }

    inner class ReceiveviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivemessage = itemView.findViewById<TextView>(R.id.receivertxt)
    }

    inner class SentImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentimage = itemView.findViewById<ImageView>(R.id.sentimage)
    }

    inner class ReceiveImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveimage = itemView.findViewById<ImageView>(R.id.receiveimage)
    }

}