package com.example.doanmess.models

class Friend {
    var id = ""
    var name: String=""
    var image: String =""
    var reqFriend: Boolean= false
    constructor() {}
    constructor(id:String, name: String, image: String, reqF: Boolean) {
        this.name = name
        this.image = image
        this.reqFriend = reqF
        this.id =id
    }
}