package com.example.doanmess.activities

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import com.example.doanmess.R

class MapsActivity  : HandleOnlineActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_maps)

        //get the web view
        val webView = findViewById<WebView>(R.id.webView)

        val messageContent = intent.getStringExtra("content")
        // messageContent: "Location: https://maps.google.com/?q=37.4220936,-122.083922"

        //parse to get the url
        val url = messageContent?.split(" ")?.get(1)

        //load the url
        webView.loadUrl(url!!)
    }
}