package com.example.rios.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rios.model.ChatMessage
import com.example.rios.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _suggestedUsers = MutableLiveData<List<User>>()
    val suggestedUsers: LiveData<List<User>> = _suggestedUsers

    init {
        if (currentUser != null) {
            getFriends()
            getSuggestedUsers()
        }
    }

    private fun getFriends() {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .collection("friends").get(Source.SERVER)
                .addOnSuccessListener { friendSnapshot ->
                    val friendIds = friendSnapshot.documents.map { it.id }
                    if (friendIds.isNotEmpty()) {
                        db.collection("profiles")
                            .whereNotEqualTo("id", currentUser.uid)
                            .whereIn("id", friendIds)
                            .addSnapshotListener { snapshot, exception ->
                                if (exception != null) {
                                    Log.w("TalksViewModel", "Error getting users", exception)
                                    return@addSnapshotListener
                                }
                                val users = snapshot?.toObjects(User::class.java)
                                _users.value = users
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.w("TalksViewModel", "Error getting friends", exception)
                }
        }
    }

    private fun getSuggestedUsers() {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("friends").get()
                .addOnSuccessListener { friendSnapshot ->
                    val friendIds: MutableList<String> =
                        friendSnapshot.documents.map { it.id } as MutableList
                    friendIds.add(friendIds.size, currentUser.uid!!)

                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .addSnapshotListener { snapshot, exception ->
                            if (exception != null) {
                                Log.w("TalksViewModel", "Error getting users", exception)
                                return@addSnapshotListener
                            }
                            val suggestedUsers = snapshot?.toObjects(User::class.java)
                            _suggestedUsers.value = suggestedUsers
                        }
                }.addOnFailureListener { exception ->
                    Log.w("TalksViewModel", "Error getting friends", exception)
                }
        }
    }
}
