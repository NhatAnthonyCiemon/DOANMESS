package com.example.doanmess

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

class CallGroup : AppCompatActivity() {
    var userId = Firebase.auth.currentUser?.uid ?: ""
    var groupId = ""
    var uniqueId = ""
    var isPeerConnected = false
    var map_peer = mutableMapOf<String, String>()
    var firebaseRef: DatabaseReference =   Firebase.database.getReference("callGroups")
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
    var call = false
    var called = false
    var isAudio = true
    var isVideo = true
    var isVideoCall = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        groupId = intent.getStringExtra("groupId") ?: ""
        call = intent.getBooleanExtra("call", false)
        isVideoCall = intent.getBooleanExtra("isVideoCall", false)
        if(!isVideoCall){
            toggleVideoBtn.visibility = View.GONE
            voiceBackgroundImg.visibility = View.VISIBLE
            firebaseRef=  Firebase.database.getReference("callGroupsvoices")
        }
        uniqueId = getUniqueID()
        setupWebView()
        if(call){
            acceptBtnCard.visibility = View.GONE
            rejectBtnCard.visibility = View.GONE
            endCallBtnCard.visibility = View.VISIBLE
            avatarCallCard.visibility = View.VISIBLE
            Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                (this@CallGroup as? LifecycleOwner)?.lifecycleScope?.launch {
                    try {
                        val ImageLoader = ImageLoader(this@CallGroup)
                        val path = ImageLoader.checkFile(avatar!!, groupId)
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
            Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { document ->
                val name = document.getString("Name")
                NameOtherTxt.text = name
            }
            firebaseRef.child(groupId).child(userId).setValue(uniqueId)
            firebaseRef.child(groupId).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    // Một phần tử mới được thêm vào
                    val key = snapshot.key.toString() // Key của trường con (VD: ENEHBGCHHNbSvr6628p1L6hh8wS2)
                    val value = snapshot.value.toString() // Giá trị của trường con

                    if (key != userId && map_peer[key] == null) {
                        // Thêm vào map và xử lý logic kết nối
                        map_peer[key] = value
                        listenForConnId(value)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Một trường con thay đổi giá trị
                    val key = snapshot.key.toString()
                    val value = snapshot.value.toString()

                    if (map_peer[key] != value) {
                        map_peer[key] = value
                        Log.d("Firebase", "Child changed: Key = $key, New Value = $value")
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Một trường con bị xóa
                    val key = snapshot.key.toString() // Key của trường con bị xóa
                    val peerId = map_peer[key] // Lấy giá trị tương ứng từ map_peer

                    if (peerId != null) {
                        // Thực hiện logic khi xóa, ví dụ: loại bỏ video
                        callJavascriptFunction("javascript:deleteRemoteVideo(\"${peerId}\")")
                        map_peer.remove(key)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Không cần xử lý nếu không có yêu cầu cụ thể
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error: ${error.message}")
                }
            })
        }
        else{
            callControlLayout.visibility = View.GONE
            endCallBtnCard.visibility = View.GONE
            avatarCallCard.visibility = View.VISIBLE

            Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                (this@CallGroup as? LifecycleOwner)?.lifecycleScope?.launch {
                    try {
                        val ImageLoader = ImageLoader(this@CallGroup)
                        val path = ImageLoader.checkFile(avatar!!, groupId)
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
            Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { document ->
                val name = document.getString("Name")
                NameOtherTxt.text = name
            }

            firebaseRef.child(groupId).addValueEventListener(object: ValueEventListener {
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
            firebaseRef.child(groupId).child(userId).setValue(null)
            finish()
        }

    }



    private fun listenForConnId(id_other: String) {
        if(id_other == uniqueId) return
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
        callJavascriptFunction("javascript:startGroupCall(\"${id_other}\")")
    }

    private fun setupWebView() {

        webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JavascriptInterfaceVer(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        var filePath = "file:android_asset/callgroup.html"//"https://call-bkxs.vercel.app"
        if(!isVideoCall){
            filePath = "file:android_asset/callgroupvoice.html"
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
        firebaseRef.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { childSnapshot ->
                        val key = childSnapshot.key.toString() // Key của trường con
                        val value = childSnapshot.value.toString() // Giá trị của trường con

                        // Nếu phần tử chưa được xử lý và không trùng với uniqueId
                        if (called && value != uniqueId && map_peer[key] == null) {
                            map_peer[key] = value
                            listenForConnId(value)
                        } else if (!called) {
                            // Gọi logic xử lý yêu cầu cuộc gọi
                            onCallRequest(value)
                        }
                    }
                } else {
                    Log.d("Firebase", "Snapshot does not exist for groupId: $groupId")
                }
            }
        })


    }

    private fun onCallRequest(caller: String?) {
        if (caller == null) return
        acceptBtnCard.setOnClickListener {
            called = true
            webView.visibility = View.VISIBLE
            if(isVideoCall){
                avatarCallCard.visibility = View.GONE
                NameOtherTxt.visibility = View.GONE
            }
            else{
                timeTxt.visibility = View.VISIBLE
                CallTimer(timeTxt).start()
            }
            firebaseRef.child(groupId).child(userId).setValue(uniqueId)

            acceptBtnCard.visibility = View.GONE
            rejectBtnCard.visibility = View.GONE
            endCallBtnCard.visibility = View.VISIBLE
            switchToControls()
        }

        rejectBtnCard.setOnClickListener {
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



    override fun onDestroy() {
        firebaseRef.child(groupId).child(userId).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }
}