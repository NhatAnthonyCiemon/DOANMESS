package com.example.doanmess
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings

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
    private val btnGroup: Button by lazy { atvtContext.findViewById(R.id.btnGroup) }

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
                    loadingBar.visibility = View.GONE
                    btnGroup.visibility = View.VISIBLE
                    list.removeIf { it !is DataMessGroup }
                    for (childSnapshot in snapshot.children) {
                        val latestsmallSnapshot = childSnapshot.child("Messages").children.maxByOrNull {
                            it.child("Time").getValue(Long::class.java) ?: 0L
                        }
                        if (latestsmallSnapshot != null) {
                            val content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                            val recvId = latestsmallSnapshot.child("RecvId").getValue(String::class.java)
                            val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                            val status = childSnapshot.child("Status").getValue(Boolean::class.java) ?: false
                            val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)

                            if (User != null && recvId != null && sendId != null) {
                                if (User!!.uid == sendId) {
                                    dbfirestore.collection("users").document(recvId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                val avatar = document.data?.get("Avatar").toString()
                                                val existingItem = list.find { it.uid == recvId.toString() }
                                  /*              if (existingItem != null) {
                                                    existingItem.apply {
                                                        this.othersend= false
                                                        this.avatar = avatar
                                                        this.name = name
                                                        this.message = if(!othersend) "Bạn: ${content.toString()}" else last_name + ": ${content.toString()}"
                                                        this.timestamp = timestamp!!
                                                        this.status = status!!
                                                    }
                                                } else {*/
                                                    list.add(
                                                        DataMess(
                                                            avatar,
                                                            recvId.toString(),
                                                            name,
                                                            content.toString(),
                                                            timestamp!!,
                                                            status!!,
                                                            false
                                                        )
                                                    )
                                              //  }
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
                                    Log.d("AllFra", snapshot.key.toString())
                                    dbfirestore.collection("users").document(sendId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                val avatar = document.data?.get("Avatar").toString()
                                                val existingItem = list.find { it.uid == sendId.toString() }
                                          /*      if (existingItem != null) {
                                                    existingItem.apply {
                                                        this.othersend= true
                                                        this.avatar = avatar
                                                        this.name = name
                                                        this.message =if(!othersend) "Bạn: ${content.toString()}" else last_name + ": ${content.toString()}"
                                                        this.timestamp = timestamp!!
                                                        this.status = status!!
                                                    }
                                                } else {*/
                                                    list.add(
                                                        DataMess(
                                                            avatar,
                                                            sendId!!,
                                                            name,
                                                            content.toString(),
                                                            timestamp!!,
                                                            status!!,
                                                            true
                                                        )
                                                    )
                                               // }

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
        Firebase.database.getReference("users").child(User!!.uid)
            .addValueEventListener(userListener)
        groupListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    loadingBar.visibility = View.GONE
                    btnGroup.visibility = View.VISIBLE
                    list.removeIf { it is DataMessGroup }
                    for (childSnapshot in snapshot.children) {
                        if (myGroup.contains(childSnapshot.key.toString())) {
                            val latestsmallSnapshot = childSnapshot.child("Messages").children.maxByOrNull {
                                it.child("Time").getValue(Long::class.java) ?: 0L
                            }
                            if (latestsmallSnapshot != null) {
                                val content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                                val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                                val status = childSnapshot.child("Status").child(User!!.uid).getValue(Boolean::class.java) ?: false
                                val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)

                                if (User!!.uid == sendId) {
                                    list.add(DataMessGroup(avatarList[childSnapshot.key.toString()].toString(), childSnapshot.key!!, myGroup[childSnapshot.key.toString()].toString(), content.toString(), timestamp!!, status!!, "Bạn", myGroup[childSnapshot.key.toString()].toString()))
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
                                                        avatarList[childSnapshot.key.toString()].toString(),
                                                        childSnapshot.key.toString(),
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
                loadingBar.visibility = View.GONE
                btnGroup.visibility = View.VISIBLE
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
//            override fun onItemClick(position: Int) {
//                val intent = Intent(atvtContext, MainChat::class.java)
//                intent.putExtra("uid", list[position].uid)
//                intent.putExtra("name", list[position].name)
//                intent.putExtra("avatar", list[position].avatar)
//                intent.putExtra("isGroup", list[position].isGroup)
//                startActivity(intent)
//            }
                override fun onItemClick(position: Int) {
                    val intent = Intent(atvtContext, MainChat::class.java)
                    val item = list[position]

                    intent.putExtra("uid", item.uid)
                    intent.putExtra("avatar", item.avatar)
                    intent.putExtra("isGroup", item.isGroup)

                    // Check if item is of type DataMessGroup to send groupname instead of name
                    if (item is DataMessGroup) {
                        intent.putExtra("isGroup", true)
                        intent.putExtra("name", item.groupname) // Send groupname for DataMessGroup
                    } else {
                        intent.putExtra("isGroup", false)
                        intent.putExtra("name", item.name) // Send name for other types
                    }

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