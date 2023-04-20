package com.example.rios.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.rios.model.post

class Difi(private val oldList: List<post>, private val newList: List<post>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].postId == newList[newItemPosition].postId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldPost = oldList[oldItemPosition]
        val newPost = newList[newItemPosition]
        val payload = mutableMapOf<String, Any>()
//        if (oldPost.caption != newPost.caption) {
//            payload["caption"] = newPost.caption
//        }
//        // Add other fields that you want to check for changes
        return if (payload.isEmpty()) null else payload
    }
}
