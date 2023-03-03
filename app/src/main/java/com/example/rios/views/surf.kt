package com.example.rios.views

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.new_post.*
import java.net.URL
import java.util.*


class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private val posts = listOf<post>()
    private lateinit var _imageUri: MutableLiveData<Uri>

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imgadded.setImageURI(uri)
                _imageUri.value = uri
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_surf, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.postRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), posts)
        recyclerView.adapter = postAdapter

        _imageUri = MutableLiveData<Uri>()

        val addPostButton = view.findViewById<FloatingActionButton>(R.id.addpost)
        addPostButton.setOnClickListener {
            showAddImageDialog()
        }

        _imageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                imgadded.setImageURI(uri)
            }
            else{
                Toast.makeText(requireContext(), "no image ", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showAddImageDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.new_post)

        val selectImageButton = dialog.findViewById<Button>(R.id.postimg)
        val selectedImageView = dialog.findViewById<ImageView>(R.id.imgadded)

        selectedImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        dialog.show()
        selectImageButton.setOnClickListener {
            val uri = _imageUri.value
            if (uri != null) {
                uploadImageToFirebaseStorageAndFirestore(requireContext(), uri, description.text.toString())
            } else {
                // Handle case where image URI is null
            }
        }
    }

    fun uploadImageToFirebaseStorageAndFirestore(
        context: Context,
        imageUri: Uri,
        caption: String,
    ) {
        // Create a unique filename for the image
        val filename = UUID.randomUUID().toString()

        // Get a reference to the Firebase Storage location where the image will be stored
        val storageRef = FirebaseStorage.getInstance().getReference("images/$filename")

        // Upload the image to Firebase Storage
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Get the download URL for the image
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    // Create a new post object with the image URL and other data
                    val post = post(
                        UUID.randomUUID().toString(), // Generate a unique post ID
                        FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        caption,
                        imageUrl,
                        Timestamp.now()
                    )

                    // Add the post to Firestore
                    FirebaseFirestore.getInstance().collection("posts")
                        .add(post)
                        .addOnSuccessListener { documentReference ->
                            // Call the onSuccess callback with the image URL

                        }
                        .addOnFailureListener { exception ->
                            // Call the onFailure callback with the exception

                        }
                }
            }
            .addOnFailureListener { exception ->
                // Call the onFailure callback with the exception

            }
    }

    companion object {
        // ...
    }
}
