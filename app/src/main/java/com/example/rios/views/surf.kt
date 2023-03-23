package com.example.rios.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.databinding.FragmentSurfBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private lateinit var posts: MutableList<post>
    private lateinit var binding: FragmentSurfBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

                    db.collection("profiles").document(post.userId).get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                post.profileUrl = documentSnapshot.getString("profilePic").toString()
                                post.username = documentSnapshot.getString("name").toString()
                            }
                        }
                        .addOnFailureListener { println(",,,,,,,,,,,,,,,,") }

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
