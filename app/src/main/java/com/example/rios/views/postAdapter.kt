package com.example.rios.views

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.rios.utils.Zoomimage
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class postAdapter(private val context: Context, private val posts: List<post>) :
    RecyclerView.Adapter<postAdapter.PostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.postitem, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: Zoomimage = itemView.findViewById(R.id.Post_image)
        private val profilePicture: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val usernameTextView: TextView = itemView.findViewById(R.id.userName)
        private val captionTextView: TextView = itemView.findViewById(R.id.about)
        private val likeButton: ImageView = itemView.findViewById(R.id.likebutton)
        private val likesTextView: TextView = itemView.findViewById(R.id.likes)
//        private val timestampTextView: TextView = itemView.findViewById(R.id.like)

        fun bind(post: post) {
            Glide.with(context)
                .load(post.imageUrl)
                .into(postImageView)
            Glide.with(context)
                .load(post.profileUrl)
                .into(profilePicture)
            usernameTextView.text = post.username
            captionTextView.text = post.caption
            likesTextView.text = post.likes.size.toString()

            if (post.isLiked) {
                likeButton.setImageResource(R.drawable.heart)
            } else {
                likeButton.setImageResource(R.drawable.favorite)
            }

            likeButton.setOnClickListener {
                post.isLiked = !post.isLiked
                if (post.isLiked) {
                    post.likes+1
                    likeButton.setImageResource(R.drawable.heart)
                } else {
                    post.likes-1
                    likeButton.setImageResource(R.drawable.favorite)
                }
                notifyItemChanged(adapterPosition)
            }
        }
//            timestampTextView.text = post.timestamp.toString()
//                SimpleDateFormat("dd MM yyyy", Locale.getDefault())
//                .format(post.timestamp)
    }
}

