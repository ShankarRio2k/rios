package com.example.rios.views

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.rios.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.postitem.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.Time
import java.util.*

class PostActivity : AppCompatActivity() {

    private val PICK_IMAGE = 123
    private lateinit var close: ImageView
    private lateinit var imgadded: ImageView
    private lateinit var description: EditText
    private lateinit var postimg: Button
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var ImageUriAccesstoken: String? = null
    private lateinit var imagepath: Uri
    var imageurl: String? = null
    private lateinit var _imageUri: Uri
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        close = findViewById(R.id.close)
        imgadded = findViewById(R.id.imgadded)
        description = findViewById(R.id.description)
        postimg = findViewById(R.id.postimg)
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

//        _imageUri = MutableLiveData()

        close.setOnClickListener {
            finish()
        }

        imgadded.setOnClickListener {
            launcher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    imgadded.setImageURI(uri)
                    _imageUri = uri
                }
            }
        }

        postimg.setOnClickListener {
            uploadimage()
        }
    }

    private fun uploadimage() {
        val pd = ProgressDialog(this@PostActivity)
        pd.setMessage("Uploading")
        pd.show()
        val posturi = UUID.randomUUID().toString()
        val imgref: StorageReference =
            firebaseAuth.uid?.let { storageReference.child("images").child(it).child(posturi) }!!
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, _imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        imgref.putBytes(data).addOnSuccessListener {
            imgref.downloadUrl.addOnSuccessListener { uri ->
                _imageUri = uri
                Toast.makeText(this@PostActivity, "Uri get success", Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener { task ->
                val downloaduri = task.result
                val user : User? = null
                imageurl = downloaduri.toString()
                val Postid = UUID.randomUUID().toString()
                val ref = firebaseFirestore.collection("Posts").document(Postid)
                val map: MutableMap<String, Any> = HashMap()
                map["postId"] = Postid
                map["imageUrl"] = _imageUri
                if (user != null) {
                    map["username"] = user.name
                }
                map["timestamp"] = Timestamp.now()
                map["caption"] = description.text.toString()
                map["userId"] = firebaseAuth?.currentUser?.uid.toString()
                println("ramuda")
                ref.set(map).addOnSuccessListener {
                    Toast.makeText(this@PostActivity, "map added", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        this@PostActivity,
                        "map not added",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                pd.dismiss()
                finish()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@PostActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}