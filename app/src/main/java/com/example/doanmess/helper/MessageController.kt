package com.example.doanmess.helper

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL


class MessageController {
    fun newCreateGroup(id: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã tạo nhóm mới"
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendgroup/$id/$uid/$content")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }

    }
    fun newFriendRequest(uid_other: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã gửi lời mời kết bạn"
                    val title = "Lời mời kết bạn mới"
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendperson/$uid_other/$uid/$content/$title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun newFriendAccpet(uid_other: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã chấp nhận lời mời kết bạn"
                    val title = "Lời mời kết bạn đã được chấp nhận"
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendperson/$uid_other/$uid/$content/$title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }

    fun newMessageFriend(uid_other: String, uid: String, content: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val afterContent = name +": "+ content
                    val title = name +" đã gửi tin nhắn"
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendperson/$uid_other/$uid/$afterContent/$title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun newMessageGroup(idGroup: String, uid: String, content: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val afterContent = name +": "+ content
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendgroup/$idGroup/$uid/$afterContent")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun callVideoFriend(uid_other: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã gọi video"
                    val title = "Cuộc gọi video mới"
                    sendAPIRequest("https://android-backend-3day.vercel.app/callperson/$uid_other/$uid/$content/$title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }

    fun callVideoGroup(idGroup: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã gọi video"
                    sendAPIRequest("https://android-backend-3day.vercel.app/callgroup/$idGroup/$uid/$content")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }

    fun callVoiceFriend(uid_other: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã gọi voice"
                    val title = "Cuộc gọi voice mới"
                    sendAPIRequest("https://android-backend-3day.vercel.app/callvoiceperson/$uid_other/$uid/$content/$title")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun callVoiceGroup(idGroup: String, uid: String){
        Firebase.firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val content = name +" đã gọi voice"
                    sendAPIRequest("https://android-backend-3day.vercel.app/callvoicegroup/$idGroup/$uid/$content")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun sendAPIRequest(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val urlConnection = URL(url)
                connection = urlConnection.openConnection() as HttpURLConnection
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Request successful: HTTP OK")
                    println("Response message: ${connection.responseMessage}")
                } else {
                    println("Request failed: HTTP response code $responseCode")
                }
            } catch (e: Exception) {
                println("Request failed: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
    }
}