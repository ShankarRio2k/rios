package com.example.rios.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.rios.R
import com.example.rios.databinding.ActivityCreateaccountBinding
import com.example.rios.databinding.FragmentShotsBinding
import kotlinx.android.synthetic.main.fragment_shots.*

class shots : Fragment() {
    private lateinit var binding: FragmentShotsBinding
    val REQUEST_CODE = 11
    private val shotsViewmodel: Homeviewmodel by lazy {
        ViewModelProvider(this).get(Homeviewmodel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShotsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addvideo.setOnClickListener {
            // Create an instance of the AddShotFragment
            val addShotFragment = AddShotFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.shots_container, addShotFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }




    }


    companion object {
        fun newInstance(title: String): shots {
            val fragment = shots()
            val args = Bundle()
            args.putString("shots", title)
            fragment.arguments = args
            return fragment
        }
    }
}
