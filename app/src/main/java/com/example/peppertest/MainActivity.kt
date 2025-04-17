package com.example.peppertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val serverUrl = "http://10.22.9.224:3000/health"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Start the API request as soon as the activity is created
        fetchHealthEndpoint()
    }
    
    private fun fetchHealthEndpoint() {
        progressBar.visibility = View.VISIBLE
        
        val request = Request.Builder()
            .url(serverUrl)
            .build()
            
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    responseTextView.text = "Error: ${e.message}"
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string() ?: "No response body"
                
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    responseTextView.text = responseText
                }
            }
        })
    }
}