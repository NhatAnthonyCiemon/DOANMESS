package com.example.doanmess

class GroupAdd {
    var name: String=""
    var image: Int=0
    var added: Boolean= false
    constructor() {}
    constructor(name: String, image: Int, added: Boolean) {
        this.name = name
        this.image = image
        this.added = added
    }
}