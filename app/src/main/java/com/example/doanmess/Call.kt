package com.example.doanmess

import HandleOnlineActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

class Call  : HandleOnlineActivity() {
    var userId = Firebase.auth.currentUser?.uid ?: ""
    var friendId = ""
    var uniqueId = ""
    var isPeerConnected = false

    var firebaseRef =  Firebase.database.getReference("calls")
    val webView by lazy { findViewById<WebView>(R.id.webView) }
    val acceptBtnCard by lazy { findViewById<CardView>(R.id.acceptBtnCard) }
    val rejectBtnCard by lazy { findViewById<CardView>(R.id.rejectBtnCard) }
    val endCallBtnCard by lazy { findViewById<CardView>(R.id.endCallBtnCard) }
    val avatarCallCard by lazy { findViewById<CardView>(R.id.avatarCallCard) }
    val timeTxt by lazy { findViewById<TextView>(R.id.timeTxt) }
    val avatarCall by lazy { findViewById<ImageView>(R.id.avatarCall) }
    val voiceBackgroundImg by lazy { findViewById<ImageView>(R.id.voiceBackgroundImg) }
    val toggleAudioBtn by lazy { findViewById<ImageView>(R.id.toggleAudioBtn) }
    val toggleVideoBtn by lazy { findViewById<ImageView>(R.id.toggleVideoBtn) }
    val NameOtherTxt by lazy { findViewById<TextView>(R.id.NameOtherTxt) }
    val callControlLayout by lazy { findViewById<LinearLayout>(R.id.callControlLayout) }
    val loadingBar by lazy { findViewById<ProgressBar>(R.id.LoadingBar) }
    var call = false
    var isAudio = true
    var isVideo = true
    var isVideoCall = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        friendId = intent.getStringExtra("friendId") ?: ""
        call = intent.getBooleanExtra("call", false)
        isVideoCall = intent.getBooleanExtra("isVideoCall", false)
        if(!isVideoCall){
            toggleVideoBtn.visibility = View.GONE
            voiceBackgroundImg.visibility = View.VISIBLE
           firebaseRef=  Firebase.database.getReference("callvoices")
        }
        uniqueId = getUniqueID()
        setupWebView()
        if(call){
            acceptBtnCard.visibility = View.GONE
            rejectBtnCard.visibility = View.GONE
            endCallBtnCard.visibility = View.VISIBLE
            avatarCallCard.visibility = View.VISIBLE
            Firebase.firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                (this@Call as? LifecycleOwner)?.lifecycleScope?.launch {
                    try {
                        val ImageLoader = ImageLoader(this@Call)
                        val path = ImageLoader.checkFile(avatar!!, friendId)
                        if(path != avatar && File(path).exists()) {
                            Picasso.get().load(File(path)).into(avatarCall)
                        }
                        else {
                            Picasso.get().load(avatar).into(avatarCall)
                        }
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            Firebase.firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                val name = document.getString("Name")
                NameOtherTxt.text = name
            }
            firebaseRef.child(friendId).get().addOnSuccessListener {
                if(it.exists()){
                    finish()
                }
                else{
                    if(isVideoCall) {
                        Firebase.database.getReference("callvoices").child(friendId).get()
                            .addOnSuccessListener {
                                if (it.exists()) {
                                    finish()
                                } else {
                                    onStartCall()
                                }
                            }
                    }
                    else{
                        Firebase.database.getReference("calls").child(friendId).get()
                            .addOnSuccessListener {
                                if (it.exists()) {
                                    finish()
                                } else {
                                    onStartCall()
                                }
                            }
                    }

                }
            }

        }
        else{
            callControlLayout.visibility = View.GONE
            endCallBtnCard.visibility = View.GONE
            avatarCallCard.visibility = View.VISIBLE

            Firebase.firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                (this@Call as? LifecycleOwner)?.lifecycleScope?.launch {
                    try {
                        val ImageLoader = ImageLoader(this@Call)
                        val path = ImageLoader.checkFile(avatar!!, friendId)
                        if(path != avatar && File(path).exists()) {
                            Picasso.get().load(File(path)).into(avatarCall)
                        }
                        else {
                            Picasso.get().load(avatar).into(avatarCall)
                        }
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            Firebase.firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                val name = document.getString("Name")
                NameOtherTxt.text = name
            }
            firebaseRef.child(userId).addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    //kiểm tra snapshot có tồn tại không
                    if (!snapshot.exists()) {
                        finish()
                    }
                }

            })
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
        endCallBtnCard.setOnClickListener {
            callJavascriptFunction("javascript:endCall()")
            if(call){
                firebaseRef.child(friendId).child("incoming").setValue("****endcall****")
                finish()
            }
            else{
                firebaseRef.child(userId).child("isAvailable").setValue("endcall")
                firebaseRef.child(userId).child("connId").setValue(null)
                firebaseRef.child(userId).child("incoming").setValue(null)

                finish()
            }
        }

    }


    private fun onStartCall(){
        firebaseRef.child(friendId).child("incoming").setValue(userId)
        firebaseRef.child(friendId).child("idIncoming").setValue(uniqueId)

        firebaseRef.child(friendId).child("isAvailable").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }
                else if(snapshot.value.toString() == "endcall") {
                    firebaseRef.child(friendId).child("isAvailable").setValue(null)
                    finish()
                }

            }

        })
        firebaseRef.child(friendId).child("incoming").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if(snapshot.value.toString() == "****reject****") {
                        firebaseRef.child(friendId).child("incoming").setValue(null)
                        finish()

                        return
                    }
                }
            }

        })
    }
    private fun listenForConnId() {
        firebaseRef.child(friendId).child("connId").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                webView.visibility = View.VISIBLE
                if(isVideoCall){
                    avatarCallCard.visibility = View.GONE
                    NameOtherTxt.visibility = View.GONE
                    timeTxt.visibility = View.GONE
                }
                else{
                    timeTxt.visibility = View.VISIBLE
                    CallTimer(timeTxt).start()
                }
                switchToControls()
                Log.e("ConnIdddddddddddd", snapshot.value.toString())
                callJavascriptFunction("javascript:startCall(\"${snapshot.value.toString()}\")")
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
        var filePath = "file:android_asset/call.html"//"https://call-bkxs.vercel.app"
        if(!isVideoCall){
            filePath = "file:android_asset/callvoice.html"
        }
        webView.loadUrl(filePath)

        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }



    private fun initializePeer() {


        webView.evaluateJavascript("javascript:init(\"${uniqueId}\")") { result ->
            // Xử lý kết quả trả về
            if (result.isNotEmpty()) {
                Log.d("JavaScript Result", result)  // Lỗi có thể xuất hiện ở đây
            }
        }
        firebaseRef.child(userId).child("incoming").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if(snapshot.value.toString() == "****endcall****") {
                        //xóa isAvailable và connId
                        firebaseRef.child(userId).child("isAvailable").setValue(null)
                        firebaseRef.child(userId).child("connId").setValue(null)
                        firebaseRef.child(userId).child("incoming").setValue(null)
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
        acceptBtnCard.setOnClickListener {
            loadingBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                webView.visibility = View.VISIBLE
                if(isVideoCall){
                    avatarCallCard.visibility = View.GONE
                    NameOtherTxt.visibility = View.GONE
                }
                else{
                    timeTxt.visibility = View.VISIBLE
                    CallTimer(timeTxt).start()
                }
                firebaseRef.child(userId).child("connId").setValue(uniqueId)
                firebaseRef.child(userId).child("isAvailable").setValue(true)
                acceptBtnCard.visibility = View.GONE
                rejectBtnCard.visibility = View.GONE
                endCallBtnCard.visibility = View.VISIBLE
                loadingBar.visibility = View.GONE
                switchToControls()
            }, 5000) // 5000 milliseconds = 5 seconds
        }

        rejectBtnCard.setOnClickListener {
            firebaseRef.child(userId).child("incoming").setValue("****reject****")
            finish()
        }

    }

    private fun switchToControls() {
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
        firebaseRef.child(userId).setValue(null)
        firebaseRef.child(friendId).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }

}