package com.example.doanmess

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        // Xác định nút btnBack
        val btnBack = findViewById<Button>(R.id.btnBack)
        val sharedPreferences = getSharedPreferences("LanguagePref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Danh sách ngôn ngữ
        val languages = listOf(
            Pair("vi", "Tiếng Việt"),
            Pair("en", "English")
        )

        // Hiển thị danh sách ngôn ngữ
        val listView = findViewById<ListView>(R.id.languageListView)
        val adapter = LanguageAdapter(this, languages)
        listView.adapter = adapter

        // Xử lý khi người dùng chọn ngôn ngữ
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languages[position].first
            AlertDialog.Builder(this)
                .setTitle("Change Language")
                .setMessage("Do you want to switch to ${languages[position].second}?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Người dùng đồng ý thay đổi ngôn ngữ
                    editor.putString("language", selectedLanguage).apply()

                    // Cập nhật ngôn ngữ
                    setLocale(selectedLanguage)

                    // Làm mới lại Activity chính
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, which ->
                    // Đóng hộp thoại nếu người dùng không đồng ý
                    dialog.dismiss()
                }
                .create().apply {
                    // Thiết lập background cho dialog (nếu có)
                    window?.setBackgroundDrawableResource(R.drawable.background_dialog_delete)
                }
                .show()
        }

        btnBack.setOnClickListener {
            finish() // Kết thúc Activity hiện tại để quay lại Activity trước đó
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }


}
