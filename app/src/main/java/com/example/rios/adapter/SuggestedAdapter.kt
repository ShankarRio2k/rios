package com.example.rios.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.User
import com.example.rios.utils.FirebaseUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SuggestedAdapter(
    val context: Context,
    var users: MutableList<User>,
    val onUpdateUserList: (User) -> Unit
) :
    RecyclerView.Adapter<SuggestedAdapter.ViewHolder>() {
    private lateinit var newUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.suggested_users, parent, false)
        return SuggestedAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        newUser = user
        holder.username.text = user.name
        holder.userbio.text = user.bio

        val storageRef =
            FirebaseStorage.getInstance().reference.child("profiles/${user.id}/profilePic")

        // Download the image from Firebase Storage
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            // Use Glide library to load the image into the ImageView
            Glide.with(holder.itemView)
                .load(uri)
                .into(holder.profileImage)

        }.addOnFailureListener { exception ->
            // Handle any errors
        }

        holder.addfriend.setOnClickListener {
            // Add the user to the "friends" collection in Firestore
            val db = FirebaseFirestore.getInstance()
            val currentUser = FirebaseUtils.firebaseAuth.currentUser
            if (currentUser != null) {
                val currentUserId = currentUser.uid
                val friendId = user.id
                val friend = hashMapOf(
                    "name" to user.name,
                    "id" to friendId
                )
                onUpdateUserList(User(friendId, user.name,"",""))

                db.collection("users").document(currentUserId)
                    .collection("friends").document(friendId)
                    .set(friend)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT)
                            .show()
                        /*  val newList = users
                          newList.removeAt(position)
                          users = newList
                          notifyItemRemoved(position)*/
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            context,
                            "Error adding friend: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    // Bind other views here

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.usernlist)
        val addfriend = itemView.findViewById<Button>(R.id.add)
        val userbio = itemView.findViewById<TextView>(R.id.bio)
        val profileImage = itemView.findViewById<ImageView>(R.id.userlistimg)
    }

}