package com.example.rios.views

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.example.rios.utils.FirebaseUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_chat.*

class Chat() : Fragment() {
    private lateinit var newUser: User
    private lateinit var db: FirebaseFirestore

    constructor(user: User) : this() {
        this.newUser = user
    }

    val TAG = "Chat"
    private lateinit var adapter: ChatAdapter
    private lateinit var messageList: ArrayList<ChatMessage>
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    var ReceiverRoom: String = ""
    var SenderRoom: String = ""
    var Room: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SenderRoom = newUser.id + FirebaseUtils.firebaseAuth.currentUser!!.uid
        ReceiverRoom = FirebaseUtils.firebaseAuth.currentUser!!.uid + newUser.id
        Room = SenderRoom

        val currentMessages = ArrayList<ChatMessage>()
        val messageAdapter = MessageAdapter(requireContext(), currentMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = messageAdapter

        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        // Retrieve messages from Firestore and update the chat
        db.collection("chat").document(Room).collection("messages")
            .orderBy("currenttime")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                currentMessages.clear()
                for (doc in value!!) {
                    Log.d(TAG, "${doc.id} => ${doc.data}")
                    val message = ChatMessage(
                        message = doc["message"].toString(),
                        currenttime = doc["currenttime"].toString(),
                        senderid = doc["senderid"].toString(),
                        room = SenderRoom
                    )
                    currentMessages.add(message)
                }
                messageAdapter.notifyDataSetChanged()
            }

        chatSendButton.setOnClickListener {
            val messageText = chatInputEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentMessage = ChatMessage(
                    message = messageText,
                    currenttime = Timestamp.now().toString(),
                    senderid = currentUserId,
                    room = SenderRoom
                )
                val message = hashMapOf(
                    "message" to currentMessage.message,
                    "currenttime" to currentMessage.currenttime,
                    "senderid" to currentMessage.senderid,
                    "room" to currentMessage.room
                )
                // Send the message to SenderRoom
                chatInputEditText.text?.clear()
                db.collection("chat").document(SenderRoom)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        // Send the same message to ReceiverRoom
                        db.collection("chat")
                            .document(ReceiverRoom)
                            .collection("messages")
                            .add(message)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Error sending message to ReceiverRoom", it)
                            }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Error sending message to SenderRoom", it)
                    }
            }
        }
    }
}
