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
            // Set the number of likes
            // Set the number of likes
            likesTextView.text = post.likes.size.toString()

            // Set the like button image based on whether the current user has liked the post or not
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = post.likes.contains(currentUserUid)
            updateLikeButtonState(isLiked)

            likeButton.setOnClickListener {
                val isCurrentlyLiked = post.likes.contains(currentUserUid)

                if (isCurrentlyLiked) {
                    // Remove the current user's like from the post
                    currentUserUid?.let {
                        post.likes.remove(it)
                    }
                } else {
                    // Add the current user's like to the post
                    currentUserUid?.let {
                        post.likes.add(it)
                    }
                }

                // Update the like button state
                updateLikeButtonState(!isCurrentlyLiked)

                // Update the number of likes displayed
                likesTextView.text = post.likes.size.toString()
            }

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
        private fun updateLikeButtonState(isLiked: Boolean) {
            val drawableRes = if (isLiked) R.drawable.heart else R.drawable.ic_heart
            likeButton.setImageResource(drawableRes)
        }
    }
}