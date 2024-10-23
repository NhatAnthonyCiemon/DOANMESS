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

class Login : AppCompatActivity() {
//    private lateinit var etEmail: EditText
//    private lateinit var etPassword: EditText
//    private lateinit var btnForgotPassword: Button
//    private lateinit var btnLogin: Button
//    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnForgotPassword = findViewById<Button>(R.id.btnForgotPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            } else {
                // Thực hiện xử lý đăng nhập (giả sử không có backend)
                if (email == "user123@gmail.com" && password == "123456") {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Chuyển tới activity chính sau khi đăng nhập thành công
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnForgotPassword.setOnClickListener {
            // Xử lý logic quên mật khẩu ở đây
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        btnSignUp.setOnClickListener {
            // Chuyển sang Activity đăng ký
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

}