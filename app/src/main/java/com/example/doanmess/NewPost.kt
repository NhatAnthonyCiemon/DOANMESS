package com.example.doanmess

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class NewPost : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var backBtn :ImageButton
    private lateinit var chooseMediaButton: Button
    private lateinit var uploadPostButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var videoPreview: VideoView
    private lateinit var backgound: ImageView
    private var selectedMediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        backgound = findViewById(R.id.background)
        titleEditText = findViewById(R.id.titleEditText)
        chooseMediaButton = findViewById(R.id.chooseMediaButton)
        uploadPostButton = findViewById(R.id.uploadPostButton)
        imagePreview = findViewById(R.id.imagePreview)
        videoPreview = findViewById(R.id.videoPreview)
        backBtn = findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            finish()
        }
        chooseMediaButton.setOnClickListener {
            chooseMedia()
        }

        uploadPostButton.setOnClickListener {
            uploadPost()
        }
    }

    private fun chooseMedia() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf("image/*", "video/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_MEDIA && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedMediaUri = uri
                val mimeType = contentResolver.getType(uri)
                Log.d("NewPost", "Selected media: $uri, MIME type: $mimeType")
           //     backgound.visibility = ImageView.VISIBLE
                if (mimeType?.startsWith("video/") == true) {
                    videoPreview.setVideoURI(uri)
                    videoPreview.visibility = VideoView.VISIBLE
                    videoPreview.setOnPreparedListener {
                        val mediaController = MediaController(this)
                        videoPreview.setMediaController(mediaController)
                        videoPreview.requestFocus()
                        mediaController.setAnchorView(videoPreview)
                        videoPreview.start()
                    }
                    imagePreview.visibility = ImageView.GONE
                } else if (mimeType?.startsWith("image/") == true) {
                    imagePreview.setImageURI(uri)
                    imagePreview.visibility = ImageView.VISIBLE
                    videoPreview.visibility = VideoView.GONE
                }
            }
        }
    }

    private fun uploadPost() {
        uploadPostButton.isEnabled = false
        val title = titleEditText.text.toString()
        val mediaUri = selectedMediaUri
        Log.d("NewPost", "Uploading post with title: $title and media URI: $mediaUri")
        if (title.isNotEmpty() || mediaUri != null) {
            val uid = intent.getStringExtra("uid") ?: return
            val postId = UUID.randomUUID().toString()
            if(mediaUri != null) {
                FirebaseStorage.getInstance().reference.child("posts").child(postId)
                    .putFile(mediaUri!!).addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val uploadRef = uri.toString()
                        val mimeType = contentResolver.getType(mediaUri)!!
                        val post = hashMapOf<String, Any>(
                            "uid" to uid,
                            "title" to title,
                            "mediaFile" to uploadRef,
                            "type" to mimeType,
                            "time" to System.currentTimeMillis(),
                            "likes" to 0
                        )
                        uploadToFiresStore(post,postId,uid)
                    }
                }
            }
            else{
                val post = hashMapOf<String, Any>(
                    "uid" to uid,
                    "title" to title,
                    "mediaFile" to "",
                    "type" to "",
                    "time" to System.currentTimeMillis(),
                    "likes" to 0
                )
                uploadToFiresStore(post,postId,uid)
            }
        } else {
            Toast.makeText(this, "Please enter a title or select a media file", Toast.LENGTH_SHORT).show()
            uploadPostButton.isEnabled = true
        }
    }

    private fun uploadToFiresStore(post: HashMap<String, Any>, postId: String, uid: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts")
            .document(postId)
            .set(post)
            .addOnSuccessListener {
                // Show a success message
                Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                uploadPostButton.isEnabled = true
                // Handle the error
                Log.e("NewPost", "Error uploading post", e)
            }
    }

    companion object {
        private const val REQUEST_CODE_PICK_MEDIA = 1
    }
}