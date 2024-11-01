package com.example.doanmess
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.createuiproject.MainChat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.CRC32

class AllChatFra : Fragment() {
    private var list: MutableList<DataMess> = mutableListOf()
    private var myGroup: MutableMap<String, String> = mutableMapOf()
    private var avatarList: MutableMap<String, String> = mutableMapOf()
    lateinit var atvtContext: Activity
    private lateinit var auth: FirebaseAuth
    private var User: FirebaseUser? = null
    val dbfirestore = Firebase.firestore
    private lateinit var adapter: Chat_AllChatAdapter
    private lateinit var loadingBar: ProgressBar
    private lateinit var userListener: ValueEventListener
    private lateinit var groupListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        atvtContext = requireActivity()
        auth = Firebase.auth
        User = auth.currentUser
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        dbfirestore.firestoreSettings = settings
    }

    private fun ResumeRealTimeListen() {
        PauseRealTimeListen()
        userListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    list.removeIf { it !is DataMessGroup }
                    for (childSnapshot in snapshot.children) {
                        val latestsmallSnapshot = childSnapshot.children.maxByOrNull {
                            it.child("Time").getValue(Long::class.java) ?: 0L
                        }
                        if (latestsmallSnapshot != null) {
                            val content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                            val recvId = latestsmallSnapshot.child("RecvId").getValue(String::class.java)
                            val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                            val status = latestsmallSnapshot.child("Status").getValue(Boolean::class.java)
                            val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)

                            if (User!!.uid == sendId) {
                                dbfirestore.collection("users").document(recvId.toString())
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            val name = document.data?.get("Name").toString()
                                            val avatar = document.data?.get("Avatar").toString()
                                            list.add(DataMess(recvId.toString(), avatar, name, content.toString(), timestamp!!, status!!, true))
                                            list.sortByDescending { it.timestamp }
                                            adapter.notifyDataSetChanged()
                                            loadingBar.visibility = View.GONE
                                        } else {
                                            Log.d("exist", "No such document")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("exist", "get failed with ", exception)
                                    }
                            } else {
                                dbfirestore.collection("users").document(sendId.toString())
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            val name = document.data?.get("Name").toString()
                                            val avatar = document.data?.get("Avatar").toString()
                                            list.add(DataMess(sendId.toString(), avatar, name, content.toString(), timestamp!!, status!!, false))
                                            list.sortByDescending { it.timestamp }
                                            adapter.notifyDataSetChanged()
                                        } else {
                                            Log.d("exist", "No such document")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d("exist", "get failed with ", exception)
                                    }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        Firebase.database.getReference("users").child(User!!.uid)
            .addValueEventListener(userListener)
        groupListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    list.removeIf { it is DataMessGroup }
                    for (childSnapshot in snapshot.children) {
                        if (myGroup.contains(childSnapshot.key.toString())) {
                            val latestsmallSnapshot = childSnapshot.children.maxByOrNull {
                                it.child("Time").getValue(Long::class.java) ?: 0L
                            }
                            if (latestsmallSnapshot != null) {
                                val content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                                val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                                val status = latestsmallSnapshot.child("Status").getValue(Boolean::class.java)
                                val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)

                                if (User!!.uid == sendId) {
                                    list.add(DataMessGroup(childSnapshot.key.toString(), avatarList[childSnapshot.key.toString()].toString(), myGroup[childSnapshot.key.toString()].toString(), content.toString(), timestamp!!, status!!, "Bạn", myGroup[childSnapshot.key.toString()].toString()))
                                    list.sortByDescending { it.timestamp }
                                    adapter.notifyDataSetChanged()
                                } else {
                                    dbfirestore.collection("users").document(sendId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                list.add(
                                                    DataMessGroup(
                                                        childSnapshot.key.toString(),
                                                        avatarList[childSnapshot.key.toString()].toString(),
                                                        name,
                                                        content.toString(),
                                                        timestamp!!,
                                                        status!!,
                                                        name,
                                                        myGroup[childSnapshot.key.toString()].toString()
                                                    )
                                                )
                                                list.sortByDescending { it.timestamp }
                                                adapter.notifyDataSetChanged()
                                            } else {
                                                Log.d("exist", "No such document")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("exist", "get failed with ", exception)
                                        }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        Firebase.database.getReference("groups").addValueEventListener(groupListener)
    }

    private fun ListenFirebase() {
        if (User == null) {
            return
        }
        list.clear()
        adapter.notifyDataSetChanged()
        Firebase.firestore.collection("users").document(User!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    PauseRealTimeListen()
                    myGroup.clear()
                    val groups = documentSnapshot.data?.get("Groups") as? List<String> ?: return@addSnapshotListener
                    val tasks = mutableListOf<Task<DocumentSnapshot>>()

                    for (group_id in groups) {
                        val task = Firebase.firestore.collection("groups").document(group_id).get()
                        tasks.add(task)
                        task.addOnSuccessListener { groupDoc ->
                            if (groupDoc != null && groupDoc.data != null) {
                                val name_group = groupDoc.data?.get("Name").toString()
                                val avatar_group = groupDoc.data?.get("Avatar").toString()
                                val groupID = groupDoc.id
                                myGroup[groupID] = name_group
                                avatarList[groupID] = avatar_group
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        ResumeRealTimeListen()
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.RVChat_AllChat)
        loadingBar = view.findViewById(R.id.LoadingBar)

        adapter = Chat_AllChatAdapter(atvtContext, list)
        adapter.setOnItemClickListener(object : Chat_AllChatAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(atvtContext, MainChat::class.java)
                startActivity(intent)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PauseRealTimeListen()
    }

    override fun onResume() {
        super.onResume()
        list.clear()
        ListenFirebase()
    }

    override fun onPause() {
        super.onPause()
        PauseRealTimeListen()
    }

    fun PauseRealTimeListen() {
        if (::userListener.isInitialized) {
            Firebase.database.getReference("users").child(User!!.uid).removeEventListener(userListener)
            userListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}
            }
        }
        if (::groupListener.isInitialized) {
            Firebase.database.getReference("groups").removeEventListener(groupListener)
            groupListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AllChatFra().apply {}
    }
}