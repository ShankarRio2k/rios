package com.example.rios.views

class ChatMessage {
    var message: String? = null
    var currenttime: String? = null
    var senderid: String? = null
    var room: String? = null

    constructor() {}

    constructor(message: String?, currenttime: String?, senderid: String?, room: String?) {
        this.message = message
        this.currenttime = currenttime
        this.senderid = senderid
        this.room = room
    }
}
