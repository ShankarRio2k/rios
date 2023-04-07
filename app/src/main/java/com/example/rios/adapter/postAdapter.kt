package com.example.rios.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.post
import de.hdodenhof.circleimageview.CircleImageView
import com.example.rios.utils.Zoomimage


class postAdapter(private val context: Context, posts: MutableList<post>) :
    ListAdapter<post, postAdapter.PostViewHolder>(DiffCallback) {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: Zoomimage = itemView.findViewById(R.id.Post_image)
        private val profilePicture: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val usernameTextView: TextView = itemView.findViewById(R.id.userName)
        private val captionTextView: TextView = itemView.findViewById(R.id.post_description)
        private val likeButton: ImageView = itemView.findViewById(R.id.post_like_icon)

        fun bind(post: post) {
            Glide.with(context)
                .load(post.imageUrl)
                .into(postImageView)
            Glide.with(context)
                .load(post.profileUrl)
                .into(profilePicture)
            usernameTextView.text = post.username
            captionTextView.text = post.caption

            if (post.isLiked) {
                likeButton.setImageResource(R.drawable.heart)
            } else {
                likeButton.setImageResource(R.drawable.favorite)
            }

            likeButton.setOnClickListener {
                post.isLiked = !post.isLiked
                if (post.isLiked) {
                    post.likes += 1
                    likeButton.setImageResource(R.drawable.heart)
                } else {
                    post.likes -= 1
                    likeButton.setImageResource(R.drawable.favorite)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.postitem, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = currentList[position]
        holder.bind(post)
    }
    fun updatePosts(updatedPosts: List<post>) {
        submitList(updatedPosts)
    }

    object DiffCallback : DiffUtil.ItemCallback<post>() {
        override fun areItemsTheSame(oldItem: post, newItem: post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: post, newItem: post): Boolean {
            return oldItem == newItem
        }
    }
}
