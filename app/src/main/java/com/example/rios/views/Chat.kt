package com.example.rios.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rios.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_chat.*

class Chat : Fragment() {

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adapter = ChatAdapter(currentUserId)

        chatRecyclerView.adapter = adapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        chatSendButton.setOnClickListener {
            val messagetxt = chatInputEditText.text.toString().trim()
            val message = chatInputEditText.text.toString().trim()
            val currenttime = Timestamp.now()
            val senderid = currentUserId
            if (messagetxt.isNotEmpty()) {
                // Send the message
                adapter.addMessage(message, currenttime, senderid )
                chatInputEditText.text?.clear()
            }
        }
    }
}
