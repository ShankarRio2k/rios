package com.example.rios.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.model.video
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.video_item.view.*

class Videoadapter(private var videos: List<video>, private val context: Context) : RecyclerView.Adapter<Videoadapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    fun updateVideos(videos: List<video>) {
        this.videos = videos
        notifyDataSetChanged()
    }
    override fun getItemCount() = videos.size

    class VideoViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        private val videoView: VideoView = itemView.findViewById(R.id.playerView)
        private val userImage: CircleImageView = itemView.findViewById(R.id.userImage)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val title: TextView = itemView.findViewById(R.id.Title)

        fun bind(video: video) {
            // Set the video URI
            val videoUri = Uri.parse(video.videoUrl)
            videoView.setVideoURI(videoUri)

            // Start the video playback
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
            }

            // Set the user image, name, and title
            Glide.with(context)
                .load(video.profileUrl)
                .placeholder(R.drawable.profilepic) // optional, to show a placeholder image while loading
                .error(R.drawable.profilepic) // optional, to show an error image if loading fails
                .into(userImage)

            userName.text = video.username
            title.text = video.title
        }

        // Release the video view when the view holder is recycled
        fun recycle() {
            videoView.stopPlayback()
        }
    }
}
