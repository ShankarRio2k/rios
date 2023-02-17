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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class talks : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var users: MutableList<User>
    // Get an instance of FirebaseAuth
    val auth = FirebaseAuth.getInstance()

// Call the signOut() method to sign out the current user


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_talks, container, false)
        users = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerofuser)
        val signout = view.findViewById<FloatingActionButton>(R.id.signout)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = userAdapter

        signout.setOnClickListener{
            auth.signOut()
            signout.setOnClickListener {
                // Sign the user out
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), signin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

        }

        db.collection("profiles")
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

        return view
    }

}




