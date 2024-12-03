package com.example.doanmess

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.google.firebase.database.*
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID

class CallGroup : AppCompatActivity() {
    private val userId = Firebase.auth.currentUser?.uid ?: ""
    private var groupId = ""
    private var uniqueId = ""
    private var isPeerConnected = false
    private val mapPeer = mutableMapOf<String, String>()
    private lateinit var firebaseRef: DatabaseReference
    private val webView by lazy { findViewById<WebView>(R.id.webView) }
    private val acceptBtnCard by lazy { findViewById<CardView>(R.id.acceptBtnCard) }
    private val rejectBtnCard by lazy { findViewById<CardView>(R.id.rejectBtnCard) }
    private val endCallBtnCard by lazy { findViewById<CardView>(R.id.endCallBtnCard) }
    private val avatarCallCard by lazy { findViewById<CardView>(R.id.avatarCallCard) }
    private val timeTxt by lazy { findViewById<TextView>(R.id.timeTxt) }
    private val avatarCall by lazy { findViewById<ImageView>(R.id.avatarCall) }
    private val voiceBackgroundImg by lazy { findViewById<ImageView>(R.id.voiceBackgroundImg) }
    val loadingBar by lazy { findViewById<ProgressBar>(R.id.LoadingBar) }
    private val toggleAudioBtn by lazy { findViewById<ImageView>(R.id.toggleAudioBtn) }
    private val toggleVideoBtn by lazy { findViewById<ImageView>(R.id.toggleVideoBtn) }
    private val nameOtherTxt by lazy { findViewById<TextView>(R.id.NameOtherTxt) }
    private val callControlLayout by lazy { findViewById<LinearLayout>(R.id.callControlLayout) }
    private var call = false
    private var called = false
    private var isAudio = true
    private var isVideo = true
    private var isVideoCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_call_group)
        setupWindowInsets()
        initializeVariables()
        setupWebView()
        if (call) setupCallerUI() else setupReceiverUI()
        setupButtonListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeVariables() {
        groupId = intent.getStringExtra("groupId") ?: ""
        call = intent.getBooleanExtra("call", false)
        isVideoCall = intent.getBooleanExtra("isVideoCall", false)
        uniqueId = UUID.randomUUID().toString()
        firebaseRef = Firebase.database.getReference(if (isVideoCall) "callGroups" else "callGroupsvoices")
    }

    private fun setupWebView() {
        webView.apply {
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.grant(request.resources)
                }
            }
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(JavascriptInterfaceVer(this@CallGroup), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    initializePeer()
                }
            }
            loadUrl(if (isVideoCall) "https://call-bkxs.vercel.app" else "https://call-voice-group.vercel.app")
        }
    }

    private fun setupCallerUI() {
        acceptBtnCard.visibility = View.GONE
        rejectBtnCard.visibility = View.GONE
        endCallBtnCard.visibility = View.VISIBLE
        avatarCallCard.visibility = View.VISIBLE
        loadGroupInfo()
        firebaseRef.child(groupId).child(userId).setValue(uniqueId)
        firebaseRef.child(groupId).addChildEventListener(childEventListener)
    }

    private fun setupReceiverUI() {
        callControlLayout.visibility = View.GONE
        endCallBtnCard.visibility = View.GONE
        avatarCallCard.visibility = View.VISIBLE
        loadGroupInfo()
        firebaseRef.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) finish()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadGroupInfo() {
        Firebase.firestore.collection("groups").document(groupId).get().addOnSuccessListener { document ->
            val avatar = document.getString("Avatar")
            val name = document.getString("Name")
            nameOtherTxt.text = name
            (this@CallGroup as? LifecycleOwner)?.lifecycleScope?.launch {
                try {
                    val imageLoader = ImageLoader(this@CallGroup)
                    val path = imageLoader.checkFile(avatar!!, groupId)
                    if (path != avatar && File(path).exists()) {
                        Picasso.get().load(File(path)).into(avatarCall)
                    } else {
                        Picasso.get().load(avatar).into(avatarCall)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupButtonListeners() {
        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"$isAudio\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }

        toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"$isVideo\")")
            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }

        endCallBtnCard.setOnClickListener {
            firebaseRef.child(groupId).child(userId).setValue(null)
            finish()
        }

        acceptBtnCard.setOnClickListener {
            loadingBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                called = true
                webView.visibility = View.VISIBLE
                if (isVideoCall) {
                    avatarCallCard.visibility = View.GONE
                    nameOtherTxt.visibility = View.GONE
                    toggleVideoBtn.visibility = View.VISIBLE
                } else {
                    avatarCallCard.visibility = View.GONE
                    nameOtherTxt.visibility = View.GONE
                    toggleVideoBtn.visibility = View.GONE
                }
                firebaseRef.child(groupId).child(userId).setValue(uniqueId)
                acceptBtnCard.visibility = View.GONE
                rejectBtnCard.visibility = View.GONE
                endCallBtnCard.visibility = View.VISIBLE
                switchToControls()
                firebaseRef.child(groupId).addChildEventListener(childEventListener)
                loadingBar.visibility = View.GONE

            }, 5000) // 5000 milliseconds = 5 seconds
        }

        rejectBtnCard.setOnClickListener {
            finish()
        }
    }

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val key = snapshot.key.toString()
            val value = snapshot.value.toString()
            if (key != userId && mapPeer[key] == null) {
                mapPeer[key] = value
                listenForConnId(value, key)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val key = snapshot.key.toString()
            val peerId = mapPeer[key]
            if (peerId != null) {
                callJavascriptFunction("javascript:deleteRemoteVideo(\"$peerId\")")
                mapPeer.remove(key)
            }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error: ${error.message}")
        }
    }

    private fun listenForConnId(idOther: String,idFibase: String) {
        if (idOther == uniqueId) return
        webView.visibility = View.VISIBLE
        if (isVideoCall) {
            avatarCallCard.visibility = View.GONE
            nameOtherTxt.visibility = View.GONE
            timeTxt.visibility = View.GONE
        } else {
            avatarCallCard.visibility = View.GONE
            nameOtherTxt.visibility = View.GONE
            timeTxt.visibility = View.GONE
        }
        switchToControls()
        if(!isVideoCall){
            Firebase.firestore.collection("users").document(idFibase).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                val name = document.getString("Name")
                callJavascriptFunction("javascript:startGroupCall(\"$idOther\",\"$avatar\",\"$name\")")
            }
        }
        else callJavascriptFunction("javascript:startGroupCall(\"$idOther\")")
    }

    private fun initializePeer() {
        if(isVideoCall) {
            webView.evaluateJavascript("javascript:init(\"$uniqueId\")") { result ->
                if (result.isNotEmpty()) {
                    Log.d("JavaScript Result", result)
                }
            }
        }
        else {
            Firebase.firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val avatar = document.getString("Avatar")
                val name = document.getString("Name")
                webView.evaluateJavascript("javascript:init(\"$uniqueId\",\"$avatar\",\"$name\")") { result ->
                    if (result.isNotEmpty()) {
                        Log.d("JavaScript Result", result)
                    }
                }
            }
        }
        if (!call) onCallRequest()
    }

    private fun switchToControls() {
        callControlLayout.visibility = View.VISIBLE
    }

    private fun callJavascriptFunction(functionString: String) {
        webView.post { webView.evaluateJavascript(functionString, null) }
    }
    private fun onCallRequest() {
        acceptBtnCard.setOnClickListener {
            loadingBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                called = true
                webView.visibility = View.VISIBLE
                if (isVideoCall) {
                    avatarCallCard.visibility = View.GONE
                    nameOtherTxt.visibility = View.GONE
                } else {
                    avatarCallCard.visibility = View.GONE
                    nameOtherTxt.visibility = View.GONE
                    toggleVideoBtn.visibility = View.GONE
                }
                firebaseRef.child(groupId).child(userId).setValue(uniqueId)
                acceptBtnCard.visibility = View.GONE
                rejectBtnCard.visibility = View.GONE
                endCallBtnCard.visibility = View.VISIBLE
                switchToControls()
                firebaseRef.child(groupId).addChildEventListener(childEventListener)
                loadingBar.visibility = View.GONE
            }, 5000) // 5000 milliseconds = 5 seconds

        }

        rejectBtnCard.setOnClickListener {
            finish()
        }
    }
    override fun onDestroy() {
        firebaseRef.child(groupId).child(userId).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }
}