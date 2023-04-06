package com.example.rios.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rios.R
import com.example.rios.databinding.ActivityCreateaccountBinding
import com.example.rios.databinding.FragmentShotsBinding


class shots : Fragment() {
    private lateinit var binding: FragmentShotsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shots, container, false)
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
