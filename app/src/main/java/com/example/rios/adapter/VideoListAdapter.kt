package com.example.rios.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.databinding.VideoItemBinding
import com.example.rios.model.video
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
class VideoListAdapter(private val listener: OnVideoClickListener) : ListAdapter<video, VideoListAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var currentPlayerViewHolder: ViewHolder? = null
    private var currentlyPlayingPosition = RecyclerView.NO_POSITION

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<video>() {
            override fun areItemsTheSame(oldItem: video, newItem: video): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: video, newItem: video): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val video = getItem(position)
        holder.bind(video)
        holder.itemView.setOnClickListener {
            currentPlayerViewHolder?.stopVideo()
            holder.playVideo()
            currentPlayerViewHolder = holder
            currentlyPlayingPosition = position
        }

        if (currentlyPlayingPosition == position) {
            holder.playVideo()
            currentPlayerViewHolder = holder
        } else {
            holder.stopVideo()
        }
    }

    fun stopCurrentVideo() {
        currentPlayerViewHolder?.stopVideo()
        currentlyPlayingPosition = RecyclerView.NO_POSITION
    }

    fun startPlayer() {
        currentPlayerViewHolder?.playVideo()
    }

    fun stopPlayer() {
        currentPlayerViewHolder?.stopVideo()
        currentlyPlayingPosition = RecyclerView.NO_POSITION
    }

    class ViewHolder(private val binding: VideoItemBinding, private val listener: OnVideoClickListener) : RecyclerView.ViewHolder(binding.root) {
        private var player: SimpleExoPlayer? = null

        init {
            player = SimpleExoPlayer.Builder(binding.root.context).build()
            binding.playerView.player = player
        }

        fun bind(video: video) {
            binding.Title.text = video.title
            binding.userName.text = video.username
            Glide.with(binding.root.context)
                .load(video.profileUrl)
                .placeholder(R.drawable.profilepic)
                .error(R.drawable.profilepic)
                .into(binding.userImage)
            val mediaItem = MediaItem.fromUri(video.videoUrl)
            val mediaSourceFactory = DefaultMediaSourceFactory(binding.root.context)
            val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

            player?.setMediaSource(mediaSource)
            player?.prepare()
            player?.playWhenReady = false
        }

        fun playVideo() {
            player?.playWhenReady = true
        }

        fun stopVideo() {
            player?.playWhenReady = false
        }

        fun releasePlayer() {
            player?.release()
            player = null
        }
    }

    interface OnVideoClickListener {
        fun onVideoClick(video: video)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.releasePlayer()
        super.onViewRecycled(holder)
    }
}
