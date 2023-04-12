package com.example.rios.views

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64.DEFAULT
import android.util.Base64
import android.util.Base64.encodeToString
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.rios.databinding.FragmentAddShotBinding
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class AddShotFragment : Fragment() {
    private lateinit var binding: FragmentAddShotBinding

    //    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var PICK_VEDIO = 11
    private var videoUri: Uri? = null
    private val addshotsViewmodel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddShotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize MediaController and ExoPlayer
        val mediaController = MediaController(requireContext())
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = firebaseStorage.reference
        // Handle click on add video button
        binding.addvideo.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(intent, PICK_VEDIO)
        }

        // Handle click on save video button
        binding.buttonSaveVideo.setOnClickListener {
            // Check if video is selected
            if (videoUri != null) {
                uploadVideo()

            } else {
                val snackbar = Snackbar.make(
                    requireView(),
                    "Please select a video",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.show()
            }
        }
    }

    private fun uploadVideo() {
        val pd = ProgressDialog(requireContext())
        pd.setMessage("Uploading")
        pd.show()

        // Get a unique ID for the video
        val videoId = UUID.randomUUID().toString()

        // Get a reference to the video storage location
        val videoRef: StorageReference = firebaseAuth.currentUser?.uid?.let {
            storageReference.child("videos").child(it).child("$videoId.mp4")
        }!!

        // Upload the selected video to Firebase Storage
        videoRef.putFile(videoUri!!).addOnProgressListener { taskSnapshot ->
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            pd.setMessage("Uploading: $progress%")
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            videoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val videoUrl = task.result.toString()
                Toast.makeText(
                    requireContext(),
                    "Video uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Create a thumbnail for the video
                val thumbnail: Bitmap? = videoUri?.path?.let { it1 ->
                    ThumbnailUtils.createVideoThumbnail(
                        it1,
                        MediaStore.Images.Thumbnails.MINI_KIND
                    )
                }

                thumbnail?.let {
                    // Convert the thumbnail to a base64-encoded string
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val thumbnailBytes = baos.toByteArray()
                    val thumbnailString = Base64.encodeToString(thumbnailBytes, Base64.DEFAULT)

                    // Retrieve user's username and profile image URL from database
                    val userId = firebaseAuth.currentUser?.uid.toString()
                    val userRef = firebaseFirestore.collection("profiles").document(userId)
                    userRef.get().addOnSuccessListener { documentSnapshot ->
                        val username = documentSnapshot.getString("name")
                        val profileImageUrl = documentSnapshot.getString("profilePic")

                        // Create a map object with post data including username, profile image URL, and thumbnail
                        val shotId = UUID.randomUUID().toString()
                        val postMap: MutableMap<String, Any> = HashMap()
                        postMap["id"] = shotId
                        postMap["userId"] = userId
                        postMap["timestamp"] = Timestamp.now()
                        postMap["description"] = binding.videoDescription.text.toString()
                        postMap["videoUrl"] = videoUrl
                        postMap["username"] = username.toString()
                        postMap["profileUrl"] = profileImageUrl.toString()
                        postMap["thumbnail"] = thumbnailString // Add thumbnail to postMap

                        // Add the post data to Firebase Firestore
                        val postRef = firebaseFirestore.collection("videos").document(shotId)
                        postRef.set(postMap).addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Post added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            pd.dismiss()
                        }.addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Failed to add post",
                                Toast.LENGTH_SHORT
                            ).show()
                            pd.dismiss()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to upload video",
                    Toast.LENGTH_SHORT
                ).show()
                pd.dismiss()
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VEDIO && resultCode == RESULT_OK && data != null && data.data != null) {
            videoUri = data.data!!
            binding.addvideo.isVisible = false
            binding.AddedVideo.isVisible = true
            binding.AddedVideo.setVideoURI(videoUri)
            binding.AddedVideo.start()

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), videoUri)

            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
            if (duration > 60_000) {
                Toast.makeText(
                    requireContext(),
                    "please select a video less than 1 min ",
                    Toast.LENGTH_SHORT
                ).show()
                binding.addvideo.isVisible = true
                binding.AddedVideo.isVisible = false
                binding.AddedVideo.stopPlayback()
            } else {

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}