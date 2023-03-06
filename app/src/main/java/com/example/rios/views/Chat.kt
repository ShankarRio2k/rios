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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*

class Chat : Fragment() {
    val TAG = "Chat"
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Firebase.firestore.collection("messages").orderBy("currenttime").get()
            .addOnSuccessListener { documents ->
                val currentMessages = ArrayList<ChatMessage>()
                if (documents != null) {
                    currentMessages.clear()
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        currentMessages.add(
                            ChatMessage(
                                message = document.data["message"].toString(),
                                currenttime = document.data["currenttime"].toString(),
                                senderid = document.data["senderid"].toString(),
                                room = ""
                            )
                        )
                    }
                    chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    adapter = ChatAdapter(currentMessages)
                    chatRecyclerView.adapter = adapter
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        //        adapter = ChatAdapter(currentUserId)


        chatSendButton.setOnClickListener {
            val currentMessage = ChatMessage(
                message = chatInputEditText.text.toString().trim(),
                currenttime = Timestamp.now().toString(),
                senderid = currentUserId
            )
            if (currentMessage.message.isNotEmpty()) {
                // Send the message
                val message = hashMapOf(
                    "message" to currentMessage.message,
                    "currenttime" to currentMessage.currenttime,
                    "senderid" to currentMessage.senderid,
                )
                FirebaseFirestore.getInstance()
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }

                chatInputEditText.text?.clear()
            }
        }
    }
}
