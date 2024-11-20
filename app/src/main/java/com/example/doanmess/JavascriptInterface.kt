package com.example.doanmess

import android.util.Log
import android.webkit.JavascriptInterface

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