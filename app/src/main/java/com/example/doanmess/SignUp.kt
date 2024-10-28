package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()


        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignIn = findViewById(R.id.btnSignIn)

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Sign-Up successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Home::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthWeakPasswordException) {
                                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            } else if (exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show()
                            } else if (etPassword != etConfirmPassword) {
                                Toast.makeText(this, "Password and Confirm Password must be the same", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Sign-Up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        btnSignIn.setOnClickListener {
            // Chuyển về màn hình đăng nhập
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Đóng màn hình đăng ký
        }
    }
}