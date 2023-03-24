package com.example.rios.views

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
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
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rios.R
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_setup_prof.*
import java.util.Random
import java.util.UUID


class SetupProf : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var _imageUri: MutableLiveData<Uri>

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                circularImageView.setImageURI(uri)
                _imageUri.value = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_prof)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        _imageUri = MutableLiveData()
        val firebaseAuth = FirebaseAuth.getInstance()

        saveprofile.setOnClickListener {
            val id = firebaseAuth.currentUser?.uid
            val name = getusername.text.toString()
            val bio = getuserbio.text.toString()
            val profilePic = ""
//        if (firebaseAuth.currentUser.isAnonymous)
            if (id != null) {
                saveProfileData(id, name, bio, profilePic)
            }
        }

        circularImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
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

        if (_imageUri.value != null) {
            imageRef.putFile(_imageUri.value!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(it)
                            .setDisplayName(name)
                            .build()

                        firebaseAuth.currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")
                                } else {
                                    Log.w(TAG, "Failed to update user profile.", task.exception)
                                }
                            }
                        profileMap["profilePic"] = it.toString()
                        saveProfileDataToFirestore(profileMap)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Error uploading profile picture", it)
//                    saveProfileDataToFirestore(profileMap)
                }
        } else {
//            saveProfileDataToFirestore(profileMap)
        }
    }

    private fun saveProfileDataToFirestore(profileMap: HashMap<String, String>) {
        firebaseAuth.currentUser?.uid?.let {
            db.collection("profiles").document(it).set(profileMap)
                .addOnSuccessListener { documentReference ->
//                    Log.d(TAG, "Profile data saved with ID: ${documentReference.id}")
                    toast("Profile added")
                    progressofsetprof.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error adding profile data", exception)
                }
        }
    }
}
