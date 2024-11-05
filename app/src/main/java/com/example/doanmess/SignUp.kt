package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName

data class User(
    @JvmField @PropertyName("Avatar") val Avatar: String = "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9",
    @JvmField @PropertyName("Name") val Name: String = "",
    @JvmField @PropertyName("RequestSent") val RequestSent: List<String> = listOf(),
    @JvmField @PropertyName("Requests") val Requests: List<String> = listOf(),
    @JvmField @PropertyName("Blocks") val Blocks: List<String> = listOf(),
    @JvmField @PropertyName("Friends") val Friends: List<String> = listOf(),
    @JvmField @PropertyName("Groups") val Groups: List<String> = listOf(),
    @JvmField @PropertyName("Devices") val Devices: List<String> = listOf()
)



class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

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
        firestore = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignIn = findViewById(R.id.btnSignIn)

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val fullName = etFullName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Password and Confirm Password must be the same", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                            val user = User(
                                Avatar = "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9",
                                Name = fullName,
                                RequestSent = listOf(),
                                Requests = listOf(),
                                Blocks = listOf(),
                                Friends = listOf(),
                                Groups = listOf(),
                                Devices = listOf()
                            )
                            firestore.collection("users").document(userId).set(user)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(this, "Sign-Up successful. Please log in.", Toast.LENGTH_SHORT).show()
                                        auth.signOut()  // Sign out the user
                                        val intent = Intent(this, Login::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthWeakPasswordException ->
                                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                is FirebaseAuthUserCollisionException ->
                                    Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show()
                                else ->
                                    Toast.makeText(this, "Sign-Up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }

        btnSignIn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
