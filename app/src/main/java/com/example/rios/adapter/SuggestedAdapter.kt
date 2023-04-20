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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.suggested_users, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.username.text = user.name
        holder.userbio.text = user.bio

        // Set the user's profile image
        val storageRef =
            FirebaseStorage.getInstance().reference.child("profiles/${user.id}/profilePic")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView)
                .load(uri)
                .into(holder.profileImage)

        }.addOnFailureListener { exception ->
            // Handle any errors
        }

        // If the user is followed, hide the "Follow" button
        if (user.isFollowed) {
            holder.addfriend.visibility = View.GONE
        } else {
            holder.addfriend.visibility = View.VISIBLE
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

                    // Update the user's isFollowed flag and notify the adapter
                    user.isFollowed = true
                    notifyItemChanged(position)

                    db.collection("users").document(currentUserId)
                        .collection("friends").document(friendId)
                        .set(friend)
                        .addOnSuccessListener {
                            onUpdateUserList(User(friendId, user.name, user.bio, user.imageUrl, true, null))
                            Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT)
                                .show()

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
    }

    // Bind other views here

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.usernlist)
        val addfriend = itemView.findViewById<Button>(R.id.add)
        val userbio = itemView.findViewById<TextView>(R.id.bio)
        val profileImage = itemView.findViewById<ImageView>(R.id.userlistimg)
    }

}