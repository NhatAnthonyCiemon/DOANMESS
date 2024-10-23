package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore


class Home : AppCompatActivity() {
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
        FirebaseApp.initializeApp(this)
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

    }
    override fun onStart() {
        super.onStart()
        User = auth.currentUser

        if (User == null) {
            auth.signInWithEmailAndPassword("doanmessg@gmail.com", "1234567")
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        User = auth.currentUser
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
        }
        else{
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

}