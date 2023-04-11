package com.example.rios.views

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.adapter.MessageAdapter
import com.example.rios.databinding.FragmentChatBinding
import com.example.rios.databinding.FragmentSurfBinding
import com.example.rios.model.ChatMessage
import com.example.rios.model.User
import com.example.rios.tabs.talks
import com.example.rios.utils.FirebaseUtils
import com.example.rios.utils.SharedPreferenceUtils
import com.example.rios.utils.SharedPreferenceUtils.getUsername
import com.example.rios.utils.SharedPreferenceUtils.setUsername
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_chat.*

class Chat() : Fragment() {
    private lateinit var newUser: User
    private lateinit var binding: FragmentChatBinding

    constructor(user: User) : this() {
        this.newUser = user
    }

    val TAG = "Chat"
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var _imageUri: MutableLiveData<Uri>
    var ReceiverRoom: String = ""
    var SenderRoom: String = ""
    var Room: String = ""

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val storageRef = FirebaseStorage.getInstance().reference
                val db = FirebaseFirestore.getInstance()

                val imageRef = storageRef.child("images/msgimages")
                imageRef.putFile(it).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        val currentMessage = ChatMessage(
                            message = "",
                            currenttime = Timestamp.now().toString(),
                            senderid = currentUserId,
                            room = SenderRoom,
                            image = imageUrl
                        )
                        val message = hashMapOf(
                            "message" to currentMessage.message,
                            "currenttime" to currentMessage.currenttime,
                            "senderid" to currentMessage.senderid,
                            "room" to currentMessage.room,
                            "image" to currentMessage.image
                        )

                        db.collection("chat").document(SenderRoom).collection("messages")
                            .add(message).addOnSuccessListener {
                                // Send the same message to ReceiverRoom
                                db.collection("chat").document(ReceiverRoom)
                                    .collection("messages").add(message).addOnSuccessListener {

                                    }.addOnFailureListener {
                                        Log.e(TAG, "Error sending message to ReceiverRoom", it)
                                    }
                            }.addOnFailureListener {
                                Log.e(TAG, "Error sending message to SenderRoom", it)
                            }
                    }
                }.addOnFailureListener {
                    Log.w(ContentValues.TAG, "Error uploading profile picture", it)
//                    saveProfileDataToFirestore(profileMap)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChatBinding.bind(view)

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = FirebaseUtils.firebaseAuth.currentUser?.uid.toString()

        SenderRoom = newUser.id + FirebaseUtils.firebaseAuth.currentUser!!.uid
        ReceiverRoom = FirebaseUtils.firebaseAuth.currentUser!!.uid + newUser.id
        Room = SenderRoom

        val currentMessages = ArrayList<ChatMessage>()
        val messageAdapter = MessageAdapter(requireContext(), currentMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = messageAdapter

        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
        db.firestoreSettings = settings

        // Retrieve messages from Firestore and update the chat
        db.collection("chat").document(Room).collection("messages").orderBy("currenttime")
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
                        room = SenderRoom,
                        image = (if (doc["image"] != null) Uri.parse(doc["image"].toString()) else null)
                    )
                    currentMessages.add(message)
                }
                messageAdapter.notifyDataSetChanged()
            }

        val userDetails = SharedPreferenceUtils.getUserDetails(requireContext())
        val username = userDetails.first
        if (username.isNullOrEmpty()) {
            val userRef = firebaseFirestore.collection("profiles").document(userId)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val username = documentSnapshot.getString("name")

                binding.chatuser.text = username
                // Store the username in SharedPreferences
                if (username != null) {
                    setUsername(requireContext(), username)
                }
            }
        } else {
            binding.chatuser.text = username
        }

        binding.sendImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.back.setOnClickListener {
            val newFragment = talks() // Replace with the fragment you want to load
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.inner_container, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.chatSendButton.setOnClickListener {
            val messageText = chatInputEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val currentMessage = ChatMessage(
                    message = messageText,
                    currenttime = Timestamp.now().toString(),
                    senderid = currentUserId,
                    room = SenderRoom,
                    image = null
                )
                val message = hashMapOf(
                    "message" to currentMessage.message,
                    "currenttime" to currentMessage.currenttime,
                    "senderid" to currentMessage.senderid,
                    "room" to currentMessage.room
                )
                // Send the message to SenderRoom
                chatInputEditText.text?.clear()
                db.collection("chat").document(SenderRoom).collection("messages").add(message)
                    .addOnSuccessListener {
                        // Send the same message to ReceiverRoom
                        db.collection("chat").document(ReceiverRoom).collection("messages")
                            .add(message).addOnSuccessListener {

                            }.addOnFailureListener {
                                Log.e(TAG, "Error sending message to ReceiverRoom", it)
                            }
                    }.addOnFailureListener {
                        Log.e(TAG, "Error sending message to SenderRoom", it)
                    }
            }
        }
    }
}


