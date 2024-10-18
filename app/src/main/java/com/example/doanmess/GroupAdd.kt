package com.example.doanmess

class GroupAdd {
    var id = ""
    var name: String=""
    var image: Int=0
    var added: Boolean= false
    constructor() {}
    constructor(id:String, name: String, image: Int, added: Boolean) {
        this.name = name
        this.image = image
        this.added = added
        this.id =id
    }
}