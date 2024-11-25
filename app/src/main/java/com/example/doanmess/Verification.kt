package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class Verification : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        auth = FirebaseAuth.getInstance()
        val btnCheckVerification = findViewById<Button>(R.id.btnCheckVerification)

        btnCheckVerification.setOnClickListener {
            auth.currentUser?.reload()?.addOnCompleteListener { task ->
                if (auth.currentUser?.isEmailVerified == true) {
                    Toast.makeText(this, "Email verified! You can log in now.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Your email is not verified.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
