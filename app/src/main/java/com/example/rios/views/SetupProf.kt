package com.example.rios.views

import android.app.Activity
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rios.R
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_setup_prof.*
import java.util.Random
import java.util.UUID


class SetupProf : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "SetProfile"
    private val IMAGE_PICK_CODE = 1
    private val REQUEST_CAMERA = 11
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri>
        get() = _imageUri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_prof)

        firebaseAuth = FirebaseAuth.getInstance()

        saveprofile.setOnClickListener {
            val id = FirebaseFirestore.getInstance().collection("profiles").document().id
            val name = getusername.text.toString()
            val bio = getuserbio.text.toString()
            val profilePic  = ""
//            if (firebaseAuth.currentUser.isAnonymous)
            saveProfileData(id, name, bio , profilePic)
        }
        circularImageView.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            data?.data?.let { uri ->
                circularImageView.setImageURI(uri)
                _imageUri.value = uri
            }
        }
    }

    private fun saveProfileData(id: String,name: String, bio: String,profilePic: String) {
        val id = FirebaseFirestore.getInstance().collection("profiles").document().id
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
        db.collection("profiles")
            .add(profileMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Profile data saved with ID: ${documentReference.id}")
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


