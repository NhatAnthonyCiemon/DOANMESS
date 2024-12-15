package com.example.doanmess


import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class CreateGroup : HandleOnlineActivity() {
    private lateinit var addRv: RecyclerView
    private lateinit var addedRv: RecyclerView
    private lateinit var adapterAdd: GroupAddAdapter
    private lateinit var adapterAdded: GroupAddedAdapter
    private lateinit var filterSearch: EditText
    private lateinit var searchBtn: ImageButton
    private lateinit var addBtn : ImageButton
    private lateinit var cancelBtn: Button
    private lateinit var createBtn: Button
    private lateinit var groupNameText: EditText
    private lateinit var groupImage : ImageView
    private var originAdd = mutableListOf<GroupAdd>()
    private var add = mutableListOf<GroupAdd>()
    private val added = mutableListOf<GroupAdded>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    val db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser
    var imageUri: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0 , systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()


        addRv = findViewById(R.id.rvAdd)
        addedRv = findViewById(R.id.rvAdded)

        adapterAdd = GroupAddAdapter(this,add) { id, name ->
            added.add(GroupAdded(name, id))
            adapterAdded.notifyItemInserted(added.size - 1)
        }

        adapterAdded = GroupAddedAdapter(added) { id ->
            originAdd[originAdd.indexOfFirst { it.id == id }].added = false
            val positon = add.indexOfFirst { it.id == id }
            if (positon != -1) {
                add[positon].added = false
                adapterAdd.notifyItemChanged(positon)
            }
            added.removeAt(added.indexOfFirst { it.id == id })

        }
        lifecycleScope.launch {
            fetchFriendsDetails()
        }
        addRv.adapter = adapterAdd
        addRv.layoutManager = LinearLayoutManager(this)

        addedRv.adapter = adapterAdded
        addedRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        filterSearch = findViewById(R.id.filter_search)
        searchBtn = findViewById(R.id.search_btn)
        cancelBtn = findViewById(R.id.cancelBtn)
        createBtn = findViewById(R.id.createBtn)
        groupNameText = findViewById(R.id.groupName)
        addBtn = findViewById(R.id.addImg)
        groupImage = findViewById(R.id.groupImage)
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri? = data?.data
                groupImage.setImageURI(uri)
                // Lưu URI của hình ảnh vào SharedPreferences
                imageUri = uri.toString()
            }
        }
        cancelBtn.setOnClickListener{
            finish()
        }

        createBtn.setOnClickListener{
            createGroup()
        }
        searchBtn.setOnClickListener{
            if (filterSearch.text.isEmpty()) {
                add = originAdd.toMutableList()
            } else {
                val filterLowerCase = filterSearch.text.toString().toLowerCase()
                add = originAdd.filter { it.name.toLowerCase().contains(filterLowerCase) }
                    .toMutableList()
            }
            adapterAdd.changeList(add)

        }
        filterSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filter = s.toString()
                if (filter.isEmpty()) {
                    add = originAdd.toMutableList()
                } else {
                    val filterLowerCase = filterSearch.text.toString().toLowerCase()
                    add = originAdd.filter { it.name.toLowerCase().contains(filterLowerCase) }
                        .toMutableList()
                }
                adapterAdd.changeList(add)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        addBtn.setOnClickListener{
            changeAvata()
        }



    }

    private fun changeAvata() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                imagePickerLauncher.launch(intent)
            }
    }
    private fun createGroup() {
        val groupName = groupNameText.text.toString().trim()
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
            return
        }
        if(imageUri == null){
            Toast.makeText(this, "Please choose a group avatar", Toast.LENGTH_SHORT).show()
            return
        }
        createBtn.isEnabled = false
        cancelBtn.isEnabled = false

        // Upload imageUri to Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val avatarRef = storage.reference.child("avatars/${UUID.randomUUID()}.jpg")
        val uri = Uri.parse(imageUri)
        // Add group details to Firestore
        avatarRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                val avatarUrl = uri.toString()

                // Add group details to Firestore
                added.add(GroupAdded("You", currentUser!!.uid))
                val group = hashMapOf(
                    "Name" to groupName,
                    "Avatar" to avatarUrl,
                    "Participants" to added.map { it.id }
                )
                db.collection("groups").add(group).addOnSuccessListener { documentReference ->
                    val groupId = documentReference.id
                    // Add group ID to each user
                    val batch = db.batch()
                    try{
                        added.forEach { member ->
                            val userRef = db.collection("users").document(member.id)
                            batch.update(userRef, "Groups", FieldValue.arrayUnion(groupId))
                        }
                        batch.commit().addOnSuccessListener {
                            Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show()
                            MessageController().newCreateGroup(groupId, currentUser!!.uid)
                            val intent = Intent(this, Home::class.java)
                            intent.putExtra("chatId", groupId)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to update users", Toast.LENGTH_SHORT).show()

                        }
                    }catch (e: Exception){
                        Toast.makeText(this, "Failed to update users", Toast.LENGTH_SHORT).show()
                    }

                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun fetchFriendsDetails() {
        val userRef = db.collection("users").document(currentUser!!.uid)
        val documentSnapshot = userRef.get().await()
        if (documentSnapshot.exists()) {
            val friendsIds = documentSnapshot.get("Friends") as? List<String> ?: emptyList()

            friendsIds.forEach { friendId ->
                fetchUserDetails(friendId)
            }
        }
        add = originAdd.toMutableList()
        adapterAdd.changeList(add)
    }
    private suspend fun fetchUserDetails(userId: String) {
        val userRef = db.collection("users").document(userId)
        val documentSnapshot = userRef.get().await()
        if (documentSnapshot.exists()) {
         //   val avatarUrl = documentSnapshot.getString("Avatar") ?: ""
            val name = documentSnapshot.getString("Name") ?: ""
            val avatarUrl = documentSnapshot.getString("Avatar") ?: ""
            // Set the drawable to the ImageView
            val groupAdd = GroupAdd(userId, name, avatarUrl, false)
            originAdd.add(groupAdd)
        }
    }
}
/*    private fun updateSpaceViewVisibility() {
        val spaceView = findViewById<View>(R.id.spaceView)
        spaceView.visibility = if (added.isEmpty()) View.GONE else View.VISIBLE
    }*/
