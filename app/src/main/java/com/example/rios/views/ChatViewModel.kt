package com.example.rios.views

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatViewModel : ViewModel() {

    val messages: MutableLiveData<List<ChatMessage>> = MutableLiveData()
    private val db: FirebaseFirestore = Firebase.firestore

    fun getChatMessages(roomId: String): Query {
        return db.collection("Chats")
            .whereEqualTo("room", roomId)
            .orderBy("currenttime")
    }

    fun sendMessage(roomId: String, message: String, currentTime: String) {
        val chat = hashMapOf(
            "room" to roomId,
            "message" to message,
            "currenttime" to currentTime
        )
        db.collection("Chats")
            .add(chat)
            .addOnSuccessListener {
                // Message sent successfully
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

}
