package com.example.doanmess

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
import androidx.fragment.app.Fragment

class Home : AppCompatActivity() {
    lateinit var btnAllchat: Button
    lateinit var btnContact: Button
    lateinit var btnInfo: Button
    lateinit var btnSearch: ImageButton
    lateinit var btnMore: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0 , systemBars.right, systemBars.bottom)
            insets
        }
        btnAllchat = findViewById<Button>(R.id.btnAllchat)
        btnContact = findViewById<Button>(R.id.btnContact)
        btnInfo = findViewById<Button>(R.id.btnInfo)
        btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        btnMore = findViewById<ImageButton>(R.id.btnMore)
        btnAllchat.setOnClickListener {
            CustomButtonToActive(btnAllchat)
            CustomButtonToInactive(btnContact)
            CustomButtonToInactive(btnInfo)
            ChangeFragment(AllChatFra.newInstance())
        }
        btnContact.setOnClickListener {
            CustomButtonToActive(btnContact)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnInfo)
            ChangeFragment(ContactsFragment.newInstance())
        }
        btnInfo.setOnClickListener {
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
        ChangeFragment(AllChatFra.newInstance())

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