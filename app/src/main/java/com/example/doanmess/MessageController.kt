package com.example.doanmess

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.net.HttpURLConnection
import java.net.URL

class MessageController {
    fun newCreateGroup(id: String, uid: String){
        Firebase.firestore.collection("user").document(uid).get()
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
        Firebase.firestore.collection("user").document(uid).get()
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
        Firebase.firestore.collection("user").document(uid).get()
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
        Firebase.firestore.collection("user").document(uid).get()
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
    fun newMessageGroup(id: String, uid: String, content: String){
        Firebase.firestore.collection("user").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.get("Name") as? String ?: ""
                    val afterContent = name +": "+ content
                    sendAPIRequest("https://android-backend-3day.vercel.app/sendgroup/$id/$uid/$afterContent")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error", e.toString())
            }
    }
    fun sendAPIRequest(url: String) {
        var connection: HttpURLConnection? = null
        try {
            val urlConnet = URL(url)
            connection = urlConnet.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("OK")
            } else {
                println("Fail")
            }
        } catch (e: Exception) {
            println("Fail")
        } finally {
            connection?.disconnect()
        }
    }
}