package com.example.doanmess

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class inforFragment : Fragment() {
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imageView: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var userId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_infor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thay userId bằng ID người dùng Firebase của bạn
        userId = "USER_ID"

        val darkMode = view.findViewById<FrameLayout>(R.id.darkMode)
        val changeAvata = view.findViewById<FrameLayout>(R.id.changeAvata)
        val changeName = view.findViewById<FrameLayout>(R.id.changeName)
        val txtName = view.findViewById<TextView>(R.id.txtName)
        val txtMode = view.findViewById<TextView>(R.id.txtCheckDarkMode)

        imageView = view.findViewById(R.id.imgView)
        button = view.findViewById(R.id.floatingActionButton)

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri? = data?.data
                imageView.setImageURI(uri)
                uri?.let { uploadImage(it) }
            }
        }

        button.setOnClickListener { changeAvata() }

        darkMode.setOnClickListener {
            changeBackgroundColor(darkMode, "#D9D9D9", 150)
            toggleDarkMode(txtMode)
        }

        changeAvata.setOnClickListener {
            changeBackgroundColor(changeAvata, "#D9D9D9", 150)
            changeAvata()
        }

        changeName.setOnClickListener {
            changeBackgroundColor(changeName, "#D9D9D9", 150)
            changeNameFunc(view)
        }

        txtName.setOnClickListener { changeNameFunc(view) }

        // Tải thông tin từ Firestore khi khởi động
        loadUserData()
    }

    private fun changeNameFunc(view: View) {
        val edtChangeName = view.findViewById<EditText>(R.id.edtChangeName)
        val txtName = view.findViewById<TextView>(R.id.txtName)

        edtChangeName.setText(txtName.text.toString())
        edtChangeName.setSelection(edtChangeName.text.length)

        txtName.visibility = View.INVISIBLE
        edtChangeName.visibility = View.VISIBLE
        edtChangeName.requestFocus()

        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtChangeName, InputMethodManager.SHOW_IMPLICIT)

        edtChangeName.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                txtName.text = edtChangeName.text.toString()
                edtChangeName.visibility = View.INVISIBLE
                txtName.visibility = View.VISIBLE

                saveUserName(txtName.text.toString())
                imm.hideSoftInputFromWindow(edtChangeName.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun toggleDarkMode(txtMode: TextView) {
        txtMode.text = if (txtMode.text.toString() == "Off") "On" else "Off"
        saveDarkMode(txtMode.text.toString())
    }

    private fun changeAvata() {
        // Gọi ImagePicker để chọn hình ảnh
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> imagePickerLauncher.launch(intent) }
    }

    private fun changeBackgroundColor(view: View, color: String, duration: Long) {
        val background = view.background
        val currentColor = if (background is ColorDrawable) background.color else Color.TRANSPARENT

        view.setBackgroundColor(Color.parseColor(color))
        view.postDelayed({ view.setBackgroundColor(currentColor) }, duration)
    }

    private fun uploadImage(uri: Uri) {
        val avatarRef = storageReference.child("avatars/$userId.jpg")
        avatarRef.putFile(uri).addOnSuccessListener {
            avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveImageUri(downloadUri.toString())
            }
        }.addOnFailureListener {
            // Xử lý lỗi tải ảnh
        }
    }

    private fun saveUserName(name: String) {
        firestore.collection("users").document(userId)
            .update("name", name)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
    }

    private fun saveDarkMode(mode: String) {
        firestore.collection("users").document(userId)
            .update("darkMode", mode)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
    }

    private fun saveImageUri(uri: String) {
        firestore.collection("users").document(userId)
            .update("imageUri", uri)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
    }

    private fun loadUserData() {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Tài liệu của người dùng tồn tại, tải dữ liệu từ Firestore
                val savedName = document.getString("name") ?: "User"
                val savedMode = document.getString("darkMode") ?: "Off"
                val savedImageUri = document.getString("imageUri")

                view?.findViewById<TextView>(R.id.txtName)?.text = savedName
                view?.findViewById<TextView>(R.id.txtCheckDarkMode)?.text = savedMode
                // Sử dụng Glide để tải ảnh từ URL
                savedImageUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(imageView)
                }
            } else {
                // Nếu tài liệu không tồn tại, tạo tài liệu mới cho người dùng với các giá trị mặc định
                val newUser = hashMapOf(
                    "name" to "User",
                    "darkMode" to "Off",
                    "imageUri" to null // Có thể để null hoặc giá trị mặc định nếu có
                )
                userDocRef.set(newUser).addOnSuccessListener {
                    // Tài liệu mới được tạo thành công
                    view?.findViewById<TextView>(R.id.txtName)?.text = "User"
                    view?.findViewById<TextView>(R.id.txtCheckDarkMode)?.text = "Off"
                }.addOnFailureListener { e ->
                    // Xử lý lỗi nếu không thể tạo tài liệu
                }
            }
        }.addOnFailureListener {
            // Xử lý lỗi nếu không thể tải dữ liệu từ Firestore
        }
    }
}
