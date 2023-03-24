package com.example.rios.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.databinding.FragmentSurfBinding
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts = mutableListOf()
        // Initialize the RecyclerView and adapter
        binding.postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), posts)
        binding.postRecyclerView.adapter = postAdapter
        binding.addpost.setOnClickListener {
            val intent = Intent(activity, PostActivity::class.java)
            startActivity(intent)
        }

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
                val user: FirebaseUser? = firebaseAuth.currentUser

                // Add the new posts to the list
                for (document in querySnapshot!!) {
                    val post = document.toObject(post::class.java)
                    if (post.userId == user?.uid) {
                        post.profileUrl = user.photoUrl.toString()
                        post.username = user.displayName
                    } else {
                        db.collection("profiles").document(post.userId).get()
                            .addOnSuccessListener { documentSnapshot ->
                                Log.d(TAG, "onViewCreated: " + documentSnapshot.data)
                                if (documentSnapshot.exists()) {
                                    post.profileUrl = documentSnapshot.getString("profilePic").toString()
                                    post.username = documentSnapshot.getString("name").toString()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "onViewCreated: ", e)

                            }
                    }
                    posts.add(post)
                }
                // Notify the adapter that the data has changed
                postAdapter.notifyDataSetChanged()
            }
    }

    companion object {
        private const val TAG = "SurfFragment"
    }

}
