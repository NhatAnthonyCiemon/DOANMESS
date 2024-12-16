package com.example.doanmess


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Locale


class Home : HandleOnlineActivity() {
    lateinit var btnAllchat: Button
    lateinit var btnContact: Button
    lateinit var btnInfo: Button
    lateinit var btnSearch: ImageButton
    lateinit var btnMore: ImageButton
    lateinit var btnGroup: Button
    lateinit var txtName: TextView
    lateinit var txtCall: TextView
    lateinit var txtCallGroup: TextView
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var User: FirebaseUser? =null
    private var dbfirestore = Firebase.firestore
    var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0 , systemBars.right, systemBars.bottom)
            insets
        }
        applyDarkMode()
        // Đọc ngôn ngữ đã lưu trong SharedPreferences
        val sharedPreferences = getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)
        val currentLanguage = sharedPreferences?.getString("language", Locale.getDefault().language) ?: Locale.getDefault().language

        // Đặt lại ngôn ngữ khi ứng dụng khởi động
        setLocale(currentLanguage)



        val auth1 = FirebaseAuth.getInstance()
        val currentUser = auth1.currentUser
        if (currentUser == null) {
            // User is not signed in, navigate to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

       // Toast.makeText(this, "Welcome ${currentUser?.email}", Toast.LENGTH_SHORT).show()

        // Tạm thời comment vì có thể cần để sau này

//        val logOutBtn = findViewById<Button>(R.id.logOutBtn)
//        logOutBtn.setOnClickListener {
//
//            val dir = filesDir
//            val files = dir.listFiles()
//            if (files != null) {
//                for (file in files) {
//                    if (file.isFile) {
//                        try {
//                            file.delete()
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//            val docRef = dbfirestore.collection("users").document(currentUser!!.uid)
//            //xóa 1 phần tử trong mảng field của firestore
//            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//
//            docRef.update("Devices", FieldValue.arrayRemove(androidId))
//                .addOnSuccessListener {
//                    Log.d("thanhhhhhhcoooong", "Phần tử đã được xóa thành công khỏi mảng")
//                }
//                .addOnFailureListener { e ->
//                    Log.d("xxxxxxxxxxxxxxxx", "Loi xoa phan tu", e)
//                }
//
//            val docRef2 = dbfirestore.collection("devices").document(androidId.toString())
//            docRef2.get()
//                .addOnSuccessListener { document ->
//                    if (document.exists()) {
//
//                        docRef2.update("User_id", "")
//                            .addOnSuccessListener {
//                                Log.d("TAG", "Trường User_id đã được ghi đè thành công")
//                                auth1.signOut()
//                                val intent = Intent(this, Login::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                            .addOnFailureListener { e ->
//                                Log.w("TAG", "Lỗi khi ghi đè trường User_id", e)
//                            }
//                    } else {
//                        docRef2.set(hashMapOf("Token" to "", "User_id" to ""))
//                            .addOnSuccessListener {
//                                Log.d("TAG", "DocumentSnapshot successfully updated!")
//                                auth1.signOut()
//                                val intent = Intent(this, Login::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                            .addOnFailureListener { e ->
//                                Log.w("TAG", "Error updating document", e)
//                            }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.d("TAG", "get failed with ", exception)
//                }
//
//        }
        // ======================================================================================================

        Firebase.firestore.clearPersistence().addOnCompleteListener {
        }
        FirebaseApp.initializeApp(this)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.e("Android IDDDDDDDDDDDDDD", androidId)
        btnAllchat = findViewById<Button>(R.id.btnAllchat)
        btnContact = findViewById<Button>(R.id.btnContact)
        btnInfo = findViewById<Button>(R.id.btnInfo)
        btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        btnMore = findViewById<ImageButton>(R.id.btnMore)
        txtName = findViewById<TextView>(R.id.txtName)
        btnGroup = findViewById<Button>(R.id.btnGroup)
        txtCall = findViewById(R.id.txtCall)
        txtCallGroup = findViewById(R.id.txtCallGroup)
        btnGroup.visibility = View.GONE
        btnGroup.setOnClickListener{
            val intent = Intent(this, CreateGroup::class.java)
            startActivity(intent)
        }

        btnAllchat.setOnClickListener {
            btnGroup.visibility = View.VISIBLE
            CustomButtonToActive(btnAllchat)
            CustomButtonToInactive(btnContact)
            CustomButtonToInactive(btnInfo)
            ChangeFragment(AllChatFra.newInstance())
        }
        btnContact.setOnClickListener {
            CustomButtonToActive(btnContact)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnInfo)
            btnGroup.visibility = View.INVISIBLE
            ChangeFragment(ContactsFragment.newInstance())
        }
        btnInfo.setOnClickListener {
            btnGroup.visibility = View.INVISIBLE
            CustomButtonToActive(btnInfo)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnContact)
            ChangeFragment(inforFragment())
        }
        btnSearch.setOnClickListener {
             CustomButtonToActive(btnContact)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnInfo)
            val fragment_Contact = ContactsFragment.newInstance()
            ChangeFragment(fragment_Contact)
            fragment_Contact.focusSearch()
        }
        btnMore.setOnClickListener{
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }
        auth = Firebase.auth
        ChangeFragment(AllChatFra.newInstance())
        User = auth.currentUser
  /*      if (User == null) {
            auth.signInWithEmailAndPassword("doanmessg@gmail.com", "1234567")
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        User = auth.currentUser
                        updateOnlineStatus(true)
                        Toast.makeText(
                            baseContext,
                            "Authentication success.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }*/
        if (User != null) {
            dbfirestore.collection("users").document(User!!.uid).get()
                .addOnSuccessListener { document ->
                    txtName.text = document.data?.get("Name").toString()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        baseContext,
                        "Error fetching document",
                        Toast.LENGTH_SHORT,
                    ).show()
                    txtName.text= "Loading..."
                }
        }
        onCallRequest(true, "calls")
        onCallRequest(false, "callvoices")
        onCallGroupRequest()
        onCallVoiceGroupRequest()
    }

    fun checkPermissionNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest, 1)
            } else {
                ChannelController(this).createNotificationChannel()
            }
        }
    }

    //nhận kết quả trả về từ 1 activity khác viết code

    override fun onResume() {
        super.onResume()
        //kiểm tra có quyền thông báo không không thì xin
        checkPermissionNotify()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                ChannelController(this).createNotificationChannel()
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun CustomButtonToActive(view: View) {
        view.background = getDrawable(R.drawable.custombtn02_home)
        (view as? Button)?.setTextColor(getColor(R.color.white))
    }

    fun CustomButtonToInactive(view: View) {
        view.background = getDrawable(R.drawable.custonlinear01_home)
        (view as? Button)?.setTextColor(getColor(R.color.xam))
    }
    fun ChangeFragment(fragment :Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitNow()
    }
    // Function to update online status
/*    private fun updateOnlineStatus(isOnline: Boolean) {
        val database = FirebaseDatabase.getInstance()
        val userStatusRef = database.getReference("users/${User!!.uid}/online")
        userStatusRef.setValue(isOnline)
    }*/
 /*   override fun onDestroy() {
        super.onDestroy()
        updateOnlineStatus(false)
    }*/

/*    override fun onResume() {
        super.onResume()
        updateOnlineStatus(true)
    }
    override fun onStop() {
        super.onStop()
        updateOnlineStatus(false)
    }*/

    fun applyDarkMode() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid  // Lấy userId từ FirebaseAuth
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val savedMode = document.getString("DarkMode") ?: "Off"
                if (savedMode != "Off") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
    private fun onCallRequest(isVideoCall: Boolean, firebasePath: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val firebaseRef = Firebase.database.getReference(firebasePath)

        firebaseRef.child(userId).child("incoming").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val incomingId = snapshot.value.toString()
                    if (incomingId == "****endcall****") {
                        txtCall.visibility = View.GONE
                        return
                    }

                    // Fetch caller information from Firestore
                    firestore.collection("users").document(incomingId).get()
                        .addOnSuccessListener { document ->
                            val callerName = document.data?.get("Name").toString()
                            val callType = if (isVideoCall) "video call" else "voice call"

                            txtCall.text = "Incoming $callType from $callerName"
                            txtCall.visibility = View.VISIBLE
                            txtCall.setOnClickListener {
                                val intent = Intent(this@Home, Call::class.java).apply {
                                    putExtra("friendId", incomingId)
                                    putExtra("call", false)
                                    putExtra("isVideoCall", isVideoCall)
                                }
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            txtCall.visibility = View.GONE
                        }
                } else {
                    txtCall.visibility = View.GONE
                }
            }
        })
    }
    // Tạo một map để lưu trữ các listener của từng group
    private val activeListeners = mutableMapOf<String, ValueEventListener>()

    private fun onCallGroupRequest() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        // Lắng nghe Firestore
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("TAG", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val groups = snapshot.get("Groups") as? List<String>
                    if (groups != null) {
                        // Hủy bỏ các listener cũ trước khi thêm listener mới
                        removeOldListeners(groups)

                        // Lắng nghe các group mới
                        for (group in groups) {
                            val listener = Firebase.database.getReference("callGroups")
                                .child(group)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val groupId = snapshot.key.toString()
                                            Firebase.firestore.collection("groups").document(groupId).get()
                                                .addOnSuccessListener { document ->
                                                    val groupName = document.data?.get("Name").toString()
                                                    txtCallGroup.text = "Incoming call from $groupName"
                                                    txtCallGroup.visibility = View.VISIBLE
                                                    txtCallGroup.setOnClickListener {
                                                        val intent = Intent(this@Home, CallGroup::class.java).apply {
                                                            putExtra("groupId", groupId)
                                                            putExtra("call", false)
                                                            putExtra("isVideoCall", true)
                                                        }
                                                        startActivity(intent)
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    txtCallGroup.visibility = View.GONE
                                                }
                                        } else {
                                            txtCallGroup.visibility = View.GONE
                                        }
                                    }
                                })

                            // Lưu listener vào map
                            activeListeners[group] = listener
                        }

                        Log.d("TAG", "Groups: $groups")
                    } else {
                        Log.d("TAG", "No groups found.")
                    }
                }
            }
    }

    private fun onCallVoiceGroupRequest() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        // Lắng nghe Firestore
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("TAG", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val groups = snapshot.get("Groups") as? List<String>
                    if (groups != null) {
                        // Hủy bỏ các listener cũ trước khi thêm listener mới
                        removeOldListeners(groups)

                        // Lắng nghe các group mới
                        for (group in groups) {
                            val listener = Firebase.database.getReference("callGroupsvoices")
                                .child(group)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {}

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val groupId = snapshot.key.toString()
                                            Firebase.firestore.collection("groups").document(groupId).get()
                                                .addOnSuccessListener { document ->
                                                    val groupName = document.data?.get("Name").toString()
                                                    txtCallGroup.text = "Incoming voice call from $groupName"
                                                    txtCallGroup.visibility = View.VISIBLE
                                                    txtCallGroup.setOnClickListener {
                                                        val intent = Intent(this@Home, CallGroup::class.java).apply {
                                                            putExtra("groupId", groupId)
                                                            putExtra("call", false)
                                                            putExtra("isVideoCall", false)
                                                        }
                                                        startActivity(intent)
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    txtCallGroup.visibility = View.GONE
                                                }
                                        } else {
                                            txtCallGroup.visibility = View.GONE
                                        }
                                    }
                                })

                            // Lưu listener vào map
                            activeListeners[group] = listener
                        }

                        Log.d("TAG", "Groups: $groups")
                    } else {
                        Log.d("TAG", "No groups found.")
                    }
                }
            }
    }

    private fun removeOldListeners(currentGroups: List<String>) {
        val groupsToRemove = activeListeners.keys.filter { it !in currentGroups }
        for (group in groupsToRemove) {
            val reference = Firebase.database.getReference("callGroups").child(group)
            val listener = activeListeners[group]
            if (listener != null) {
                reference.removeEventListener(listener)
            }
            activeListeners.remove(group)
        }
    }

    fun setLocale(languageCode: String) {
        try {
            val sharedPreferences = getSharedPreferences("UserLanguagePref", Context.MODE_PRIVATE)  // Đổi tên ở đây
            val currentLanguage = sharedPreferences.getString("language", Locale.getDefault().language) ?: Locale.getDefault().language

            // Kiểm tra nếu ngôn ngữ đã thay đổi
            if (currentLanguage != languageCode) {
                // Tạo đối tượng Locale từ mã ngôn ngữ
                val locale = Locale(languageCode)
                Locale.setDefault(locale)

                // Cập nhật cấu hình ngôn ngữ
                val config = resources.configuration
                config.setLocale(locale)

                // Tạo context mới với cấu hình ngôn ngữ
                createConfigurationContext(config)

                // Lưu ngôn ngữ đã thay đổi vào SharedPreferences
                sharedPreferences.edit().putString("language", languageCode).apply()

                // Tái tạo lại Activity để áp dụng ngôn ngữ mới
                val intent = intent
                recreate()  // Tái tạo lại Activity
                startActivity(intent)  // Khởi động lại Activity với ngôn ngữ mới
            }
        } catch (e: Exception) {
            // Xử lý lỗi nếu có
            Toast.makeText(this, "Error setting language: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onPause() {
        super.onPause()
        // Lưu ngôn ngữ vào SharedPreferences khi ứng dụng chuyển sang trạng thái tạm dừng
        val sharedPreferences = getSharedPreferences("UserLanguagePref", Context.MODE_PRIVATE)
        val languageCode = Locale.getDefault().language  // Lấy ngôn ngữ hiện tại
        sharedPreferences.edit().putString("language", languageCode).apply()
    }




    // Hàm để lấy và xóa giá trị từ SharedPreferences


}