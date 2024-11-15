package com.example.doanmess

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.UUID

class Call : AppCompatActivity() {
    var username = Firebase.auth.currentUser?.uid ?: ""
    var friendsUsername = ""

    var isPeerConnected = false

    var firebaseRef =  Firebase.database.getReference("calls")
    val webView by lazy { findViewById<WebView>(R.id.webView) }
    val callBtn by lazy { findViewById<Button>(R.id.callBtn) }
    val toggleAudioBtn by lazy { findViewById<ImageView>(R.id.toggleAudioBtn) }
    val toggleVideoBtn by lazy { findViewById<ImageView>(R.id.toggleVideoBtn) }
    val friendNameEdit by lazy { findViewById<EditText>(R.id.friendNameEdit) }
    val callLayout by lazy { findViewById<RelativeLayout>(R.id.callLayout) }
    val incomingCallTxt by lazy { findViewById<TextView>(R.id.incomingCallTxt) }
    val acceptBtn by lazy { findViewById<ImageView>(R.id.acceptBtn) }
    val rejectBtn by lazy { findViewById<ImageView>(R.id.rejectBtn) }
    val btnEndCall by lazy { findViewById<Button>(R.id.btnEndCall) }
    val inputLayout by lazy { findViewById<RelativeLayout>(R.id.inputLayout) }
    val callControlLayout by lazy { findViewById<LinearLayout>(R.id.callControlLayout) }
    var call = false
    var isAudio = true
    var isVideo = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)



        callBtn.setOnClickListener {
            friendsUsername = friendNameEdit.text.toString()
            sendCallRequest()
            call = true
        }

        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24 )
        }

        toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24 )
        }
        btnEndCall.setOnClickListener {
            callJavascriptFunction("javascript:endCall()")
            if(call){
                firebaseRef.child(friendsUsername).child("incoming").setValue("****endcall****")
                finish()
            }
            else{
                firebaseRef.child(username).child("isAvailable").setValue("endcall")
                firebaseRef.child(username).child("connId").setValue(null)
                firebaseRef.child(username).child("incoming").setValue(null)
                finish()
            }
        }
        setupWebView()
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You're not connected. Check your internet", Toast.LENGTH_LONG).show()
            return
        }

        firebaseRef.child(friendsUsername).child("incoming").setValue(username)
        firebaseRef.child(friendsUsername).child("isAvailable").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }
                else if(snapshot.value.toString() == "endcall") {
                    firebaseRef.child(friendsUsername).child("isAvailable").setValue(null)
                    finish()
                }

            }

        })

    }

    private fun listenForConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

        })
    }

    private fun setupWebView() {

        webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JavascriptInterface(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        webView.loadUrl(filePath)

        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    var uniqueId = ""

    private fun initializePeer() {

        uniqueId = getUniqueID()

        webView.evaluateJavascript("javascript:init(\"${uniqueId}\")") { result ->
            // Xử lý kết quả trả về
            if (result.isNotEmpty()) {
                Log.d("JavaScript Result", result)  // Lỗi có thể xuất hiện ở đây
            }
        }
        firebaseRef.child(username).child("incoming").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if(snapshot.value.toString() == "****endcall****") {
                        //xóa isAvailable và connId
                        firebaseRef.child(username).child("isAvailable").setValue(null)
                        firebaseRef.child(username).child("connId").setValue(null)
                        firebaseRef.child(username).child("incoming").setValue(null)
                        finish()

                        return
                    }
                    onCallRequest(snapshot.value as? String)
                }
            }
        })


    }

    private fun onCallRequest(caller: String?) {
        if (caller == null) return

        callLayout.visibility = View.VISIBLE
        incomingCallTxt.text = "$caller is calling..."

        acceptBtn.setOnClickListener {
            firebaseRef.child(username).child("connId").setValue(uniqueId)
            firebaseRef.child(username).child("isAvailable").setValue(true)

            callLayout.visibility = View.GONE
            switchToControls()
        }

        rejectBtn.setOnClickListener {
            firebaseRef.child(username).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
        }

    }

    private fun switchToControls() {
        inputLayout.visibility = View.GONE
        callControlLayout.visibility = View.VISIBLE
    }


    private fun getUniqueID(): String {
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString: String) {
        webView.post { webView.evaluateJavascript(functionString, null) }
    }


    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        firebaseRef.child(username).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }

}