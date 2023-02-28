package com.example.rios.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.itenmessage.*

class Chat : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private var messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        chatAdapter = ChatAdapter(requireContext(), messages, firebaseAuth.currentUser!!.uid)
        chatRecyclerView.adapter = chatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatSendButton.setOnClickListener {
            val message = chatInputEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }
        db.collection("messages")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot) {
                        val chatMessage = doc.toObject(ChatMessage::class.java)
                        messages.add(chatMessage)
                    }
                    chatAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }
    private fun sendMessage(messageText: String) {
        val userId = firebaseAuth.currentUser!!.uid
        val userName = firebaseAuth.currentUser!!.displayName ?: "Unknown"
        val message = ChatMessage(messageText, userId, userName, Timestamp.now())
        db.collection("messages")
            .add(message)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Message sent with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding message", e)
            }
        chatInputEditText.setText("")
    }
    companion object {
        private const val TAG = "ChatFragment"
    }
}

