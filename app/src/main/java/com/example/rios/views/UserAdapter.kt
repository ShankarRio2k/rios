package com.example.rios.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.google.firebase.storage.FirebaseStorage

class UserAdapter(val context: Context, val users: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatitem, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        holder.username.text = user.name
//        holder.userbio.text = user.bio

        val storageRef = FirebaseStorage.getInstance().reference.child("profiles/${user.id}/profilePic")

        // Download the image from Firebase Storage
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Use Glide library to load the image into the ImageView
            Glide.with(holder.itemView)
                .load(uri)
                .into(holder.profileImage)

        }.addOnFailureListener { exception ->
            // Handle any errors
        }

        holder.itemView.setOnClickListener{
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.inner_container, Chat(user)).commit()
        }
        // Bind other views here
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.usernlist)

        //        val userbio = itemView.findViewById<TextView>(R.id.userbio)
        val profileImage = itemView.findViewById<ImageView>(R.id.userlistimg)
    }

}