package com.example.doanmess.fragments

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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.example.doanmess.activities.LanguageSelectionActivity
import com.example.doanmess.activities.Login
import com.example.doanmess.MessageSQLDatabase
import com.example.doanmess.R
import com.example.doanmess.activities.Block
import com.example.doanmess.activities.FriendRequest
import com.example.doanmess.activities.Home
import com.example.doanmess.activities.friendList
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale


class inforFragment : Fragment() {
    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imageView: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var userId: String
    private lateinit var friendReqFrame : FrameLayout
    private lateinit var friendListFrame : FrameLayout
    private lateinit var blockListFrame : FrameLayout
    private lateinit var changeLanguage : FrameLayout
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
        val sharedPreferences = activity?.getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)


        friendReqFrame = view.findViewById(R.id.friendRequest)
        friendListFrame = view.findViewById(R.id.friendList)
        blockListFrame = view.findViewById(R.id.blockList)
        changeLanguage = view.findViewById(R.id.changeLanguage)
        imageView = view.findViewById(R.id.imgView)
        button = view.findViewById(R.id.floatingActionButton)
        val requestNumbers = view.findViewById<TextView>(R.id.txtNumberRequest)
        val blockNumbers = view.findViewById<TextView>(R.id.txtNumberBlock)
        val friendsNumber = view.findViewById<TextView>(R.id.txtFriendList)
        loadUserData(requestNumbers, blockNumbers, friendsNumber)


        val auth1 = FirebaseAuth.getInstance()
        val currentUser = auth1.currentUser


        logOutBtn.setOnClickListener {
            val messageSQLDatabase = MessageSQLDatabase.getInstance(requireContext())
            messageSQLDatabase.messageDao().deleteAllMessages()
            messageSQLDatabase.messageGroupDao().deleteAllGroupMessages()

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

        changeLanguage.setOnClickListener {
            val intent = Intent(activity, LanguageSelectionActivity::class.java)
            startActivity(intent)
        }

        blockListFrame.setOnClickListener {
            val intent = Intent(context, Block::class.java)
            startActivity(intent)
        }

        friendReqFrame.setOnClickListener {
            val intent = Intent(context, FriendRequest::class.java)
            startActivity(intent)
        }

        friendListFrame.setOnClickListener {
            val intent = Intent(context, friendList::class.java)
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

    override fun onResume() {
        super.onResume()

        // Kiểm tra nếu view không null
        view?.let {
            val requestNumbers = it.findViewById<TextView>(R.id.txtNumberRequest)
            val blockNumbers = it.findViewById<TextView>(R.id.txtNumberBlock)
            val friendsNumber = it.findViewById<TextView>(R.id.txtFriendList)
            loadUserData(requestNumbers, blockNumbers, friendsNumber)
        }
    }



    private fun loadUserData(requestNumbers: TextView, blockNumbers: TextView, friendsNumber: TextView) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Get the "Requests" and "Blocks" fields
                val requests = document["Requests"] as? List<*>
                val blocks = document["Blocks"] as? List<*>
                val friends = document["Friends"] as? List<*>

                // Set the counts to the TextViews
                requestNumbers.text = requests?.size.toString()
                blockNumbers.text = blocks?.size.toString()
                friendsNumber.text = friends?.size.toString()

                // Other data loading...
            } else {
                // Handle the case where the document does not exist
                requestNumbers.text = "0"
                blockNumbers.text = "0"
                friendsNumber.text = "0"
            }
        }.addOnFailureListener {
            // Handle the error
            requestNumbers.text = "0"
            blockNumbers.text = "0"
            friendsNumber.text = "0"
        }
    }

    private fun changeNameFunc(view: View) {
        val edtChangeName = view.findViewById<EditText>(R.id.edtChangeName)
        val txtNameFragment = view.findViewById<TextView>(R.id.txtName)

        // Truy cập TextView trong Activity
        val parentActivity = activity as? Home
        val txtNameActivity = parentActivity?.findViewById<TextView>(R.id.txtName)

        // Đồng bộ EditText với TextView (ưu tiên Fragment trước)
        txtNameFragment?.let {
            edtChangeName.setText(it.text.toString())
        }
        edtChangeName.setSelection(edtChangeName.text.length)

        // Hiển thị EditText và ẩn TextView trong Fragment
        txtNameFragment.visibility = View.INVISIBLE
        edtChangeName.visibility = View.VISIBLE
        edtChangeName.requestFocus()

        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edtChangeName, InputMethodManager.SHOW_IMPLICIT)

        edtChangeName.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val newName = edtChangeName.text.toString()

                // Cập nhật TextView trong Fragment
                txtNameFragment.text = newName
                txtNameFragment.visibility = View.VISIBLE

                // Cập nhật TextView trong Activity
                txtNameActivity?.text = newName

                edtChangeName.visibility = View.INVISIBLE

                // Gọi hàm lưu tên người dùng
                saveUserName(newName)

                // Ẩn bàn phím
                imm.hideSoftInputFromWindow(edtChangeName.windowToken, 0)
                true
            } else {
                false
            }
        }
    }


    private fun toggleDarkMode(txtMode: TextView) {
        val currentMode = txtMode.text.toString()

        // Tạo hộp thoại xác nhận
        AlertDialog.Builder(requireContext())
            .setTitle("Dark Mode")
            .setMessage("Do you want to turn ${if (currentMode == "Off") "On" else "Off"} Dark Mode?")
            .setPositiveButton("Yes") { _, _ ->
                // Thay đổi chế độ Dark Mode
                txtMode.text = if (currentMode == "Off") "On" else "Off"
                saveDarkMode(txtMode.text.toString())
            }
            .setNegativeButton("No") { dialog, _ ->
                // Đóng hộp thoại
                dialog.dismiss()
            }
            .create().apply {
                // Thiết lập background cho dialog (tùy chọn)
                window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
            }
            .show()
    }

    private fun changeAvata() {
        // Gọi ImagePicker để chọn hình ảnh
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent -> imagePickerLauncher.launch(intent) }


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

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Cập nhật lại resources với ngữ cảnh mới
        resources.updateConfiguration(config, resources.displayMetrics)
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
