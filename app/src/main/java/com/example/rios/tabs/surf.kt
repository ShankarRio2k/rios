package com.example.rios.tabs

import android.content.Intent
import android.os.Bundle
import android.service.controls.ControlsProviderService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.example.rios.adapter.postAdapter
import com.example.rios.databinding.FragmentSurfBinding
import com.example.rios.model.post
import com.example.rios.utils.FirebaseUtils
import com.example.rios.views.Homeviewmodel
import com.example.rios.views.PostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private lateinit var posts: MutableList<post>
    private lateinit var binding: FragmentSurfBinding
    val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

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
        val friends = currentUser?.uid?.let {
            db.collection("users").document(it)
                .collection("friends").get(Source.SERVER)
                .addOnSuccessListener { friendSnapshot ->
                    val friendIds = friendSnapshot.documents.map { it.id }

                    db.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException != null) {
                                Log.w(ControlsProviderService.TAG, "Listen failed.", firebaseFirestoreException)
                                return@addSnapshotListener
                            }
                            posts.clear()
                            val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser

                            // Add the new posts to the list
                            for (document in querySnapshot!!) {
                                val post = document.toObject(post::class.java)
                                if (post.userId != user?.uid && (post.userId == user?.uid || friendIds.contains(
                                        post.userId
                                    ))
                                ) {
                                    if (post.userId == user?.uid) {
                                        post.profileUrl = user.photoUrl.toString()
                                        post.username = user.displayName
                                    } else {
                                        db.collection("profiles").document(post.userId).get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                Log.d(
                                                    ControlsProviderService.TAG,
                                                    "onViewCreated: " + documentSnapshot.data
                                                )
                                                if (documentSnapshot.exists()) {
                                                    post.profileUrl =
                                                        documentSnapshot.getString("profilePic").toString()
                                                    post.username =
                                                        documentSnapshot.getString("name").toString()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(ControlsProviderService.TAG, "onViewCreated: ", e)

                                            }
                                    }
                                    posts.add(post)
                                }
                            }
                            // Notify the adapter that the data has changed
                            postAdapter.notifyDataSetChanged()
                        }

                }

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


