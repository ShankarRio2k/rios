package com.example.rios.views

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.example.rios.adapter.MessageAdapter
import com.example.rios.databinding.FragmentChatBinding
import com.example.rios.model.ChatMessage
import com.example.rios.model.User
import com.example.rios.tabs.talks
import com.example.rios.utils.FirebaseUtils
import com.example.rios.utils.SharedPreferenceUtils.setUsername
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class Chat() : Fragment() {
    private lateinit var newUser: User
    private lateinit var binding: FragmentChatBinding
    private var isTyping = false
    private var  REQUEST_PERMISSIONS_CODE = 11
//    private lateinit var users: MutableList<User>

    constructor(user: User) : this() {
        this.newUser = user
    }

    val TAG = "Chat"
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
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
        val userId = FirebaseUtils.firebaseAuth.currentUser?.uid.toString()

        SenderRoom = newUser.id + FirebaseUtils.firebaseAuth.currentUser!!.uid
        ReceiverRoom = FirebaseUtils.firebaseAuth.currentUser!!.uid + newUser.id
        Room = SenderRoom

        val currentMessages = ArrayList<ChatMessage>()
        val messageAdapter = MessageAdapter(requireContext(), currentMessages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = messageAdapter

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

        binding.chatInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    setTypingStatus(true)
                } else {
                    setTypingStatus(false)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
        

//        val userDetails = SharedPreferenceUtils.getUserDetails(requireContext())
        val username = newUser.name
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
        binding.chatAudioButton.isClickable = true
        binding.chatAudioButton.isFocusable = true

        binding.sendImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.chatAudioButton.setOnLongClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        RECORD_AUDIO,
                        WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_PERMISSIONS_CODE
                )
            } else {
                // Initialize a MediaRecorder object
                val recorder = MediaRecorder()

                // Set the audio source to the device's microphone
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)

                // Set the output format and file path
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                val externalCacheDir = requireContext().externalCacheDir
                val audioFilePath = "${externalCacheDir?.absolutePath}/audio_file.3gp"
                recorder.setOutputFile(audioFilePath)

                // Set the audio encoder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                } else {
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }

                try {
                    // Prepare and start recording
                    recorder.prepare()
                    recorder.start()
                } catch (e: Exception) {
                    // Handle exception here
                    e.printStackTrace()
                }

                binding.chatAudioButton.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        // Stop recording and release the MediaRecorder object
                        recorder.stop()
                        recorder.release()

                        // Upload the audio file to Firebase Storage
                        val audioFile = File(audioFilePath)
                        val storageRef = FirebaseStorage.getInstance().reference
                        val audioRef = storageRef.child("audio/${audioFile.name}")
                        audioRef.putFile(Uri.fromFile(audioFile))
                            .addOnSuccessListener { taskSnapshot ->
                                // File upload successful, get the download URL
                                audioRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    val downloadUrl = downloadUri.toString()

                                    // Create a message document in the sender's room collection
                                    val messageData = hashMapOf(
                                        "type" to "audio",
                                        "url" to downloadUrl,
                                        "timestamp" to FieldValue.serverTimestamp(),
                                        "senderId" to FirebaseUtils.firebaseAuth.currentUser!!.uid
                                    )

                                    db.collection("chat").document(SenderRoom)
                                        .collection("messages").add(messageData)
                                        .addOnSuccessListener { documentReference ->
                                            // Message document created successfully
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle message document creation failure here
                                            e.printStackTrace()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle file upload failure here
                                e.printStackTrace()
                            }

                        true // Return true to indicate that the event has been handled
                    } else {
                        false
                    }
                }


            }

            true // Return true to indicate that the event has been handled
        }

//        override fun performClick(): Boolean {
//            super.performClick()
//            // Add your custom click logic here
//            return true
//        }

        binding.back.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
        }

        binding.chatSendButton.setOnClickListener {
            val messageText = binding.chatInputEditText.text.toString().trim()
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
                binding.chatInputEditText.text?.clear()
                newUser.lastMessageTime = currentMessage.currenttime
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

    private fun setTypingStatus(isTyping: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("chat").document(Room)

        this.isTyping = isTyping

        if (isTyping) {
            docRef.update(currentUserId, true)
        } else {
            docRef.update(currentUserId, false)
        }
    }

//    val editText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.chatInputEditText)
//    editText.addTextChangedListener(textWatcher)

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            isTyping = s?.isNotBlank() == true
            updateMicAndSendImages()
        }
    }

    private fun updateMicAndSendImages() {
        val chatSendButton = binding.chatSendButton
        val chatAudioButton = binding.chatAudioButton

        if (isTyping) {
            chatSendButton.visibility = View.VISIBLE
            chatAudioButton.visibility = View.GONE
        } else {
            chatSendButton.visibility = View.GONE
            chatAudioButton.visibility = View.VISIBLE
        }
    }
}


