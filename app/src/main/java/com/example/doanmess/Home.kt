package com.example.doanmess

import HandleOnlineActivity
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore


class Home : HandleOnlineActivity() {
    lateinit var btnAllchat: Button
    lateinit var btnContact: Button
    lateinit var btnInfo: Button
    lateinit var btnSearch: ImageButton
    lateinit var btnMore: ImageButton
    lateinit var btnGroup: Button
    lateinit var txtName: TextView
    private lateinit var auth: FirebaseAuth
    private var User: FirebaseUser? =null
    private var dbfirestore = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0 , systemBars.right, systemBars.bottom)
            insets
        }


        // CODE DƯỚI ĐÂY LÀ DUY LÂM VIẾT THÊM ĐỂ LÀM PHẦN ĐĂNG NHẬP, NẾU CÓ MÂU THUẪN VỚI CODE CŨ THÌ BÁO ĐỂ CHỈNH LẠI NHA
        // ======================================================================================================
        // code nay de kiem tra xem user da dang nhap chua neu roi thi cho vo Home luon

        val auth1 = FirebaseAuth.getInstance()
        val currentUser = auth1.currentUser
        if (currentUser == null) {
            // User is not signed in, navigate to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

       // Toast.makeText(this, "Welcome ${currentUser?.email}", Toast.LENGTH_SHORT).show()
        val logOutBtn = findViewById<Button>(R.id.logOutBtn)
        logOutBtn.setOnClickListener {

            val dir = filesDir
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        try {
                            file.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            val docRef = dbfirestore.collection("users").document(currentUser!!.uid)
            //xóa 1 phần tử trong mảng field của firestore
            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            docRef.update("Devices", FieldValue.arrayRemove(androidId))
                .addOnSuccessListener {
                    Log.d("thanhhhhhhcoooong", "Phần tử đã được xóa thành công khỏi mảng")
                }
                .addOnFailureListener { e ->
                    Log.d("xxxxxxxxxxxxxxxx", "Loi xoa phan tu", e)
                }

            val docRef2 = dbfirestore.collection("devices").document(androidId.toString())
            docRef2.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {

                        docRef2.update("User_id", "")
                            .addOnSuccessListener {
                                Log.d("TAG", "Trường User_id đã được ghi đè thành công")
                                auth1.signOut()
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Lỗi khi ghi đè trường User_id", e)
                            }
                    } else {
                        docRef2.set(hashMapOf("Token" to "", "User_id" to ""))
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully updated!")
                                auth1.signOut()
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Error updating document", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }

        }
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
           /* CustomButtonToActive(btnInfo)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnContact)
            ChangeFragment(inforFragment())*/
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
}