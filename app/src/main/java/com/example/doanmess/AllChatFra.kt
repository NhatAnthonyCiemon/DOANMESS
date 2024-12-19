package com.example.doanmess
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    //private lateinit var loadingBar: ProgressBar
    private lateinit var messageSQLDatabase: MessageSQLDatabase
    private lateinit var messageSQLDao: MessageSQLDao
    private lateinit var messageGroupSQLDao: MessageGroupSQLDao
    private var firstLoad: Boolean = true
    private lateinit var listOfMessageSQL: MutableList<MessageSQL>
    private lateinit var listOfMessageGroupSQL: MutableList<MessageGroupSQL>
    private var listOff: MutableList<String> = mutableListOf()
    private var copiedList: MutableList<String> = mutableListOf()
    private var idMess: MutableMap<String,Boolean> = mutableMapOf()
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
        messageSQLDatabase = MessageSQLDatabase.getInstance(atvtContext)
        messageSQLDao = messageSQLDatabase.messageDao()
        messageGroupSQLDao = messageSQLDatabase.messageGroupDao()
        listOfMessageSQL = messageSQLDao.getAllMessages()
        listOfMessageGroupSQL = messageGroupSQLDao.getAllGroupMessages()
        //avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, othersend: Boolean,isNotify:Boolean
        list.addAll(
            listOfMessageSQL.map {
                DataMess(
                    it.avatar,
                    it.uid,
                    it.name,
                    it.message,
                    it.timestamp,
                    it.status,
                    it.othersend,
                    it.isNotify,
                    false,
                    true
                )
            }
        )
        //avatar: String?, uid: String, name: String, message: String, timestamp: Long, status: Boolean, whosend: String, groupname: String, isNotify:Boolean,
        list.addAll(
            listOfMessageGroupSQL.map {
                DataMessGroup(
                    it.avatar,
                    it.uid,
                    it.name,
                    it.message,
                    it.timestamp,
                    it.status,
                    "it.whosend",
                    it.groupName,
                    it.isNotify,
                    true,
                    true
                )
            }
        )
        list.sortByDescending { it.timestamp }
        list.forEach {
            idMess[it.uid] = if (it.isGroup) true else false
        }
    }


    private fun ResumeRealTimeListen() {
        PauseRealTimeListen()
        userListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("AllFra", snapshot.key.toString())
                if (snapshot.exists()) {
                    //loadingBar.visibility = View.GONE
                    btnGroup.visibility = View.VISIBLE
                    for (childSnapshot in snapshot.children) {
                        val latestsmallSnapshot = childSnapshot.child("Messages").children.maxByOrNull {
                            it.child("Time").getValue(Long::class.java) ?: 0L
                        }
                        if (latestsmallSnapshot != null) {
                            var content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                            val recvId = latestsmallSnapshot.child("RecvId").getValue(String::class.java)
                            val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                            val status = childSnapshot.child("Status").getValue(Boolean::class.java) ?: false
                            val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)
                            val type = latestsmallSnapshot.child("Type").getValue(String::class.java)
                            content = changeContent(content, type)
                            if (User != null && recvId != null && sendId != null) {
                                if (User!!.uid == sendId) {
                                    dbfirestore.collection("users").document(recvId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                val avatar = document.data?.get("Avatar").toString()
                                                val existingItem = list.find { it.uid == recvId.toString() }
                                                if (existingItem != null) {
                                                    existingItem.apply {
                                                        this.othersend = false
                                                        this.avatar = avatar
                                                        this.name = name
                                                        this.message = if (!othersend) "${atvtContext.getString(R.string.You)}: ${content.toString()}" else last_name + ": ${content.toString()}"
                                                        this.timestamp = timestamp!!
                                                        this.status = status
                                                    }
                                                } else {
                                                    if (listOff.contains(recvId.toString())) {
                                                        list.add(DataMess(avatar, recvId.toString(), name, content.toString(), timestamp!!, status, false, false))
                                                    } else {
                                                        list.add(DataMess(avatar, recvId.toString(), name, content.toString(), timestamp!!, status, false, true))
                                                    }
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
                                } else {
                                    Log.d("AllFra", snapshot.key.toString())
                                    dbfirestore.collection("users").document(sendId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                val avatar = document.data?.get("Avatar").toString()
                                                val existingItem = list.find { it.uid == sendId.toString() }
                                                if (existingItem != null) {
                                                    existingItem.apply {
                                                        this.othersend = true
                                                        this.avatar = avatar
                                                        this.name = name
                                                        this.message = if (!othersend) "${atvtContext.getString(R.string.You)}: ${content.toString()}" else last_name + ": ${content.toString()}"
                                                        this.timestamp = timestamp!!
                                                        this.status = status
                                                    }
                                                } else {
                                                    if (listOff.contains(sendId.toString())) {
                                                        list.add(DataMess(avatar, sendId.toString(), name, content.toString(), timestamp!!, status, true, false))
                                                    } else {
                                                        list.add(DataMess(avatar, sendId, name, content.toString(), timestamp!!, status, true, true))
                                                    }
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
        }
        Firebase.database.getReference("users").child(User!!.uid)
            .addValueEventListener(userListener)
        groupListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("AllFra", snapshot.key.toString())
                if (snapshot.exists()) {
                   // loadingBar.visibility = View.GONE
                    btnGroup.visibility = View.VISIBLE
                    for (childSnapshot in snapshot.children) {
                        if (myGroup.contains(childSnapshot.key.toString())) {
                            val latestsmallSnapshot = childSnapshot.child("Messages").children.maxByOrNull {
                                it.child("Time").getValue(Long::class.java) ?: 0L
                            }
                            if (latestsmallSnapshot != null) {
                                var content = latestsmallSnapshot.child("Content").getValue(String::class.java)
                                val sendId = latestsmallSnapshot.child("SendId").getValue(String::class.java)
                                val status = childSnapshot.child("Status").child(User!!.uid).getValue(Boolean::class.java) ?: false
                                val timestamp = latestsmallSnapshot.child("Time").getValue(Long::class.java)
                                val type = latestsmallSnapshot.child("Type").getValue(String::class.java)
                                content = changeContent(content, type)
                                if (User!!.uid == sendId) {
                                    val existingItem = list.find { it.uid == childSnapshot.key.toString() }
                                    if (existingItem != null) {
                                        existingItem.apply {
                                            this.message = "${atvtContext.getString(R.string.You)}: ${content.toString()}"
                                            this.timestamp = timestamp!!
                                            this.status = status
                                        }
                                    } else {
                                        if (listOff.contains(childSnapshot.key.toString())) {
                                            list.add(DataMessGroup(avatarList[childSnapshot.key.toString()].toString(), childSnapshot.key!!, myGroup[childSnapshot.key.toString()].toString(), content.toString(), timestamp!!, status, "${atvtContext.getString(R.string.You)}", myGroup[childSnapshot.key.toString()].toString(), false))
                                        } else {
                                            list.add(DataMessGroup(avatarList[childSnapshot.key.toString()].toString(), childSnapshot.key!!, myGroup[childSnapshot.key.toString()].toString(), content.toString(), timestamp!!, status, "${atvtContext.getString(R.string.You)}", myGroup[childSnapshot.key.toString()].toString(), true))
                                        }
                                    }
                                    list.sortByDescending { it.timestamp }
                                    adapter.notifyDataSetChanged()
                                } else {
                                    dbfirestore.collection("users").document(sendId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                val existingItem = list.find { it.uid == childSnapshot.key.toString() }
                                                if (existingItem != null) {
                                                    existingItem.apply {
                                                        this.message = "$name: ${content.toString()}"
                                                        this.timestamp = timestamp!!
                                                        this.status = status
                                                        this.last_name = name
                                                    }
                                                } else {
                                                    if (listOff.contains(childSnapshot.key.toString())) {
                                                        list.add(DataMessGroup(avatarList[childSnapshot.key.toString()].toString(), childSnapshot.key.toString(), name, content.toString(), timestamp!!, status, name, myGroup[childSnapshot.key.toString()].toString(), false))
                                                    } else {
                                                        list.add(DataMessGroup(avatarList[childSnapshot.key.toString()].toString(), childSnapshot.key.toString(), name, content.toString(), timestamp!!, status, name, myGroup[childSnapshot.key.toString()].toString(), true))
                                                    }
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
        }
        Firebase.database.getReference("groups").addValueEventListener(groupListener)
    }

    private fun changeContent(content: String?, type: String?): String? {
        return when (type) {
            "image" -> atvtContext.getString(R.string.Image)
            "video" -> atvtContext.getString(R.string.Video)
            "location" -> atvtContext.getString(R.string.Image)
            "audio" -> atvtContext.getString(R.string.Location)
            "file" -> atvtContext.getString(R.string.File)
            else -> content
        }
    }

    private fun ListenFirebase() {
        if (User == null) {
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val userDocRef = Firebase.firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val unnoticed = document.get("Unnoticed") as? MutableList<String>
                if (unnoticed != null) {
                    listOff.addAll(unnoticed)
                }
                getGroupAndMessage()
            }
        }.addOnFailureListener { exception ->
            Log.e("FirestoreError", "Error fetching document: ${exception.message}")
        }
    }

    private fun getGroupAndMessage() {
        Firebase.firestore.collection("users").document(User!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                //loadingBar.visibility = View.GONE
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
        //loadingBar = view.findViewById(R.id.LoadingBar)

        adapter = Chat_AllChatAdapter(atvtContext, list)
        adapter.setOnItemClickListener(object : Chat_AllChatAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (list.isEmpty()) {
                    return
                }
                val intent = Intent(atvtContext, MainChat::class.java)
                val item = list[position]

                intent.putExtra("uid", item.uid)
                intent.putExtra("avatar", item.avatar)
                intent.putExtra("isGroup", item.isGroup)

                if (item is DataMessGroup) {
                    intent.putExtra("isGroup", true)
                    intent.putExtra("name", item.groupname)
                } else {
                    intent.putExtra("isGroup", false)
                    intent.putExtra("name", item.name)
                }

                startActivity(intent)
            }
            override fun onItemClickLong(position: Int) {
                val dialogView = LayoutInflater.from(atvtContext).inflate(R.layout.dialog_delete, null)
                val dialog = AlertDialog.Builder(atvtContext)
                    .setView(dialogView)
                    .create()
                dialog.window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                val btnCancel = dialogView.findViewById<Button>(R.id.dialog_cancel)
                val btnDelete = dialogView.findViewById<Button>(R.id.dialog_delete)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnDelete.setOnClickListener {
                    deleteChat(list[position].uid)
                    list.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    dialog.dismiss()
                }

                dialog.show() // Hiển thị dialog
            }

        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        //fadeOutAndHide(view)
        return view
    }


    override fun onResume() {
        super.onResume()
        ListenFirebase()
        //xem thử Các phần twf của list có trong listOff không
        if(!firstLoad) checkNotify()
        else firstLoad = false
        checkExistMessage()
    }

    private fun checkExistMessage() {
        val firebase_user = Firebase.database.getReference("users").child(User!!.uid)
        val firebase_group = Firebase.database.getReference("groups")
        for(item in idMess){
            if(item.value){
                firebase_group.child(item.key).child("Messages").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            idMess.remove(item.key)
                            idMess[item.key] = false
                            val index = list.indexOfFirst { it.uid == item.key }
                            if (index != -1) {
                                list.removeAt(index)
                                adapter.notifyItemRemoved(index)
                                //Toast.makeText(atvtContext, "Group ${item.key} đã bị xóa", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                firebase_user.child(item.key).child("Messages").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            idMess.remove(item.key)
                            idMess[item.key] = false
                            //list.removeAt(list.indexOfFirst { it.uid == item.key })
                            //lấy vị trí của item trong list và xóa nó sau đó cập nhật lại adapter
                            val index = list.indexOfFirst { it.uid == item.key }
                            if (index != -1) {
                                list.removeAt(index)
                                adapter.notifyItemRemoved(index)
                                adapter.notifyItemRangeChanged(index, list.size)
                            }
                            //Toast.makeText(atvtContext, "User ${item.key} đã bị xóa", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun checkNotify() {

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val userDocRef = Firebase.firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val unnoticed = document.get("Unnoticed") as? MutableList<String>

                listOff.clear()
                if (unnoticed != null) {
                    listOff.addAll(unnoticed)
                }
                //hợp của 2 list
                val onlyListOff = listOff.filter { !copiedList.contains(it) }.toMutableList() // Chuyển sang MutableList
                val onlyListCopy = copiedList.filter { !listOff.contains(it) }
                onlyListOff.addAll(onlyListCopy) // Bây giờ addAll sẽ hoạt động



                onlyListOff.forEach { uidFromListOff ->
                    val index = list.indexOfFirst { it.uid == uidFromListOff }
                    if (index != -1) {
                        list[index].isNotify = !list[index].isNotify
                        adapter.notifyItemChanged(index)
                    }
                }

            }
        }.addOnFailureListener { exception ->
            Log.e("FirestoreError", "Error fetching document: ${exception.message}")
        }

    }
    override fun onPause() {
        super.onPause()
        //val deepCopiedList: MutableList<String> = originalList.map { it }.toMutableList()
        copiedList = listOff.map{it}.toMutableList()
        writeToSQL()
        idMess.clear()
        list.forEach {
            idMess[it.uid] = if (it.isGroup) true else false
        }
    }

    fun writeToSQL() {
        messageSQLDao.deleteAllMessages()
        messageGroupSQLDao.deleteAllGroupMessages()
        listOfMessageSQL.clear()
        listOfMessageGroupSQL.clear()

        for (item in list) {
            when (item) {
                is DataMessGroup -> { // Kiểm tra nếu item là DataMessGroup
                    listOfMessageGroupSQL.add(
                        MessageGroupSQL(
                            item.avatar,
                            item.uid,
                            item.name,
                            item.message,
                            item.timestamp,
                            item.status,
                            item.last_name,
                            item.groupname, // groupname tồn tại trong DataMessGroup
                            item.isNotify,
                            item.isGroup
                        )
                    )
                }
                is DataMess -> { // Kiểm tra nếu item là DataMess, nhưng không phải DataMessGroup
                    if (item !is DataMessGroup) {
                        listOfMessageSQL.add(
                            MessageSQL(
                                item.avatar,
                                item.uid,
                                item.name,
                                item.message,
                                item.timestamp,
                                item.status,
                                item.othersend,
                                item.isNotify
                            )
                        )
                    }
                }
            }
        }
        // Chèn dữ liệu đã xử lý vào cơ sở dữ liệu
        messageSQLDao.insertListMessage(listOfMessageSQL)
        messageGroupSQLDao.insertListGroupMessage(listOfMessageGroupSQL)
    }
    override fun onDestroy() {
        super.onDestroy()
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
    fun deleteChat(uid: String) {
        val firebase_user = Firebase.database.getReference("users").child(User!!.uid)
        val firebase_group = Firebase.database.getReference("groups")
        if (idMess[uid] == true) {
            firebase_group.child(uid).removeValue()
        } else {
            firebase_user.child(uid).removeValue()
        }
        idMess.remove(uid)
    }



    companion object {
        @JvmStatic
        fun newInstance() = AllChatFra().apply {}
    }
}