package com.example.doanmess
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

open class HandleOnlineActivity : AppCompatActivity() {
    private var user: FirebaseUser? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userStatusRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userStatusRef = database.getReference("users/${user?.uid}/online")
    }

    override fun onStart() {
        super.onStart()
        updateOnlineStatus(true)
    }

    override fun onPause() {
        super.onPause()
        updateOnlineStatus(false)
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        user?.let {
            userStatusRef.setValue(isOnline).addOnCompleteListener {
                //         updateGroupOnlineStatus()
            }
        }
    }

    private fun updateGroupOnlineStatus() {
        firestore.collection("users").document(user!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val groupIds = document.get("Groups") as? List<String> ?: emptyList()
                    for (groupId in groupIds) {
                        updateSingleGroupOnlineStatus(groupId)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle possible errors.
            }
    }

    private fun updateSingleGroupOnlineStatus(groupId: String) {
        firestore.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userIds = document.get("userIds") as? List<String> ?: emptyList()
                    checkGroupUsersOnlineStatus(userIds, groupId)
                }
            }
            .addOnFailureListener { e ->
                // Handle possible errors.
            }
    }

    private fun checkGroupUsersOnlineStatus(userIds: List<String>, groupId: String) {
        var onlineCount = 0
        val userStatusRefs = userIds.map { database.getReference("users/$it/online") }

        for (ref in userStatusRefs) {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                    if (isOnline) {
                        onlineCount++
                    }
                    if (ref == userStatusRefs.last()) {
                        val groupStatusRef = database.getReference("groups/$groupId/online")
                        groupStatusRef.setValue(onlineCount > 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }
}