package com.example.doanmess

import android.app.Activity
import android.util.Log
import android.webkit.JavascriptInterface

class JavascriptInterface(val callActivity: Call) {

    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
        Log.e("JavascriptInterface", "onPeerConnected")
    }

}