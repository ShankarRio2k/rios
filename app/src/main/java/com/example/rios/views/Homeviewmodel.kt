package com.example.rios.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rios.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
class Homeviewmodel : ViewModel() {

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> = _userList

    private val _suggestedUserList = MutableLiveData<List<User>?>()
    val suggestedUserList: MutableLiveData<List<User>?> = _suggestedUserList

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadFriends()
        loadSuggestedUsers()
    }

    private fun loadFriends() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val friendIds = mutableListOf<String>()

            db.collection("users").document(currentUser.uid)
                .collection("friends").get(Source.SERVER)
                .addOnSuccessListener { friendSnapshot ->
                    friendIds.addAll(friendSnapshot.documents.map { it.id })

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
                                _userList.value = users
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TalksViewModel", "Error getting friends", exception)
                }
        }
    }

    private fun loadSuggestedUsers() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val friendIds = mutableListOf<String>()

            db.collection("users").document(currentUser.uid)
                .collection("friends").get(Source.SERVER)
                .addOnSuccessListener { friendSnapshot ->
                    friendIds.addAll(friendSnapshot.documents.map { it.id })

                    friendIds.add(currentUser.uid)

                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .addSnapshotListener { snapshot, exception ->
                            if (exception != null) {
                                Log.w("TalksViewModel", "Error getting suggested users", exception)
                                return@addSnapshotListener
                            }

                            val users = snapshot?.toObjects(User::class.java)
                            _suggestedUserList.value = users
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w("TalksViewModel", "Error getting friends", exception)
                }
        }
    }
}
