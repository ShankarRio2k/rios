package com.example.rios.model

import android.net.Uri

class ChatMessage(
    var message: String?,
    var currenttime: String?,
    var senderid: String?,
    var room: String?,// new property for the image
    var documentUrl: String?,
    var documentName: String?,
    var image: Uri?
) {


}
