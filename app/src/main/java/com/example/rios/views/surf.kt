package com.example.rios.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rios.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class surf : Fragment() {

    private lateinit var postAdapter: postAdapter
    private val posts = listOf<post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_surf, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.postRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = postAdapter(requireContext(), posts)
        recyclerView.adapter = postAdapter
        return view


    }

    companion object {
        // ...
    }
}
