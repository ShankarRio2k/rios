package com.example.rios.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.rios.R
import com.example.rios.databinding.ActivityCreateaccountBinding
import com.example.rios.databinding.ActivitySigninBinding
import com.example.rios.databinding.FragmentTalksBinding
import com.example.rios.utils.Extensions.toast
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.example.rios.utils.FirebaseUtils.firebaseUser
import com.google.firebase.auth.FirebaseUser

class Createaccount : AppCompatActivity() {
    lateinit var userEmail: String
    lateinit var userPassword: String
    private lateinit var binding: ActivityCreateaccountBinding
    lateinit var createAccountInputsArray: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateaccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createAccountInputsArray = arrayOf(binding.etEmail, binding.etPassword, binding.etConfirmPassword)

       binding.btnCreateAccount.setOnClickListener {
            signIn()
        }

        binding.btnSignIn2.setOnClickListener {
            startActivity(Intent(this, signin::class.java))
            toast("please sign into your account")
            finish()
        }
    }

    /* check if there's a signed-in user*/

    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("welcome back")
        }
    }

    private fun notEmpty(): Boolean = binding.etEmail.text.toString().trim().isNotEmpty() &&
            binding.etPassword.text.toString().trim().isNotEmpty() &&
            binding.etConfirmPassword.text.toString().trim().isNotEmpty()

    private fun identicalPassword(): Boolean {
        var identical = false
        if (notEmpty() &&
            binding.etPassword.text.toString().trim() == binding.etConfirmPassword.text.toString().trim()
        ) {
            identical = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        } else {
            toast("passwords are not matching !")
        }
        return identical
    }

    private fun signIn() {
        if (identicalPassword()) {
            // identicalPassword() returns true only  when inputs are not empty and passwords are identical
            userEmail = binding.etEmail.text.toString().trim()
            userPassword = binding.etPassword.text.toString().trim()

            /*create a user*/
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("created account successfully !")
                        sendEmailVerification()
                        startActivity(Intent(this, SetupProf::class.java))
                        finish()
                    } else {
                        toast("failed to Authenticate !")
                    }
                }
        }
    }

    /* send verification email to the new user. This will only
    *  work if the firebase user is not null.
    */

    private fun sendEmailVerification() {
        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("email sent to $userEmail")
                }
            }
        }
    }
}
