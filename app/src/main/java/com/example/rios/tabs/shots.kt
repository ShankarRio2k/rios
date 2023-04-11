package com.example.rios.tabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.rios.R
import com.example.rios.adapter.Videoadapter
import com.example.rios.databinding.ActivityCreateaccountBinding
import com.example.rios.databinding.FragmentShotsBinding
import com.example.rios.views.AddShotFragment
import com.example.rios.views.Homeviewmodel
import kotlinx.android.synthetic.main.fragment_shots.*

class shots : Fragment() {
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
        val videoAdapter = Videoadapter(emptyList(), requireContext())

        // Set the adapter on the RecyclerView
        binding.videoList.adapter = videoAdapter

        // Fetch the videos from Firestore using the Homeviewmodel instance
        shotsViewmodel.getVideosFromFirestore().observe(viewLifecycleOwner) { videos ->
            // Update the list of videos in the adapter
            videoAdapter.updateVideos(videos)
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
