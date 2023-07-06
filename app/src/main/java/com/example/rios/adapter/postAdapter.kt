package com.example.rios.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.rios.utils.Zoomimage
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.post
import com.example.rios.utils.Difi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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
        private val commentButton: ImageView = itemView.findViewById(R.id.post_comment_icon)
        private val shareButton: ImageView = itemView.findViewById(R.id.post_share_icon)

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
            if (post.caption.isNotEmpty()) {
                captionTextView.text = post.caption
            }else{
                captionTextView.visibility = View.GONE
            }
            // Set the number of likes
            likesTextView.text = post.likes.size.toString()

// Set the like button image based on whether the current user has liked the post or not
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val isLiked = post.likes.contains(currentUserUid)
            updateLikeButtonState(isLiked)

            shareButton.setOnClickListener {
                val postImageUrl = post.imageUrl
                val username = post.username
                val postId = post.postId
                val shareText = "Posted by $username"

                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap: Bitmap? = try {
                        BitmapFactory.decodeStream(URL(postImageUrl).openStream())
                    } catch (e: IOException) {
                        e.printStackTrace()
                        null
                    }

                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            val file = File(context?.cacheDir, "$postId.png")
                            try {
                                val fileOutputStream = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                                fileOutputStream.flush()
                                fileOutputStream.close()

                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "image/png"
                                val imageUri = FileProvider.getUriForFile(
                                    context,
                                    context?.packageName + ".provider",
                                    file
                                )
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

                                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)))
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }



//            private fun shareableBitmap(bitmap: Bitmap?): Uri {
//                val cachePath = File(this, "imageai/")
//                cachePath.mkdirs()
//                val tsLong = System.currentTimeMillis() / 1000
//                val ts = tsLong.toString()
//
//                val file = File(cachePath, "ImageAI${ts}.png")
//                val fileOutputStream: FileOutputStream
//                try {
//                    fileOutputStream = FileOutputStream(file)
//                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//                    fileOutputStream.flush()
//                    fileOutputStream.close()
//                } catch (_: FileNotFoundException) {
//                } catch (_: IOException) {
//                }
//                return FileProvider.getUriForFile(
////                   this, this.applicationContext.packageName + ".provider", file
//                )
//            }

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

                commentButton.setOnClickListener {

                }

                // Update the like button state
                updateLikeButtonState(!isCurrentlyLiked)

                // Update the number of likes displayed
                likesTextView.text = post.likes.size.toString()

                // Perform the necessary updates in your Firestore database to reflect the changes
                val firestore = FirebaseFirestore.getInstance()
                val postRef = firestore.collection("posts").document(post.postId)

                firestore.runTransaction { transaction ->
                    val currentPost = transaction.get(postRef).toObject(post::class.java)

                    currentPost?.likes = post.likes

                    transaction.set(postRef, currentPost as Any)
                    currentPost // Return the updated Post object
                }.addOnSuccessListener { updatedPost ->
                    // Transaction success
                    likesTextView.text = updatedPost.likes.size.toString()

                    post.likes = updatedPost.likes
                    // Update the like count in Firestore
                    postRef.update("likes", updatedPost.likes as List<String>)
                        .addOnSuccessListener {
                            // Like count updated in Firestore
                        }
                        .addOnFailureListener { exception ->
                            // Failed to update like count in Firestore
                        }
                }.addOnFailureListener { exception ->
                    // Transaction failure
                }
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