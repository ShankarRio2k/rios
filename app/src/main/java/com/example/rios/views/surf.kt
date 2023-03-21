package com.example.rios.views

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.databinding.FragmentSurfBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private lateinit var posts: MutableList<post>
    private lateinit var binding: FragmentSurfBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSurfBinding.inflate(inflater, container, false)
        posts = mutableListOf()

        // Initialize the RecyclerView and adapter
        binding.postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), posts)
        binding.postRecyclerView.adapter = postAdapter

        // Load the posts from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                posts.clear()

                binding.addpost.setOnClickListener{
                    val intent = Intent(activity, PostActivity::class.java)
                    startActivity(intent)
                }

                // Add the new posts to the list
                for (document in querySnapshot!!) {
                    val post = document.toObject(post::class.java)
                    posts.add(post)
                }
                // Notify the adapter that the data has changed
                postAdapter.notifyDataSetChanged()
            }

        return binding.root
    }

    companion object {
        private const val TAG = "SurfFragment"
    }

}
