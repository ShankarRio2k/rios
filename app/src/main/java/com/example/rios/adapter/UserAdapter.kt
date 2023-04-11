package com.example.rios.views

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.User
import com.example.rios.utils.FirebaseUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserAdapter(val context: Context, val users: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private lateinit var newUser: User

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatitem, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        newUser = user
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
        holder.itemView.setOnClickListener {
            val fragment = Chat(user)
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.inner_container, fragment)
                .addToBackStack(null) // Add the fragment to the back stack
                .commit()
        }


        holder.profileImage.setOnClickListener {
            val fragment = profileFragment()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.inner_container, fragment)
                .addToBackStack(null) // Add the fragment to the back stack
                .commit()
        }


        holder.itemView.setOnLongClickListener {
            val dialogBuilder = AlertDialog.Builder(context)
            val db = FirebaseFirestore.getInstance()
            val SenderRoom = newUser.id + FirebaseUtils.firebaseAuth.currentUser!!.uid
            dialogBuilder.setMessage("Are you sure you want to delete all messages in this conversation?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                    // Delete all messages from the SenderRoom collection
                    db.collection("chat").document(SenderRoom).collection("messages").get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                document.reference.delete()
                            }
                            Toast.makeText(context, "All messages deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error deleting messages: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Delete Conversation")
            alert.show()
            true
        }

        // Bind other views here
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.usernlist)

        //        val userbio = itemView.findViewById<TextView>(R.id.userbio)
        val profileImage = itemView.findViewById<ImageView>(R.id.userlistimg)
    }

}
