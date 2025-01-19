package com.example.doanmess.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.doanmess.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging



class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1
    private val db = FirebaseFirestore.getInstance()


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

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for the sign-in button
        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            signInWithGoogle()
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
                            val user = auth.currentUser
                            if (user != null && user.isEmailVerified) {
//                            if (user != null) {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Home::class.java)
                                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                                LoginNewDevice().RegisterNewDevice(androidId, user.uid)
                                startActivity(intent)
                                finish()
                            } else {
                                auth.signOut()
                                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                            }
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
    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("Login", "Google Sign-In failed", e)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                        LoginNewDevice().RegisterNewDevice(androidId, user.uid)
                        saveUserToFirestoreIfNew(user) { newUser ->
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Home::class.java).apply {
                                putExtra("userName", newUser.Name)
                                putExtra("userAvatar", newUser.Avatar)
                            }
                            startActivity(intent)
                            finish()
                        }
                    } else if (user != null && !user.isEmailVerified) {
                        auth.signOut()
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login with Google failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestoreIfNew(user: FirebaseUser, callback: (User) -> Unit) {
        val uid = user.uid
        val userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = User(
                    Avatar = "https://firebasestorage.googleapis.com/v0/b/doan-cb428.appspot.com/o/avatars%2F3a1a9f11-a045-4072-85da-7202c9bc9989.jpg?alt=media&token=4f3a7b0d-7c87-443f-9e1d-4222f8d22bb9",
                    Name = user.displayName ?: "Temp Name",
                    RequestSent = listOf(),
                    Requests = listOf(),
                    Blocks = listOf(),
                    Friends = listOf(),
                    Groups = listOf(),
                    Devices = listOf(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
                )
                userRef.set(newUser).addOnSuccessListener {
                    callback(newUser)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                val existingUser = document.toObject(User::class.java)
                if (existingUser != null) {
                    callback(existingUser)
                }
            }
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
