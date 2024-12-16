package com.example.doanmess

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
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
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, languages.map { it.second })
        listView.adapter = adapter

        // Xử lý khi người dùng chọn ngôn ngữ
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languages[position].first
            editor.putString("language", selectedLanguage).apply()

            // Cập nhật ngôn ngữ
            setLocale(selectedLanguage)

            // Làm mới lại Activity chính
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
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
