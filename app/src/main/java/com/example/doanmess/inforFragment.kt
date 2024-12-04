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
import android.provider.Settings
import android.util.Log
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
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage



class inforFragment : Fragment() {
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imageView: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var userId: String
    private lateinit var friendReqFrame : FrameLayout
    private lateinit var blockListFrame : FrameLayout
    private var dbfirestore = Firebase.firestore


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_infor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thay userId bằng ID người dùng Firebase của bạn
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        val darkMode = view.findViewById<FrameLayout>(R.id.darkMode)
        val changeAvata = view.findViewById<FrameLayout>(R.id.changeAvata)
        val changeName = view.findViewById<FrameLayout>(R.id.changeName)
        val txtName = view.findViewById<TextView>(R.id.txtName)
        val txtMode = view.findViewById<TextView>(R.id.txtCheckDarkMode)
        val logOutBtn = view.findViewById<FrameLayout>(R.id.logout)
        val requestNumbers = view.findViewById<TextView>(R.id.txtNumberRequest)
        val blockNumbers = view.findViewById<TextView>(R.id.txtNumberBlock)

        friendReqFrame = view.findViewById(R.id.friendRequest)
        blockListFrame = view.findViewById(R.id.blockList)
        imageView = view.findViewById(R.id.imgView)
        button = view.findViewById(R.id.floatingActionButton)

        loadUserData(requestNumbers, blockNumbers)


        val auth1 = FirebaseAuth.getInstance()
        val currentUser = auth1.currentUser


        logOutBtn.setOnClickListener {
            val dir = requireContext().filesDir
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        try {
                            file.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            val docRef = dbfirestore.collection("users").document(currentUser!!.uid)
            //xóa 1 phần tử trong mảng field của firestore
            val androidId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)

            docRef.update("Devices", FieldValue.arrayRemove(androidId))
                .addOnSuccessListener {
                    Log.d("thanhhhhhhcoooong", "Phần tử đã được xóa thành công khỏi mảng")
                }
                .addOnFailureListener { e ->
                    Log.d("xxxxxxxxxxxxxxxx", "Loi xoa phan tu", e)
                }

            val docRef2 = dbfirestore.collection("devices").document(androidId.toString())
            docRef2.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {

                        docRef2.update("User_id", "")
                            .addOnSuccessListener {
                                Log.d("TAG", "Trường User_id đã được ghi đè thành công")
                                auth1.signOut()
                                val intent = Intent(requireContext(), Login::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Lỗi khi ghi đè trường User_id", e)
                            }
                    } else {
                        docRef2.set(hashMapOf("Token" to "", "User_id" to ""))
                            .addOnSuccessListener {
                                Log.d("TAG", "DocumentSnapshot successfully updated!")
                                auth1.signOut()
                                val intent = Intent(requireContext(), Login::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("TAG", "Error updating document", e)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }

        }

        blockListFrame.setOnClickListener {
            val intent = Intent(context, Block::class.java)
            startActivity(intent)
        }

        friendReqFrame.setOnClickListener {
            val intent = Intent(context, FriendRequest::class.java)
            startActivity(intent)
        }
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

    private fun loadUserData(requestNumbers: TextView, blockNumbers: TextView) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Get the "Requests" and "Blocks" fields
                val requests = document["Requests"] as? List<*>
                val blocks = document["Blocks"] as? List<*>

                // Set the counts to the TextViews
                requestNumbers.text = requests?.size.toString()
                blockNumbers.text = blocks?.size.toString()

                // Other data loading...
            } else {
                // Handle the case where the document does not exist
                requestNumbers.text = "0"
                blockNumbers.text = "0"
            }
        }.addOnFailureListener {
            // Handle the error
            requestNumbers.text = "0"
            blockNumbers.text = "0"
        }
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
        // Can I choose video instead of image? Show me how to do it.



    }
    private fun chooseMedia(){
        // Chọn hình ảnh hoặc video từ thư viện
        ImagePicker.with(this)
            .galleryOnly()
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
            .update("Name", name)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
    }

    private fun saveDarkMode(mode: String) {
        firestore.collection("users").document(userId)
            .update("DarkMode", mode)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
        // Lưu giá trị chế độ Dark Mode (On hoặc Off)
        val isDarkMode = mode == "On"
        // Cập nhật chế độ hiển thị ngay lập tức
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun saveImageUri(uri: String) {
        firestore.collection("users").document(userId)
            .update("Avatar", uri)
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu cập nhật thất bại
            }
    }

    private fun loadUserData() {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Tài liệu của người dùng tồn tại, tải dữ liệu từ Firestore
                val savedName = document.getString("Name") ?: "User"
                val savedMode = document.getString("DarkMode") ?: "Off"
                val savedImageUri = document.getString("Avatar")

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
