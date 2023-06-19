package com.example.rios.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth


class MessageAdapter(val context: Context, val messageList: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val ITEM_IMAGE_SENT = 3
    val ITEM_IMAGE_RECEIVE = 4
    val ITEM_DOC_SENT = 5
    val ITEM_DOC_RECEIVE = 6

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
            ITEM_DOC_SENT -> {
                val view = inflater.inflate(R.layout.senderdoc, parent, false)
                SenderDocViewHolder(view)
            }
            ITEM_DOC_RECEIVE -> {
                val view = inflater.inflate(R.layout.receiverdoc, parent, false)
                ReceiverDocViewHolder(view)
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
            ITEM_DOC_SENT -> {
                val viewHolder = holder as SenderDocViewHolder
                viewHolder.sentdocName.text = currentMsg.documentName
                viewHolder.sentdoc.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(currentMsg.documentUrl), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(intent)
                }
            }
            ITEM_DOC_RECEIVE -> {
                val viewHolder = holder as ReceiverDocViewHolder
                viewHolder.receivedocName.text = currentMsg.documentName
                viewHolder.receivedoc.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(currentMsg.documentUrl), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return when {
            currentMessage.senderid == FirebaseAuth.getInstance().currentUser?.uid && currentMessage.documentUrl != null -> ITEM_DOC_SENT
            currentMessage.documentUrl != null && currentMessage.senderid != FirebaseAuth.getInstance().currentUser?.uid -> ITEM_DOC_RECEIVE
            currentMessage.senderid == FirebaseAuth.getInstance().currentUser?.uid && currentMessage.image != null -> ITEM_IMAGE_SENT
            currentMessage.image != null && currentMessage.senderid != FirebaseAuth.getInstance().currentUser?.uid -> ITEM_IMAGE_RECEIVE
            currentMessage.senderid == FirebaseAuth.getInstance().currentUser?.uid -> ITEM_SENT
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
    inner class SenderDocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentdoc = itemView.findViewById<ImageView>(R.id.sender_document_icon)
        val sentdocName = itemView.findViewById<TextView>(R.id.sender_document_name)
    }
    inner class ReceiverDocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedoc = itemView.findViewById<ImageView>(R.id.receive_document_icon)
        val receivedocName = itemView.findViewById<TextView>(R.id.receiver_document_name)
    }

}