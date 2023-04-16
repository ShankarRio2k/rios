package com.example.rios.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.rios.R
import com.example.rios.adapter.VideoListAdapter
import com.example.rios.adapter.onclickvideo
import com.example.rios.databinding.FragmentShotsBinding
import com.example.rios.model.video
import com.example.rios.views.AddShotFragment
import com.example.rios.views.Homeviewmodel

class shots : Fragment(), VideoListAdapter.OnVideoClickListener {
    private lateinit var binding: FragmentShotsBinding
    val REQUEST_CODE = 11
    private val shotsViewmodel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShotsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create an instance of the VideoAdapter class
        val videoAdapter = VideoListAdapter(this)

        // Set the adapter on the RecyclerView
        binding.videoList.adapter = videoAdapter

        // Set the orientation of the LinearLayoutManager to horizontal
        binding.videoList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Add a SnapHelper to snap the videos to the center of the screen
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.videoList)

        // Fetch the videos from Firestore using the HomeViewModel instance
        shotsViewmodel.getVideosFromFirestore().observe(viewLifecycleOwner) { videos ->
            // Update the list of videos in the adapter
            videoAdapter.submitList(videos)
        }

        binding.addvideo.setOnClickListener {
            // Create an instance of the AddShotFragment
            val addShotFragment = AddShotFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.shots_container, addShotFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }
    override fun onStart() {
        super.onStart()
        val adapter = binding.videoList.adapter as? VideoListAdapter
        adapter?.startPlayer()
    }

    override fun onStop() {
        super.onStop()
        val adapter = binding.videoList.adapter as? VideoListAdapter
        adapter?.stopPlayer()
    }

    override fun onVideoClick(video: video) {
        // handle video click event
    }

    companion object {
        fun newInstance(title: String): shots {
            val fragment = shots()
            val args = Bundle()
            args.putString("shots", title)
            fragment.arguments = args
            return fragment
        }
    }
}
