package com.example.doanmess

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging


class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Kiem tra da dang nhap chua
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, navigate to Home activity
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
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
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Home::class.java)
                            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                            LoginNewDevice().RegisterNewDevice(androidId, auth.currentUser!!.uid)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnForgotPassword.setOnClickListener {
            // Chuyển sang Activity quên mật khẩu
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            // Chuyển sang Activity đăng ký
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

}

class LoginNewDevice {
    constructor() {

    }
    fun RegisterNewDevice(deviceID:String, uid: String) {
        // thêm mã thiết bị vào 1 trường array tên Devices trong collection users
        val docRef = Firebase.firestore.collection("users").document(uid)
        docRef
            .update("Devices", FieldValue.arrayUnion(deviceID))
            .addOnSuccessListener {
                Log.d("TAG", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error updating document", e)
            }

        val secondDocRef = Firebase.firestore.collection("devices").document(deviceID)
        secondDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.e("HHHHHHHHHHHHHHH", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }

                        // Get new FCM registration token
                        val token = task.result.toString()
                        val data = hashMapOf(
                            "Token" to token,
                            "User_id" to uid
                        )
                        secondDocRef
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Error updating document", e)
                            }
                    })
                } else {
                    secondDocRef.update("User_id", uid)
                        .addOnSuccessListener {
                            Log.d("TAG", "User_id field successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error updating User_id field", e)
                        }

                }
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error fetching document", e)
            }


    }

}
