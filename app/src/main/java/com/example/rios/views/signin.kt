package com.example.rios.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.rios.R
import com.example.rios.databinding.ActivitySigninBinding
import com.example.rios.databinding.FragmentChatBinding
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth

class signin : AppCompatActivity() {
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    private lateinit var binding: ActivitySigninBinding
    lateinit var signInInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInInputsArray = arrayOf(binding.etSignInEmail, binding.etSignInPassword)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        binding.btnCreateAccount2.setOnClickListener {
            startActivity(Intent(this, Createaccount::class.java))
            finish()
        }
        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {
        signInEmail = binding.etSignInEmail.text.toString().trim()
        signInPassword = binding.etSignInPassword.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(this, Createaccount::class.java))
                        toast("signed in successfully")
                        finish()
                    } else {
                        toast("sign in failed")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this@signin, MainActivity::class.java))
            finish()
        }
    }
}