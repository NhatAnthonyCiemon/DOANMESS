package com.example.doanmess.helper

import android.util.Log
import android.webkit.JavascriptInterface
import com.example.doanmess.activities.Call
import com.example.doanmess.activities.CallGroup

class JavascriptInterface(val callActivity: Call) {

    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
    @JavascriptInterface
    public fun onCallReady(callId: String) {
        Log.e("NOOOOOOOOOOOOOOOOOOOO", callId)
    }
    @JavascriptInterface
    public fun onCallError(){
        Log.e("NOOOOOOOOOOOOOOOOOOOO", "onCallError")
    }
}
class JavascriptInterfaceVer(val callActivity: CallGroup) {

    @JavascriptInterface
    public fun onPeerConnected() {

    }
    @JavascriptInterface
    public fun onCallReady(callId: String) {
        Log.e("NOOOOOOOOOOOOOOOOOOOO", callId)
    }
    @JavascriptInterface
    public fun onCallError(){
        Log.e("NOOOOOOOOOOOOOOOOOOOO", "onCallError")
    }
}