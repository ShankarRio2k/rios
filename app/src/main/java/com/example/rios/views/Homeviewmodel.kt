package com.example.rios.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rios.model.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Homeviewmodel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val TAG = "SurfViewModel"
    private val currentUser: FirebaseUser? = auth.currentUser
    private val firestore = Firebase.firestore
    private val videosCollectionRef = firestore.collection("videos")
    private val _posts = MutableLiveData<List<post>>()
    private val userProfileCache = mutableMapOf<String, User>()

    val posts: LiveData<List<post>>
        get() = _posts
//    private val dao = DatabaseProvider.getDatabase(application).friendsDao()
//private val database = DatabaseProvider.getDatabase()

    // Create a private property for FriendsDao and initialize it in the init block
//    private val dao = database.friendsDao()

    private val _friends = MutableLiveData<List<User>>()
    val friends: LiveData<List<User>>
        get() = _friends

    fun loadFriends(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch the friends from Firestore asynchronously
            currentUser?.uid?.let { uid ->
                db.collection("users").document(uid).collection("friends")
                    .addSnapshotListener { friendSnapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Error getting friends", error)
                            return@addSnapshotListener
                        }

                        friendSnapshot?.let {
                            val friendIds = it.documents.map { doc -> doc.id }
                            if (friendIds.isNotEmpty()) {
                                val cachedUserProfiles = mutableListOf<User>()

                                // Look up cached user profiles first
                                for (friendId in friendIds) {
                                    userProfileCache[friendId]?.let { cachedUserProfiles.add(it) }
                                }

                                // Query Firestore for user profiles that are not cached
                                val uncachedFriendIds = friendIds.filter { userProfileCache[it] == null }
                                if (uncachedFriendIds.isNotEmpty()) {
                                    db.collection("profiles")
                                        .whereNotEqualTo("id", currentUser.uid)
                                        .whereIn("id", uncachedFriendIds)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            val users = snapshot.map { document ->
                                                val user = document.toObject<User>()
                                                User(user.id, user.name, user.bio, user.imageUrl, false, null)
                                            }

                                            // Cache the retrieved user profiles
                                            for (user in users) {
                                                userProfileCache[user.id] = user
                                            }

                                            cachedUserProfiles.addAll(users)

                                            viewModelScope.launch(Dispatchers.Main) {
                                                _friends.value = cachedUserProfiles
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w(TAG, "Error getting suggested users", exception)
                                        }
                                } else {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        _friends.value = cachedUserProfiles
                                    }
                                }
                            }
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
