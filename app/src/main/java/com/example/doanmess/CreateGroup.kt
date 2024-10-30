package com.example.doanmess

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.createuiproject.MainChat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreateGroup : AppCompatActivity() {
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
    var imageUri: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        enableEdgeToEdge()

        originAdd.add(GroupAdd("vwAUzgbCSNWNq4a48xoM2zZVCcH3", "Group 3", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("c33ebNdc6rStVchv3ovFalNOxDh2", "Group 4", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("5sOWGPgonbafPOPh2weIwvcP0wK2", "Group 5", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("6", "Group 1", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("7", "Group 2", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("8", "Group 3", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("9", "Group 4", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("10", "Group 5", R.drawable.avatar_placeholder_allchat, false))
        add = originAdd.toMutableList()

        addRv = findViewById(R.id.rvAdd)
        addedRv = findViewById(R.id.rvAdded)

        adapterAdd = GroupAddAdapter(add) { id, name ->
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
        cancelBtn.setOnClickListener({
            val i = Intent(this, Home::class.java)
            startActivity(i)
        })

        createBtn.setOnClickListener({
            createGroup()
        })
        searchBtn.setOnClickListener({
            if (filterSearch.text.isEmpty()) {
                add = originAdd.toMutableList()
            } else {
                val filterLowerCase = filterSearch.text.toString().toLowerCase()
                add = originAdd.filter { it.name.toLowerCase().contains(filterLowerCase) }
                    .toMutableList()
            }
            adapterAdd.changeList(add)

        })
        addBtn.setOnClickListener({
            changeAvata()
        })
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
        // Upload imageUri to Firebase Storage
        val storage = FirebaseStorage.getInstance()
        val avatarRef = storage.reference.child("group_avatars/${UUID.randomUUID()}.jpg")
        val uri = Uri.parse(imageUri)
        // Add group details to Firestore
        avatarRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
            avatarRef.downloadUrl.addOnSuccessListener { uri ->
                val avatarUrl = uri.toString()

                // Add group details to Firestore
                val group = hashMapOf(
                    "name" to groupName,
                    "avatar" to avatarUrl,
                    "members" to added.map { it.id }
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
                            val intent = Intent(this, MainChat::class.java)
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
}
/*    private fun updateSpaceViewVisibility() {
        val spaceView = findViewById<View>(R.id.spaceView)
        spaceView.visibility = if (added.isEmpty()) View.GONE else View.VISIBLE
    }*/
