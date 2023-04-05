package com.example.rios.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class talks : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var suggestedAdapter: SuggestedAdapter
    private lateinit var users: MutableList<User>
    private lateinit var Sugestedusers: MutableList<User>
    private val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_talks, container, false)
        users = mutableListOf()
        Sugestedusers = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users)
        suggestedAdapter = SuggestedAdapter(requireContext(), Sugestedusers) {
            users.add(it)
            userAdapter.notifyDataSetChanged()
            getSuggestedUsers()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerofuser)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = userAdapter

        val recyclerViewofSuggestedAdapter =
            view.findViewById<RecyclerView>(R.id.recyclerofsuggesteduser)
        recyclerViewofSuggestedAdapter?.layoutManager = LinearLayoutManager(activity)
        recyclerViewofSuggestedAdapter?.adapter = suggestedAdapter

        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUser != null) {

            GlobalScope.launch {
                getFriends()
                getSuggestedUsers()
            }
        }

        // Notify the userAdapter with the latest data
        userAdapter.notifyDataSetChanged()
    }

    private fun getFriends() {

        currentUser?.uid?.let {
            db.collection("users").document(it)
                .collection("friends").get(Source.SERVER)
                .addOnSuccessListener { friendSnapshot ->
                    val friendIds = friendSnapshot.documents.map { it.id }
                    if (friendIds.isNotEmpty()) {
                        db.collection("profiles")
                            .whereNotEqualTo("id", currentUser.uid)
                            .whereIn("id", friendIds)
                            .addSnapshotListener { snapshot, exception ->
                                if (exception != null) {
                                    Log.w("talks", "Error getting users", exception)
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
                }.addOnFailureListener { exception ->
                    Log.w("talks", "Error getting friends", exception)
                }
        }

    }

    private fun getSuggestedUsers() {
        currentUser?.uid?.let {
            db.collection("users").document(it).collection("friends").get()
                .addOnSuccessListener { friendSnapshot ->
                    val friendIds: MutableList<String> =
                        friendSnapshot.documents.map { it.id } as MutableList
                    friendIds.add(friendIds.size, currentUser.uid!!)

                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .addSnapshotListener { snapshot, exception ->
                            if (exception != null) {
                                Log.w("talks", "Error getting users", exception)
                                return@addSnapshotListener
                            }
                            Sugestedusers.clear()
                            for (document in snapshot!!) {
                                val user = document.toObject(User::class.java)
                                Sugestedusers.add(user)
                            }
                            suggestedAdapter.notifyDataSetChanged()
                        }
                }.addOnFailureListener { exception ->
                    Log.w("talks", "Error getting friends", exception)
                }
        }
    }

    companion object {
        fun newInstance(title: String): talks {
            val fragment = talks()
            val args = Bundle()
            args.putString("talks", title)
            fragment.arguments = args
            return fragment
        }
    }
}