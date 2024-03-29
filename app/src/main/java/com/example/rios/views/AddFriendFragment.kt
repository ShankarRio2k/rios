package com.example.rios.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.adapter.UserAdapter
import com.example.rios.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class AddFriendFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var users: MutableList<User>
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        users = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users) { user ->
            // handle user click event here
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.suggestions)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = userAdapter

        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            Log.d("talks", "Current user ID: ${currentUser.uid}")
//            db.collection("profiles")
//                .whereNotEqualTo("id", currentUser.uid) // exclude current user
//                .get()
//                .addOnSuccessListener { result ->
//                    for (document in result) {
//                        val user = document.toObject(User::class.java)
//                        users.add(user)
//                    }
//                    userAdapter.notifyDataSetChanged()
//                }
//                .addOnFailureListener { exception ->
//                    Log.w("talks", "Error getting users", exception)
//                }
//        }

        return view
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("profiles")
                .whereNotEqualTo("id", currentUser.uid) // exclude current user
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.w("friends", "Error getting users", exception)
                        return@addSnapshotListener
                    }
                    users.clear()
                    for (document in snapshot!!) {
                        val user = document.toObject(User::class.java)
                        users.add(user)
                    }
                    userAdapter.notifyDataSetChanged()
                }
        }
    }

    companion object {
        fun newInstance(title: String): AddFriendFragment {
            val fragment = AddFriendFragment()
            val args = Bundle()
            args.putString("talks", title)
            fragment.arguments = args
            return fragment
        }
    }
}