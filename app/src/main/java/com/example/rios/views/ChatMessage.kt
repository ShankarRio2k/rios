package com.example.rios.views

import android.net.Uri

class ChatMessage(
    var message: String?,
    var currenttime: String?,
    var senderid: String?,
    var room: String?,// new property for the image
    var image: Uri?
) {


}
