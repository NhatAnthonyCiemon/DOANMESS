package com.example.doanmess

import HandleOnlineActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.jar.Manifest


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
            auth1.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
        // ======================================================================================================
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("HHHHHHHHHHHHHHH", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.e("HHHHHHHHHHHHHHH", token!!)
        })
        Firebase.firestore.clearPersistence().addOnCompleteListener {
        }
        FirebaseApp.initializeApp(this)
        //kiểm tra có quyền thông báo không không thì xin
        checkPermissionNotify()
        btnAllchat = findViewById<Button>(R.id.btnAllchat)
        btnContact = findViewById<Button>(R.id.btnContact)
        btnInfo = findViewById<Button>(R.id.btnInfo)
        btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        btnMore = findViewById<ImageButton>(R.id.btnMore)
        txtName = findViewById<TextView>(R.id.txtName)
        btnGroup = findViewById<Button>(R.id.btnGroup)
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
            CustomButtonToActive(btnInfo)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnContact)
            ChangeFragment(inforFragment())
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
         //   updateOnlineStatus(true)
            dbfirestore.collection("users").document(User!!.uid).get()
                .addOnSuccessListener { document ->
                    txtName.text = document.getString("Name")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
            else{
                ChannelController(this).createNotificationChannel()
            }
        }
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