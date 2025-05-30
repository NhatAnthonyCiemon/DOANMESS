package com.example.doanmess.helper

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.provider.Settings
import com.example.doanmess.R
import com.example.doanmess.activities.Call
import com.example.doanmess.activities.CallGroup
import com.example.doanmess.activities.Home
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        var title = remoteMessage.notification?.title ?: "Default Title"
        var message = remoteMessage.notification?.body ?: "Default Message"
        val uid = remoteMessage.data["uid"] ?: "Default UID"

        val type = remoteMessage.data["type"] ?: "Default Type"
        val auth = Firebase.auth
        val user = auth.currentUser
        if(user?.uid != uid) {
            // Gọi hàm hiển thị thông báo
            if(type =="group"){
                val id_group = remoteMessage.data["idGroup"] ?: "Default ID Group"
                Firebase.firestore.collection("groups").document(id_group).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotification(this, title, message,url,id_group)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotification(this, title, message,"",id_group)
                    }
            }
            else if(type =="personal"){
                Firebase.firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotification(this, title, message,url,uid)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotification(this, title, message,"",uid)
                    }
            }
            else if(type =="call"){
                title = remoteMessage.data["title"] ?: "Default Title"
                message = remoteMessage.data["body"] ?: "Default Message"
                Firebase.firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotificationCall(this, title, message,url,uid,true)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotificationCall(this, title, message,"",uid,true)
                    }
            }
            else if (type =="callvoice"){
                title = remoteMessage.data["title"] ?: "Default Title"
                message = remoteMessage.data["body"] ?: "Default Message"
                Firebase.firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotificationCall(this, title, message,url,uid,false)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotificationCall(this, title, message,"",uid,false)
                    }
            }
            else if(type =="callgroup"){
                title = remoteMessage.data["title"] ?: "Default Title"
                message = remoteMessage.data["body"] ?: "Default Message"
                val id_group = remoteMessage.data["idGroup"] ?: "Default ID Group"
                Firebase.firestore.collection("groups").document(id_group).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotificationCallGroup(this, title, message,url,id_group,true)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotificationCallGroup(this, title, message,"",id_group,true)
                    }
            }
            else if(type =="callvoicegroup"){
                title = remoteMessage.data["title"] ?: "Default Title"
                message = remoteMessage.data["body"] ?: "Default Message"
                val id_group = remoteMessage.data["idGroup"] ?: "Default ID Group"
                Firebase.firestore.collection("groups").document(id_group).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val url = document.get("Avatar") as? String ?: ""
                            showHighPriorityNotificationCallGroup(this, title, message,url,id_group,false)
                        }
                    }
                    .addOnFailureListener { e ->
                        showHighPriorityNotificationCallGroup(this, title, message,"",id_group,false)
                    }
            }
        }
    }
    override fun onNewToken(token: String) {
        Log.e("HHHHHHHHHHHHHHHHHHHHHHHH", "Refreshed token: $token")
        //sendRegistrationToServer(token)

        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val docRef = Firebase.firestore.collection("devices").document(androidId)
        val auth = Firebase.auth
        val user_current = auth.currentUser
        var User_id = ""
        if (user_current != null) {
            User_id = user_current.uid
        }
        val user = hashMapOf(
            "Token" to token,
            "User_id" to User_id
        ) as Map<String, Any?>
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    docRef
                        .update("Token", token)
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                }
                else{
                    docRef
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
                }
            }
        docRef
            .set(user, SetOptions.merge())
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }

    }

    private fun showHighPriorityNotification(context: Context, title: String, message: String,url : String,id : String) {

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val channelId = "MESSAGE"
            var bitmapAvatar: Bitmap? = null
            if(url != ""){
                GlobalScope.launch {
                    bitmapAvatar = loadBitmapFromUrl(url,id)
                    val intent = Intent(context, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    val pendingIntent: PendingIntent =
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.icon_messeger)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmapAvatar)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                    with(NotificationManagerCompat.from(context)) {
                        notify(getTimeCurrent(), builder.build())
                    }
                }
            }
            else{
                val intent = Intent(context, Home::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.icon_messeger)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

                with(NotificationManagerCompat.from(context)) {
                    notify(getTimeCurrent(), builder.build())
                }
            }

        }
    }
    private fun showHighPriorityNotificationCall(context: Context, title: String, message: String, url: String, id: String,isVideo: Boolean) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val channelId = "MESSAGE"
            var bitmapAvatar: Bitmap? = null
            if (url != "") {
                GlobalScope.launch {
                    bitmapAvatar = loadBitmapFromUrl(url, id)
                    var intent: Intent? = null
                    if(isVideo){
                        intent = Intent(context, Call::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("friendId", id)
                            putExtra("call", false)
                            putExtra("isVideoCall", true)
                        }
                    }
                    else{
                        intent = Intent(context, Call::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("friendId", id)
                            putExtra("call", false)
                            putExtra("isVideoCall", false)
                        }
                    }

                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.icon_messeger)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmapAvatar)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                    with(NotificationManagerCompat.from(context)) {
                        notify(getTimeCurrent(), builder.build())
                    }
                }
            } else {
                var intent: Intent? = null
                if(isVideo){
                    intent = Intent(context, Call::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("friendId", id)
                        putExtra("call", false)
                        putExtra("isVideoCall", true)
                    }
                }
                else{
                    intent = Intent(context, Call::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("friendId", id)
                        putExtra("call", false)
                        putExtra("isVideoCall", false)
                    }
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.icon_messeger)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

                with(NotificationManagerCompat.from(context)) {
                    notify(getTimeCurrent(), builder.build())
                }
            }
        }
    }

    private fun showHighPriorityNotificationCallGroup(context: Context, title: String, message: String, url: String, id: String,isVideo: Boolean){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val channelId = "MESSAGE"
            var bitmapAvatar: Bitmap? = null
            if (url != "") {
                GlobalScope.launch {
                    bitmapAvatar = loadBitmapFromUrl(url, id)
                    var intent: Intent? = null
                    if(isVideo){
                        intent = Intent(context, CallGroup::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("groupId", id)
                            putExtra("call", false)
                            putExtra("isVideoCall", true)
                        }
                    }
                    else{
                        intent = Intent(context, CallGroup::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("groupId", id)
                            putExtra("call", false)
                            putExtra("isVideoCall", false)
                        }
                    }

                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.icon_messeger)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmapAvatar)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                    with(NotificationManagerCompat.from(context)) {
                        notify(getTimeCurrent(), builder.build())
                    }
                }
            } else {
                var intent: Intent? = null
                if(isVideo){
                    intent = Intent(context, CallGroup::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("groupId", id)
                        putExtra("call", false)
                        putExtra("isVideoCall", true)
                    }
                }
                else{
                    intent = Intent(context, CallGroup::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("groupId", id)
                        putExtra("call", false)
                        putExtra("isVideoCall", false)
                    }
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.icon_messeger)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

                with(NotificationManagerCompat.from(context)) {
                    notify(getTimeCurrent(), builder.build())
                }
            }
        }
    }

    fun getTimeCurrent(): Int {
        return System.currentTimeMillis().toInt()
    }


    suspend fun loadBitmapFromUrl(url: String, id: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            var bitmap: Bitmap? = null
            try {
                val fileName = "$id.jpg"
                val file = File(filesDir, fileName)
                if(file.exists()&& !isFileBeingWritten(file)){
                    bitmap = BitmapFactory.decodeFile(file.absolutePath)
                }
                else {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(input)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bitmap
        }
    }
    fun isFileBeingWritten(file: File): Boolean {
        return try {
            // Mở tệp để kiểm tra xem nó có thể được đọc/ghi không
            val inputStream = FileInputStream(file)
            inputStream.close() // Đóng ngay lập tức để không giữ khóa tệp
            false // Tệp không bị ghi
        } catch (e: IOException) {
            true // Nếu có IOException, tệp có thể đang bị ghi
        }
    }
}