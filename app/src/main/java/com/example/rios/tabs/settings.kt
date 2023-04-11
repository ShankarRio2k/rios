package com.example.rios.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.rios.R
import com.example.rios.databinding.FragmentSettingsBinding
import com.example.rios.databinding.FragmentSurfBinding
import com.example.rios.utils.FirebaseUtils.firebaseAuth
import com.example.rios.utils.SharedPrefernceHelper
import com.example.rios.views.Homeviewmodel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class settings : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val settingViewModel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        val userId = firebaseAuth.currentUser?.uid.toString()

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val sharedPreferencesHelper = SharedPrefernceHelper(requireContext())
        val (username, bio, profileImageUrl) = sharedPreferencesHelper.getUserDetails()

        if (username != null && bio != null && profileImageUrl != null) {
            Glide.with(this).load(profileImageUrl).into(binding.userprof)
            binding.username.text = username
            binding.bio.text = bio
        } else {
            val userRef = firebaseFirestore.collection("profiles").document(userId)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val username = documentSnapshot.getString("name")
                val bio = documentSnapshot.getString("bio")
                val profileImageUrl = documentSnapshot.getString("profilePic")

                Glide.with(this).load(profileImageUrl).into(binding.userprof)
                binding.username.text = username
                binding.bio.text = bio

                sharedPreferencesHelper.saveUserDetails(username!!, bio!!, profileImageUrl!!)
            }

        }

    }

    companion object {
        fun newInstance(title: String): settings {
            val fragment = settings()
            val args = Bundle()
            args.putString("settings", title)
            fragment.arguments = args
            return fragment
        }
    }
}
