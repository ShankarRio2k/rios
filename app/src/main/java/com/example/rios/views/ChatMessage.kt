package com.example.rios.views

class ChatMessage {
    var message: String? = null
    var currenttime: String? = null
    var senderid: String? = null
    var room: String? = null
    var image: String? = null // new property for the image

    constructor() {}

    constructor(message: String?, currenttime: String?, senderid: String?, room: String?, image: String?) {
        this.message = message
        this.currenttime = currenttime
        this.senderid = senderid
        this.room = room
        this.image = image
    }
}
