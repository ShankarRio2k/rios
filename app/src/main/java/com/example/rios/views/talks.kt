package com.example.rios.views

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


import kotlinx.android.synthetic.main.fragment_talks.*

class talks : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var users: MutableList<User>
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_talks, container, false)
        users = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerofuser)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = userAdapter

        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("talks", "Current user ID: ${currentUser.uid}")
            db.collection("profiles")
                .whereNotEqualTo("id", currentUser.uid) // exclude current user
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val user = document.toObject(User::class.java)
                        users.add(user)
                    }
                    userAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("talks", "Error getting users", exception)
                }
        }

        return view
    }
}