package com.example.rios.views

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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.itenmessage.*

class Chat() : Fragment() {
    private lateinit var newUser: User

    constructor(user: User) : this() {
        this.newUser = user
    }

    val TAG = "Chat"
    private lateinit var adapter: ChatAdapter
    private lateinit var messageList: ArrayList<ChatMessage>
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

    var ReceiverRoom: String = ""
    var SenderRoom: String = ""

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

        val currentMessages = ArrayList<ChatMessage>()
        val messageAdapter = MessageAdapter(requireContext(), currentMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = messageAdapter

        Firebase.firestore.collection("chat").document(SenderRoom).collection("messages")
            .orderBy("currenttime").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    currentMessages.clear()
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        currentMessages.add(
                            ChatMessage(
                                message = document.data["message"].toString(),
                                currenttime = document.data["currenttime"].toString(),
                                senderid = document.data["senderid"].toString(),
                                room = SenderRoom
                            )
                        )
                    }
                    messageAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        //        adapter = ChatAdapter(currentUserId)


        chatSendButton.setOnClickListener {
            chatSendButton.setOnClickListener {
                val currentMessage = ChatMessage(
                    message = chatInputEditText.text.toString().trim(),
                    currenttime = Timestamp.now().toString(),
                    senderid = currentUserId,
                    room = SenderRoom
                )
                if (currentMessage.message?.isNotEmpty() == true) {
                    // Send the message
                    val message = hashMapOf(
                        "message" to currentMessage.message,
                        "currenttime" to currentMessage.currenttime,
                        "senderid" to currentMessage.senderid,
                    )
                    FirebaseFirestore.getInstance().collection("chat").document(SenderRoom)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener {
                            FirebaseFirestore.getInstance().collection("chat")
                                .document(ReceiverRoom)
                                .collection("messages")
                                .add(message)
                        }
                        .addOnFailureListener {

                        }

                    chatInputEditText.text?.clear()
                }
            }
        }
    }
}
