package com.example.rios.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
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
        private val postImageView: ImageView = itemView.findViewById(R.id.post_image)
        private val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val captionTextView: TextView = itemView.findViewById(R.id.post_text)
        private val timestampTextView: TextView = itemView.findViewById(R.id.like_count)

        fun bind(post: post) {
            Glide.with(context)
                .load(post.imageUrl)
                .into(postImageView)

            usernameTextView.text = post.username
            captionTextView.text = post.caption
            timestampTextView.text = post.timestamp.toString()
//                SimpleDateFormat("dd MM yyyy", Locale.getDefault())
//                .format(post.timestamp)
        }
    }
}
