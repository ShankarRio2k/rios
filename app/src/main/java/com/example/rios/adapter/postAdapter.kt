package com.example.rios.adapter

import android.content.Context
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.rios.utils.Zoomimage
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.post
import com.example.rios.utils.Difi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class postAdapter(
    private val context: Context,
    private var posts: List<post>
) : RecyclerView.Adapter<postAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.postitem, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, holder)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun updatePost(newPosts: List<post>) {
        val diffCallback = Difi(posts, newPosts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        posts = newPosts
        diffResult.dispatchUpdatesTo(this)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: Zoomimage = itemView.findViewById(R.id.Post_image)
        private val profilePicture: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val usernameTextView: TextView = itemView.findViewById(R.id.userName)
        private val captionTextView: TextView = itemView.findViewById(R.id.post_description)
        private val likeButton: ImageView = itemView.findViewById(R.id.post_like_icon)
        private val likesTextView: TextView = itemView.findViewById(R.id.likesTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.time)

        fun bind(post: post, holder: PostViewHolder) {
            // Load image into postImageView using Glide
            Glide.with(context)
                .load(post.imageUrl)
                .into(postImageView)

            // Load profile picture into profilePicture using Glide
            Glide.with(context)
                .load(post.profileUrl)
                .into(profilePicture)

            // Set the username and caption
            usernameTextView.text = post.username
            captionTextView.text = post.caption

            // Set the number of likes
            likesTextView.text = post.likes.size.toString()

            // Set the like button image based on whether the current user has liked the post or not
            if (post.isLiked) {
                likeButton.setImageResource(R.drawable.heart)
            } else {
                likeButton.setImageResource(R.drawable.favorite)
            }

            // Set the click listener for the like button
            likeButton.setOnClickListener {
                // Toggle the like status of the post
                post.isLiked = !post.isLiked

                // Get the ID of the current user
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (post.isLiked) {
                    currentUserId?.let {
                        try {
                            val userId = it.toLong()
                            post.likes = post.likes.toMutableList().apply { add(userId) }
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Invalid user id: $it")
                        }
                    }
                    likeButton.setImageResource(R.drawable.heart)
                } else {
                    currentUserId?.let {
                        try {
                            val userId = it.toLong()
                            post.likes = post.likes.toMutableList().apply { remove(userId) }
                        } catch (e: NumberFormatException) {
                            Log.e(TAG, "Invalid user id: $it")
                        }
                    }
                    likeButton.setImageResource(R.drawable.favorite)
                }

                // Update the like count in Firestore
                val postRef =
                    FirebaseFirestore.getInstance().collection("Posts").document(post.postId)
                postRef.update("likes", post.likes.toList())
                    .addOnSuccessListener {
                        Log.d(TAG, "Like count updated successfully in Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating like count in Firestore", e)
                    }

                // Update the number of likes displayed
                likesTextView.text = post.likes.size.toString()

                // Notify the adapter that the item has changed
                notifyItemChanged(holder.adapterPosition, post as Any)
            }

            // Set the timestamp text using a SimpleDateFormat

            // Set the timestamp text using a SimpleDateFormat
            val timestamp = post.timestamp
            if (timestamp != null) {
                val currentTime = System.currentTimeMillis() // current time in milliseconds
                val postTime = timestamp.toDate().time // post time in milliseconds
                val timeDifference = (currentTime - postTime) / 1000 // time difference in seconds
                val timeText = when {
                    timeDifference < 60 -> "$timeDifference sec ago"
                    timeDifference < 3600 -> "${timeDifference / 60} min ago"
                    timeDifference < 86400 -> "${timeDifference / 3600} hours ago"
                    else -> "${timeDifference / 86400} days ago"
                }
                timestampTextView.text = timeText
            }

        }
    }
}
