package com.example.peppertest

import android.os.Bundle
import android.util.Log
import android.view.View
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    private val client = OkHttpClient()
    private val serverUrl = "http://10.22.9.224:3000/health"
    private var qiContext: QiContext? = null
    
    // Store animations and animate actions
    private var raiseHandsAnimate: Animate? = null
    private var danceAnimate: Animate? = null
    private var currentAnimate: Animate? = null
    private var isAnimationRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
        
        // Set up button click listeners
        raiseHandsButton.setOnClickListener {
            Log.d( "MARCUS", "raiseHandsButton::setOnClickListener called")
            playAnimation(raiseHandsAnimate, "Raise Hands")
        }
        
        danceButton.setOnClickListener {
            playAnimation(danceAnimate, "Dance")
        }
    }
    
    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
    
    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the QiContext for later use
        this.qiContext = qiContext
        
        // Preload all animations in onRobotFocusGained
        preloadAnimations(qiContext)
        
        // Remove or move to different thread, onRobotFocusGained runs on a different thread than the
        // main UI thread.
//        responseTextView.text = "Ready to play animations!"

        /* 
        // Original implementation - commented out as requested
        // Start the API request as soon as the activity is created
        fetchHealthEndpoint()
        */
    }
    
    override fun onRobotFocusLost() {
        // Clean up resources
        raiseHandsAnimate?.removeAllOnStartedListeners()
        danceAnimate?.removeAllOnStartedListeners()
        currentAnimate = null
        raiseHandsAnimate = null
        danceAnimate = null
        isAnimationRunning = false
        
        // Reset QiContext
        this.qiContext = null
    }
    
    override fun onRobotFocusRefused(reason: String) {
        // Handle focus refused
        "Robot focus refused: $reason".also { responseTextView.text = it }
    }
    
    private fun preloadAnimations(qiContext: QiContext) {
        try {
            // Load the raise hands animation
            val raiseHandsAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_both_hands_b001)
                .build()
                
            // Create the animate action for raise hands
            raiseHandsAnimate = AnimateBuilder.with(qiContext)
                .withAnimation(raiseHandsAnimation)
                .build()
                
            // Load the dance animation
            val danceAnimation = AnimationBuilder.with(qiContext)
                .withResources(R.raw.dance_b001)
                .build()
                
            // Create the animate action for dance
            danceAnimate = AnimateBuilder.with(qiContext)
                .withAnimation(danceAnimation)
                .build()
                
            // Enable buttons when animations are loaded
            runOnUiThread {
                raiseHandsButton.isEnabled = true
                danceButton.isEnabled = true
            }
            
        } catch (e: Exception) {
            Log.e("Animation", "Error loading animations: ${e.message}")
            runOnUiThread {
                responseTextView.text = "Error loading animations: ${e.message}"
            }
        }
    }
    
    private fun playAnimation(animate: Animate?, animationName: String) {
        Log.d( "MARCUS", "currently in playAnimation(Animate?, String)")

        // Check if an animation is already running
        if (isAnimationRunning) {
            responseTextView.text = "Animation already running, please wait..."
            return
        }
        
        if (animate == null) {
            responseTextView.text = "Animation not loaded. Try again later."
            return
        }
        
        // Show progress indicator
        progressBar.visibility = View.VISIBLE
        responseTextView.text = "Starting $animationName animation..."
        
        // Store current animate action
        currentAnimate = animate
        
        // Add listeners for animation events
//        animate.addOnStartedListener {
//            Log.d( "MARCUS", "Animate.addOnStartedListener called")
//            isAnimationRunning = true
////            runOnUiThread {
////                responseTextView.text = "$animationName animation started"
////                progressBar.visibility = View.GONE
////            }
//        }
        
        // Run the animation asynchronously
        animate.async().run().thenConsume { future ->
            isAnimationRunning = false
            runOnUiThread {
                if (future.isSuccess) {
                    responseTextView.text = "$animationName animation completed"
                } else if (future.hasError()) {
                    val error = future.error
                    Log.e("Animation", "Error during animation: ${error.message}")
                    responseTextView.text = "Animation error: ${error.message}"
                }
                progressBar.visibility = View.GONE
                
                // Remove the listener after animation completes
                animate.removeAllOnStartedListeners()
            }
        }
    }
    
    /*
    // Original implementation - commented out as requested
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
    */
}