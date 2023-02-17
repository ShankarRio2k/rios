package com.example.rios.views

import com.example.rios.views.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text)
        val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profile_img)
        val messageTime: TextView = itemView.findViewById(R.id.message_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        holder.messageText.text = message.text
        holder.messageTime.text = message.time

        val profileImageRef = FirebaseStorage.getInstance().reference.child("profile_images/${message.fromId}.jpg")
        profileImageRef.downloadUrl.addOnSuccessListener {
            Glide.with(holder.itemView.context).load(it).into(holder.profileImage)
        }

        if (message.imageUrl.isNotEmpty()) {
            holder.messageImage.visibility = View.VISIBLE
            holder.messageText.visibility = View.GONE
            Glide.with(holder.itemView.context).load(message.imageUrl).into(holder.messageImage)
        } else {
            holder.messageImage.visibility = View.GONE
            holder.messageText.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = messageList.size
}
