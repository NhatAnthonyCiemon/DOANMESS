package com.example.doanmess

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnAllchat = findViewById<Button>(R.id.btnAllchat)
        val btnContact = findViewById<Button>(R.id.btnContact)
        val btnInfo = findViewById<Button>(R.id.btnInfo)
        btnAllchat.setOnClickListener {
            CustomButtonToActive(btnAllchat)
            CustomButtonToInactive(btnContact)
            CustomButtonToInactive(btnInfo)
        }
        btnContact.setOnClickListener {
            CustomButtonToActive(btnContact)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnInfo)
        }
        btnInfo.setOnClickListener {
            CustomButtonToActive(btnInfo)
            CustomButtonToInactive(btnAllchat)
            CustomButtonToInactive(btnContact)
        }

        val fragment_ChatAll = AllChatFra.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment_ChatAll)
        transaction.commit()

    }


        fun CustomButtonToActive(view: View) {
        view.background = getDrawable(R.drawable.custombtn02_home)
        (view as? Button)?.setTextColor(getColor(R.color.white))
    }

    fun CustomButtonToInactive(view: View) {
        view.background = getDrawable(R.drawable.custonlinear01_home)
    }

}