package com.example.doanmess

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val btnBack = findViewById<Button>(R.id.backBtn)

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error sending password reset email", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}