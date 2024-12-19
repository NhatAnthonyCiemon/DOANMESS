package com.example.doanmess

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class LanguageAdapter(
    context: Context,
    private val languages: List<Pair<String, String>>
) : ArrayAdapter<Pair<String, String>>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_language, parent, false)

        val language = languages[position]
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val languageTextView = view.findViewById<TextView>(R.id.languageTextView)

        // Gán dữ liệu vào View
        languageTextView.text = language.second
        // Bạn có thể thay đổi icon theo ngôn ngữ
        iconImageView.setImageResource(if (language.first == "vi") R.drawable.ic_flag_vietnam else R.drawable.ic_flag_english)

        return view
    }
}
