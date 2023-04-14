package com.example.rios.views

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rios.model.User
import com.example.rios.model.post
import com.example.rios.model.video
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Homeviewmodel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val TAG = "SurfViewModel"
    private val currentUser: FirebaseUser? = auth.currentUser
    private val firestore = Firebase.firestore
    private val videosCollectionRef = firestore.collection("videos")

    private val _posts = MutableLiveData<List<post>>()
    val posts: LiveData<List<post>>
        get() = _posts

    var videouri = MutableLiveData<Uri>()
    var username = MutableLiveData<String>()
    var bio = MutableLiveData<String>()
    var profileImageUrl = MutableLiveData<String>()

    val friends: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            getFriends()
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
                                val users = mutableListOf<User>()
                                for (document in snapshot!!) {
                                    val user = document.toObject(User::class.java)
                                    users.add(user)
                                }
                                friends.value = users
                            }
                    }
                }
        }
    }

    val suggestedUsers: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
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
                    friendIds.add(friendIds.size, currentUser.uid!!)
                    db.collection("profiles")
                        .whereNotIn("id", friendIds)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e)
                                return@addSnapshotListener
                            }
                            val suggestedUsers = mutableListOf<User>()
                            for (document in snapshot!!) {
                                val user = document.toObject(User::class.java)
                                suggestedUsers.add(user)
                            }
                            this.suggestedUsers.value = suggestedUsers
                        }
                }
        }
    }

    init {
        loadPosts()
    }

    private fun loadPosts() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    val friendIds = loadFriendIds(user.uid)
                    val posts = loadPosts(user.uid, friendIds)
                    _posts.value = posts
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading posts", e)
                }
            }
        }
    }

    fun getVideosFromFirestore(): LiveData<List<video>> {
        val videosLiveData = MutableLiveData<List<video>>()
        videosCollectionRef.get()
            .addOnSuccessListener { result ->
                val videosList = mutableListOf<video>()
                for (document in result) {
                    val video = document.toObject<video>()
                    videosList.add(video)
                }
                videosLiveData.value = videosList
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting videos from Firestore", exception)
            }
        return videosLiveData
    }

    private suspend fun loadFriendIds(userId: String): List<String> {
        val friendsSnapshot =
            db.collection("users").document(userId).collection("friends").get(Source.SERVER).await()
        return friendsSnapshot.documents.map { it.id }
    }

    private suspend fun loadPosts(userId: String, friendIds: List<String>): List<post> {
        val querySnapshot = db.collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get(Source.SERVER).await()

        val user = auth.currentUser
        return querySnapshot.documents.mapNotNull { document ->
            val post = document.toObject(post::class.java)
            if (post != null) {
                if (post.userId == user?.uid) {
                    // Skip the post if it was created by the current user
                    return@mapNotNull null
                } else if (friendIds.contains(post.userId)) {
                    val profileSnapshot =
                        db.collection("profiles").document(post.userId).get(Source.SERVER).await()
                    if (profileSnapshot.exists()) {
                        post.profileUrl = profileSnapshot.getString("profilePic").toString()
                        post.username = profileSnapshot.getString("name").toString()
                    }
                } else {
                    return@mapNotNull null
                }
            }
            return@mapNotNull post
        }
    }



//        fun addPost(title: String, content: String) {
//            val currentUser = auth.currentUser
//            currentUser?.let { user ->
//                val post = post(user.uid, title, content, System.currentTimeMillis())
//                db.collection("Posts").add(post)
//            }
//        }
}
