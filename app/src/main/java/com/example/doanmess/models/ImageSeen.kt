package com.example.doanmess.models

class ImageSeen {
    var id = "";
    var image = "";
    var seen = false;
    constructor(id: String, image: String, seen: Boolean) {
        this.id = id;
        this.image = image;
        this.seen = seen;
    }
    fun setSeenId(seen: Boolean) {
        this.seen = seen;
    }
}