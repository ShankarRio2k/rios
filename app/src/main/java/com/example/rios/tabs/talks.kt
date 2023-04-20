package com.example.rios.tabs

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.example.rios.adapter.SuggestedAdapter
import com.example.rios.model.*
import com.example.rios.views.Homeviewmodel
import com.example.rios.adapter.UserAdapter
import com.example.rios.views.Chat
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_talks, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        users = mutableListOf()
        suggestedUsers = mutableListOf()
        userAdapter = UserAdapter(requireContext(), users) { user ->
            val fragment = Chat(user)
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.inner_container, fragment)
                .addToBackStack(null) // Add the fragment to the back stack
                .commit()
        }

        suggestedAdapter = SuggestedAdapter(requireContext(), suggestedUsers) { user ->
            users.add(user)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        talksViewModel.friends.observe(viewLifecycleOwner) { friends ->
            users.clear()
            users.addAll(friends)
            userAdapter.notifyDataSetChanged()
        }

        if (currentUser != null) {
            talksViewModel.loadFriends(currentUser.uid)
            getSuggestedUsers()
        }
    }

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
                    friendIds.add(currentUser.uid!!)
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
