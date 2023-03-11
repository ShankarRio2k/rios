package com.example.rios.views

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_setup_prof.*
import kotlinx.android.synthetic.main.new_post.*
import java.util.*


class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private lateinit var posts: MutableList<post>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_surf, container, false)

        // Initialize the list of posts
        posts = mutableListOf()

        // Initialize the RecyclerView and adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.postRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), posts)
        recyclerView.adapter = postAdapter

        // Load the posts from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                posts.clear()

                // Add the new posts to the list
                for (document in querySnapshot!!) {
                    val post = document.toObject(post::class.java)
                    posts.add(post)
                }

                // Notify the adapter that the data has changed
                postAdapter.notifyDataSetChanged()
            }

        return view
    }

    companion object {
        private const val TAG = "PostListFragment"
    }

}
