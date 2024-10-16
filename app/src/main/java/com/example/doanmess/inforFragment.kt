package com.example.doanmess

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton

class inforFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_infor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE)

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
                // Lưu URI của hình ảnh vào SharedPreferences
                saveImageUri(uri.toString())
            }
        }

        button.setOnClickListener {
            changeAvata()
        }

        darkMode.setOnClickListener {
            changeBackgroundColor(darkMode, "#D9D9D9", 150)
            changeMode(view)
        }

        changeAvata.setOnClickListener {
            changeBackgroundColor(changeAvata, "#D9D9D9", 150)
            changeAvata()
        }

        changeName.setOnClickListener {
            changeBackgroundColor(changeName, "#D9D9D9", 150)
            changeNameFunc(view)
        }

        txtName.setOnClickListener {
            changeNameFunc(view)
        }

        // Đọc và hiển thị thông tin khi khởi động
        loadData()
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

                // Lưu tên người dùng
                saveData(txtName.text.toString())

                imm.hideSoftInputFromWindow(edtChangeName.windowToken, 0)

                true
            } else {
                false
            }
        }
    }

    private fun changeMode(view: View) {
        val txtMode = view.findViewById<TextView>(R.id.txtCheckDarkMode)
        if (txtMode.text.toString() == "Off") {
            txtMode.text = "On"
        } else {
            txtMode.text = "Off"
        }
        // Lưu trạng thái chế độ tối
        saveMode(txtMode.text.toString())
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

    private fun changeBackgroundColor(view: View, color: String, duration: Long) {
        val background = view.background
        val currentColor = if (background is ColorDrawable) background.color else Color.TRANSPARENT

        view.setBackgroundColor(Color.parseColor(color))
        view.postDelayed({ view.setBackgroundColor(currentColor) }, duration)
    }

    // Hàm lưu trạng thái chế độ tối
    private fun saveMode(mode: String) {
        sharedPreferences.edit().putString("DarkMode", mode).apply()
    }

    // Hàm lưu tên người dùng
    private fun saveData(name: String) {
        sharedPreferences.edit().putString("UserName", name).apply()
    }

    // Hàm lưu URI của hình ảnh
    private fun saveImageUri(uri: String) {
        sharedPreferences.edit().putString("ImageUri", uri).apply()
    }

    // Hàm đọc và hiển thị thông tin
    private fun loadData() {
        val savedName = sharedPreferences.getString("UserName", "User")
        val savedMode = sharedPreferences.getString("DarkMode", "Off")
        val savedImageUri = sharedPreferences.getString("ImageUri", null)

        view?.findViewById<TextView>(R.id.txtName)?.text = savedName
        view?.findViewById<TextView>(R.id.txtCheckDarkMode)?.text = savedMode

        // Hiển thị hình ảnh nếu có URI đã lưu
        savedImageUri?.let {
            imageView.setImageURI(Uri.parse(it))
        }
    }
}
