package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUp : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignIn = findViewById(R.id.btnSignIn)

        btnSignUp.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Thực hiện xử lý đăng ký người dùng
                // Bạn có thể kết nối với backend hoặc Firebase để thực hiện đăng ký
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                // Sau khi đăng ký thành công, bạn có thể chuyển về màn hình đăng nhập
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish() // Đóng màn hình đăng ký sau khi chuyển về màn hình đăng nhập
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