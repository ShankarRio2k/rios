package com.example.rios.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rios.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
class Homeviewmodel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>> = _friends

    private val _suggestedUsers = MutableLiveData<List<User>>()
    val suggestedUsers: LiveData<List<User>> = _suggestedUsers

    init {
        getFriends()
        getSuggestedUsers()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    fun getFriends() {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("friends")
                .addSnapshotListener { friendSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error getting friends: ", error)
                        return@addSnapshotListener
                    }

                    val friendIds = friendSnapshot?.documents?.map { it.id }

                    if (friendIds.isNullOrEmpty()) {
                        _friends.value = emptyList()
                        return@addSnapshotListener
                    }

                    db.collection("profiles")
                        .whereNotEqualTo("id", currentUser.uid)
                        .whereIn("id", friendIds)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val users = mutableListOf<User>()
                            for (document in snapshot) {
                                val user = document.toObject(User::class.java)
                                users.add(user)
                            }
                            _friends.value = users
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error getting friend profiles: ", e)
                        }
                }
        }
    }
    fun addFriend(user: User) {
        val currentUser = Firebase.auth.currentUser
        val db = Firebase.firestore

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("friends")
                .document(user.id!!)
                .set(user)
                .addOnSuccessListener {
                    getFriends()
                }
        }
    }
    fun getSuggestedUsers() {
        currentUser?.uid?.let {
            db.collection("users").document(it).collection("friends")
                .addSnapshotListener { friendSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error getting friends: ", error)
                        return@addSnapshotListener
                    }

                    val friendIds: MutableList<String> =
                        friendSnapshot?.documents?.map { it.id }?.toMutableList() ?: mutableListOf()
                    friendIds.add(friendIds.size, currentUser.uid!!)

                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val suggestedUsers = mutableListOf<User>()
                            for (document in snapshot) {
                                val user = document.toObject(User::class.java)
                                suggestedUsers.add(user)
                            }
                            _suggestedUsers.value = suggestedUsers
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error getting suggested user profiles: ", e)
                        }
                }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
