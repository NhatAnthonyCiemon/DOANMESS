package com.example.doanmess.models

class GroupAdded {
    var name: String = ""
    var id : String = ""
    constructor() {}
    constructor(name: String,  id: String) {
        this.name = name
        this.id = id
    }
}