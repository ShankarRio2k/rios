package com.example.rios.tabs

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.adapter.SuggestedAdapter
import com.example.rios.model.User
import com.example.rios.views.Homeviewmodel
import com.example.rios.views.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class talks : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var suggestedAdapter: SuggestedAdapter
    private lateinit var users: MutableList<User>
    private val talksViewModel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }
    private lateinit var suggestedUsers: MutableList<User>
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_talks, container, false).also {
        users = mutableListOf()
        suggestedUsers = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users)
        suggestedAdapter = SuggestedAdapter(requireContext(), suggestedUsers) {
            users.add(it)
            userAdapter.notifyDataSetChanged()
            getSuggestedUsers()

        }

        val recyclerView = it.findViewById<RecyclerView>(R.id.recyclerofuser)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = userAdapter

        val recyclerViewofSuggestedAdapter =
            it.findViewById<RecyclerView>(R.id.recyclerofsuggesteduser)
        recyclerViewofSuggestedAdapter?.layoutManager = LinearLayoutManager(activity)
        recyclerViewofSuggestedAdapter?.adapter = suggestedAdapter

        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (currentUser != null) {
            getFriends()
            getSuggestedUsers()
        }
    }

    private fun getFriends() {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("friends")
                .addSnapshotListener { friendSnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    val friendIds = friendSnapshot?.documents?.map { it.id }
                    if (friendIds != null && friendIds.isNotEmpty()) {
                        db.collection("profiles")
                            .whereNotEqualTo("id", currentUser.uid)
                            .whereIn("id", friendIds)
                            .addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.w(TAG, "Listen failed.", e)
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
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getSuggestedUsers() {
        currentUser?.uid?.let {
            val friendIds: MutableList<String> = mutableListOf()
            db.collection("users").document(it).collection("friends")
                .addSnapshotListener { friendSnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    friendIds.addAll(friendSnapshot?.documents?.map { it.id } ?: listOf())
                    friendIds.add(friendIds.size, currentUser.uid!!)
                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e)
                                return@addSnapshotListener
                            }
                            suggestedUsers.clear()
                            for (document in snapshot!!) {
                                val user = document.toObject(User::class.java)
                                suggestedUsers.add(user)
                            }
                            suggestedAdapter.notifyDataSetChanged()
                        }
                }
        }
    }

    override fun onStart() {
        super.onStart()

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
