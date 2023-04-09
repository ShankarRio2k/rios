package com.example.rios.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.model.video
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.video_item.view.*

class Videoadapter(private val videos: List<video>) :
    RecyclerView.Adapter<Videoadapter.VideoViewHolder>() {

    public var player: SimpleExoPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.detachPlayer()
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.attachPlayer()
    }

    override fun getItemCount() = videos.size

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val playerView: PlayerView = itemView.playerView
        private lateinit var mediaSource: MediaSource
        private var player: SimpleExoPlayer? = null

        fun bind(video: video) {
            // Create a data source factory to create a media source
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                itemView.context,
                Util.getUserAgent(itemView.context, "app-name")
            )

            // Create a media source for the video and attach it to the player
            val videoUri = Uri.parse(video.videoUrl)
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))

            // Create a player and attach it to the player view
            player = SimpleExoPlayer.Builder(itemView.context).build()
            playerView.player = player
            player!!.setMediaSource(mediaSource)
            player!!.prepare()
            player!!.play()
        }

        fun attachPlayer() {
            if (player == null) {
                player = SimpleExoPlayer.Builder(itemView.context).build()
            }
            // Attach the player to the player view when it is reattached to the window
            playerView.player = player
        }

        fun detachPlayer() {
            // Detach the player from the player view when it is detached from the window
            playerView.player = null
            player?.release()
            player = null
        }
    }

}
