package com.example.doanmess.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.doanmess.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class NewPost : HandleOnlineActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var backBtn :ImageButton
    private lateinit var chooseMediaButton: Button
    private lateinit var uploadPostButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var videoPreview: PlayerView
    private lateinit var backgound: ImageView
    private var selectedMediaUri: Uri? = null
    private var audioPlayer : ExoPlayer? =null
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
        val intent = Intent(Intent.ACTION_GET_CONTENT)
       // intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf("image/*", "video/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, REQUEST_CODE_PICK_MEDIA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_MEDIA && resultCode == Activity.RESULT_OK) {
            selectedMediaUri = data?.data
            if (selectedMediaUri != null) {
                val fileSize = contentResolver.openFileDescriptor(selectedMediaUri!!, "r")?.statSize ?: 0
                if (fileSize > 15 * 1024 * 1024) { // 15 MB in bytes
                    Toast.makeText(this, "File size exceeds 15MB", Toast.LENGTH_SHORT).show()
                    return
                }
                else{
                    val uri = selectedMediaUri!!
                    val mimeType = contentResolver.getType(uri)
                    Log.d("NewPost", "Selected media: $uri, MIME type: $mimeType")
                    if (mimeType?.startsWith("video/") == true) {
                        imagePreview.visibility = ImageView.GONE
                        videoPreview.visibility = VideoView.VISIBLE
                        audioPlayer= ExoPlayer.Builder(videoPreview.context).build()
                        val mediaItem = MediaItem.fromUri(uri)
                        audioPlayer?.setMediaItem(mediaItem)
                        videoPreview.player = audioPlayer
                        audioPlayer?.prepare()
                    }
                    else if (mimeType?.startsWith("image/") == true) {
                        backgound.visibility = ImageView.VISIBLE
                        imagePreview.setImageURI(uri)
                        imagePreview.visibility = ImageView.VISIBLE
                        videoPreview.visibility = VideoView.GONE
                    }else {
                        // Handle other MIME types or show an error message
                        Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioPlayer?.release()
    }
    private fun uploadPost() {
        uploadPostButton.text = "Uploading..."
        uploadPostButton.isEnabled = false
        val title = titleEditText.text.toString()
        val mediaUri = selectedMediaUri
        Log.d("NewPost", "Uploading post with title: $title and media URI: $mediaUri")
        if (title.isNotEmpty() || mediaUri != null) {
            val uid = intent.getStringExtra("uid") ?: return
            val postId = UUID.randomUUID().toString()
            if(mediaUri != null){
                uploadMediaToFirebase(mediaUri!!, postId,title)
            }
            else{
                val post = hashMapOf<String, Any>(
                    "uid" to uid,
                    "title" to title,
                    "mediaFile" to "",
                    "type" to "",
                    "time" to System.currentTimeMillis(),
                    "likes" to 0,
                    "comments" to 0
                )
                uploadToFiresStore(post,postId,uid)
            }
        } else {
            Toast.makeText(this, "Please enter a title or select a media file", Toast.LENGTH_SHORT).show()
            uploadPostButton.isEnabled = true
            uploadPostButton.text = "UPLOAD POST"
        }
    }
    fun compressImage(fileUri: Uri, context: Context): ByteArray {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Adjust quality as needed
        return outputStream.toByteArray()
    }
    private fun uploadMediaToFirebase(fileUri: Uri, postId: String, title : String) {
        // Xác định loại MIME của tệp
        val uid = intent.getStringExtra("uid") ?: return
        val mimeType = contentResolver.getType(fileUri)
        val storageRef = FirebaseStorage.getInstance().reference.child("posts").child(postId)
        val uploadTask = if (mimeType?.startsWith("image/") == true) {
            val compressedImage = compressImage(fileUri, this)
            Log.d("NewPost", "Compressed image size: ${compressedImage.size}")
            storageRef.putBytes(compressedImage)
        } else {
            Log.d("NewPost", "Uploading video")
            storageRef.putFile(fileUri)
        }
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val uploadRef = uri.toString()
                val mimeType = contentResolver.getType(fileUri)!!
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
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
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