package com.example.peppertest

import android.os.Bundle
import android.util.Log
import android.view.View
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    private val client = OkHttpClient()
    private val serverUrl = "http://10.22.9.224:3000/health"
    private var qiContext: QiContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
    }
    
    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
    
    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the QiContext for later use
        this.qiContext = qiContext

        // Start the API request as soon as the activity is created
        fetchHealthEndpoint()
    }
    
    override fun onRobotFocusLost() {
        // Reset QiContext
        this.qiContext = null
    }
    
    override fun onRobotFocusRefused(reason: String) {
        // Handle focus refused
        "Robot focus refused: $reason".also { responseTextView.text = it }
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
                    val errorMessage = "Error: ${e.message}"
                    responseTextView.text = errorMessage
                    
                    // Speak the error message if QiContext is available
                    qiContext?.let { context ->
                        speakText(context, errorMessage)
                    }
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string() ?: "No response body"

                // Try to directly say a simple phrase to test if speech works regardless of directory issues
                try {
                    val testPhrase = Phrase("Here is what I got from the endpoint: $responseText")
                    val say = SayBuilder.with(qiContext)
                        .withPhrase(testPhrase)
                        .build()
                        
                    // Execute in a separate thread to avoid blocking UI
                    Thread {
                        try {
                            say.run()
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("PepperApp", "Error during test speech: ${e.message}")
                            }
                        }
                    }.start()
                } catch (e: Exception) {
                    Log.e("PepperApp", "Error creating test speech: ${e.message}")
                }
                
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    responseTextView.text = responseText
                }
            }
        })
    }
    
    private fun speakText(qiContext: QiContext, text: String) {
        // Create a phrase with the text to say
        val phrase = Phrase(text)
        
        // Build the say action
        val say = SayBuilder.with(qiContext)
            .withPhrase(phrase)
            .build()
        
        // Execute the say action
        say.run()
    }
}