package com.example.createuiproject

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doanmess.InforChat
import com.example.doanmess.R

class MainChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_chat)
        val chatMessages = listOf(
            ChatMessage("Hello, how are you?", false),
            ChatMessage("I'm good, thank you!", true),
            ChatMessage("What's the cost for the app design?", false),
            ChatMessage("It will be 3000$", true),
            ChatMessage("Alright, let me think about it.", false) ,
        )

        val chatAdapter = ChatAdapter(chatMessages)
        val recyclerViewMessages = findViewById<RecyclerView>(R.id.main_chat_recycler)
        recyclerViewMessages.adapter = chatAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(this)

        // Get the display metrics
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Set RecyclerView height to a percentage of the screen height (e.g., 70%)
        val layoutParams = recyclerViewMessages.layoutParams
        layoutParams.height = (screenHeight * 0.75).toInt() // Change the percentage as needed
        recyclerViewMessages.layoutParams = layoutParams

        //set the input bar width to 70%
        val inputBar = findViewById<LinearLayout>(R.id.input_bar)
        val inputBarLayoutParams = inputBar.layoutParams
        inputBarLayoutParams.width = (screenHeight * 0.70).toInt()
        inputBar.layoutParams = inputBarLayoutParams

        // set on click listener for the back button to navigate back to the home activity
        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }

        //set on click listener for the info button to navigate to the user info activity
        findViewById<ImageButton>(R.id.info_button).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            startActivity(intent)
        }

        //set on click listener for the name_layout to navigate to the user infor activity
        findViewById<LinearLayout>(R.id.name_layout).setOnClickListener {
            // Navigate to user info activity
            val intent = Intent(this, InforChat::class.java)
            startActivity(intent)
        }

        // set on click listener for the mic button to start voice recording
        findViewById<ImageView>(R.id.mic_button).setOnClickListener {
            // Start voice recording
        }
    }
}