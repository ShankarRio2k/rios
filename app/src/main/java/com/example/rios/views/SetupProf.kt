package com.example.rios.views

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.databinding.ActivityCreateaccountBinding
import com.example.rios.databinding.ActivitySetupProfBinding
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.example.rios.utils.SharedPrefernceHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Random
import java.util.UUID


class SetupProf : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySetupProfBinding
    private var _imageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.circularImageView.setImageURI(uri)
                _imageUri = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

//        val sharedPreferencesHelper = SharedPrefernceHelper(this)
//        val (username, bio, profileImageUrl) = sharedPreferencesHelper.getUserDetails()
        val userId = FirebaseUtils.firebaseAuth.currentUser?.uid.toString()

        binding.saveprofile.setOnClickListener {
            val id = firebaseAuth.currentUser?.uid
            val name = binding.getusername.text.toString()
            val bio = binding.getuserbio.text.toString()
            val profilePic = _imageUri.toString()
//        if (firebaseAuth.currentUser.isAnonymous)
            if (id != null) {
                saveProfileData(id, name, bio, profilePic)
                createProgressDialog()
                progressDialog.show()
            }
        }

        binding.circularImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun createProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Uploading")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.max = 100
    }

    private fun saveProfileData(id: String, name: String, bio: String, profilePic: String) {
        val profileMap = hashMapOf(
            "id" to id,
            "name" to name,
            "bio" to bio,
            "profilePic" to profilePic
        )
        val storageRef = FirebaseStorage.getInstance().reference

        val imageRef = storageRef.child("profiles/${id}/profilePic")

        if (_imageUri != null) {
            imageRef.putFile(_imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(it)
                            .setDisplayName(name)
                            .build()

                            firebaseAuth.currentUser?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        profileMap["profilePic"] = it.toString()
                                        Log.d(TAG, "User profile updated.")
                                        saveProfileDataToFirestore(profileMap)
                                    } else {
                                        Log.w(TAG, "Failed to update user profile.", task.exception)
                                    }
                                }
                        }
                    }
                        .addOnFailureListener {
                            Log.w(TAG, "Error uploading profile picture", it)
//                    saveProfileDataToFirestore(profileMap)
                        }
                }
    }

    private fun saveProfileDataToFirestore(profileMap: HashMap<String, String>) {
        firebaseAuth.currentUser?.uid?.let {
            db.collection("profiles").document(it).set(profileMap)
                .addOnSuccessListener { documentReference ->
//                    Log.d(TAG, "Profile data saved with ID: ${documentReference.id}")
                    toast("Profile added")
                    progressDialog.dismiss()
                    binding.progressofsetprof.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error adding profile data", exception)
                }
        }
    }
}
