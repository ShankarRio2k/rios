package com.example.rios.tabs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.example.rios.adapter.postAdapter
import com.example.rios.databinding.FragmentSurfBinding
import com.example.rios.model.post
import com.example.rios.views.Homeviewmodel
import com.example.rios.views.PostActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class surf : Fragment(R.layout.fragment_surf) {

    private lateinit var binding: FragmentSurfBinding
    private lateinit var postAdapter: postAdapter
    private val surfViewModel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSurfBinding.bind(view)

        // Initialize the RecyclerView and adapter
        binding.postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), mutableListOf())
        binding.postRecyclerView.adapter = postAdapter

        binding.addpost.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)
            startActivity(intent)
        }

        // Observe the posts LiveData and update the adapter when the data changes
        surfViewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.updatePosts(posts)
        }
    }

    companion object {
        fun newInstance(title: String): surf {
            val fragment = surf()
            val args = Bundle()
            args.putString("surf", title)
            fragment.arguments = args
            return fragment
        }
    }
}