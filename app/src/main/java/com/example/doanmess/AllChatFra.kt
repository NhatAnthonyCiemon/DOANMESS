package com.example.doanmess
import android.annotation.SuppressLint
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
    // TODO: Rename and change types of parameters
    private var list: MutableList<DataMess> = mutableListOf()
    private var myGroup: MutableMap<String, String> = mutableMapOf()
    lateinit var atvtContext: Context
    private lateinit var auth: FirebaseAuth
    private var User: FirebaseUser? = null
    val dbfirestore = Firebase.firestore
    private lateinit var adapter: Chat_AllChatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        atvtContext = requireActivity()
        auth = Firebase.auth
        User = auth.currentUser
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        dbfirestore.firestoreSettings = settings
        //myGroup["1"]= "Vô Lăng Vàng"

    }

    private fun showHighPriorityNotification(context: Context, title: String, message: String, idNotify: Int) {
        if (ActivityCompat.checkSelfPermission(
                atvtContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val channelId = "TIN_NHAN_MOI"
            val bitmapAvatar = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.avatar_placeholder_allchat
            )
            val intent = Intent(context, MainChat::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.checkmark2)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setLargeIcon(bitmapAvatar)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            with(NotificationManagerCompat.from(atvtContext)) {
                notify(idNotify, builder.build())
            }
        }
    }

    fun encodeStringToNumber(input: String): Int {
        val crc = CRC32()
        crc.update(input.toByteArray())
        return (crc.value % Int.MAX_VALUE).toInt()
    }

    private fun ResumeRealTimeListen() {
        Firebase.database.getReference("users").child(User!!.uid)
            .addValueEventListener(object : ValueEventListener {
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
                                                list.add(DataMess(R.drawable.avatar_placeholder_allchat, name, content.toString(), timestamp!!, status!!, true))
                                                list.sortByDescending { it.timestamp }
                                                adapter.notifyDataSetChanged()
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
                                                list.add(DataMess(R.drawable.avatar_placeholder_allchat, name, content.toString(), timestamp!!, status!!, false))

                                                if(status){
                                                    showHighPriorityNotification(atvtContext, name, content.toString(), encodeStringToNumber(sendId.toString()+timestamp.toString()))
                                                }
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
            })

        Firebase.database.getReference("groups").addValueEventListener(object : ValueEventListener {
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
                                    list.add(DataMessGroup(R.drawable.avatar_placeholder_allchat, myGroup[childSnapshot.key.toString()].toString(), content.toString(), timestamp!!, status!!, "Bạn", myGroup[childSnapshot.key.toString()].toString()))
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
                                                        R.drawable.avatar_placeholder_allchat,
                                                        name,
                                                        content.toString(),
                                                        timestamp!!,
                                                        status!!,
                                                        name,
                                                        myGroup[childSnapshot.key.toString()].toString()
                                                    )
                                                )
                                                if(status){
                                                    showHighPriorityNotification(atvtContext, myGroup[childSnapshot.key.toString()].toString(),name  +": "+ content.toString(), encodeStringToNumber(sendId.toString()+timestamp.toString()))
                                                }
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
        })

    }
    private fun ListenFirebase(){
        if(User == null){
            return
        }
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
                                val groupID = groupDoc.id
                                myGroup[groupID] = name_group
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
        adapter = Chat_AllChatAdapter(list)
        adapter.setOnItemClickListener(object : Chat_AllChatAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                //code để chuyển đến màn hình chat
                val intent = Intent(atvtContext, MainChat::class.java)
                startActivity(intent)
            }
        })

        recyclerView.adapter = adapter

        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return view
    }

    override fun onDestroyView(){
        super.onDestroyView()
        myGroup.clear()
        PauseRealTimeListen()
    }


    override fun onResume() {
        super.onResume()
        ListenFirebase()
    }

    override fun onPause() {
        super.onPause()
        PauseRealTimeListen()
    }

    fun PauseRealTimeListen(){
        Firebase.database.getReference("users").child(User?.uid.toString()).removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        Firebase.database.getReference("groups").removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            AllChatFra().apply {
            }
    }
}