package com.example.rios.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.rios.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context, val messageList: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<ViewHolder>() {
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       if (viewType == 1){
           val view:View = LayoutInflater.from(context).inflate(R.layout.senderchatlayout,parent,false)
           return  SentviewHolder(view)
       }else{
           val view:View = LayoutInflater.from(context).inflate(R.layout.receiverchatlayout,parent,false)
           return  ReceiveviewHolder(view)
       }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentmsg = messageList[position]

        if (holder.javaClass == SentviewHolder::class.java) {
            val viewHolder = holder as SentviewHolder
            viewHolder.sentmessage.text = currentmsg.message

        } else {
            val viewHolder = holder as ReceiveviewHolder
            viewHolder.receivemessage.text = currentmsg.message

        }

    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderid)){
            return  ITEM_SENT
        }else{
            return ITEM_RECEIVE
        }

            return super.getItemViewType(position)
    }

    class SentviewHolder(ItemView: View) : ViewHolder(ItemView) {
        val sentmessage = itemView.findViewById<TextView>(R.id.senndertext)
    }

    class ReceiveviewHolder(ItemView: View) : ViewHolder(ItemView) {
        val receivemessage = itemView.findViewById<TextView>(R.id.receivertxt)

    }

    override fun getItemCount(): Int {
             return  messageList.size

    }
}