package com.example.doanmess

class Friend {
    var id = ""
    var name: String=""
    var image: Int=0
    var reqFriend: Boolean= false
    constructor() {}
    constructor(id:String, name: String, image: Int, reqF: Boolean) {
        this.name = name
        this.image = image
        this.reqFriend = reqF
        this.id =id
    }
}