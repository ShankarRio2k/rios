package com.example.rios.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.rios.R
import com.example.rios.databinding.FragmentAddShotBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.offline.DownloadHelper.createMediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class AddShotFragment : Fragment() {
    private lateinit var binding: FragmentAddShotBinding
    private lateinit var exoPlayer: SimpleExoPlayer
    private val addshotsViewmodel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddShotBinding.inflate(inflater, container, false)
//        exoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
//        binding.AddedVideo.player = exoPlayer
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addshotsViewmodel.videouri.observe(viewLifecycleOwner) { uri ->
//            val mediaSource = MediaSourceFactory.createMediaSource(requireContext(), uri)
//            exoPlayer.prepare(mediaSource)
//            exoPlayer.playWhenReady = true
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()

    }

    companion object {

    }
}