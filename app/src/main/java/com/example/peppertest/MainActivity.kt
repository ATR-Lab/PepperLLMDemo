package com.example.peppertest

import android.os.Bundle
import android.util.Log
import android.view.View
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.human.EngagementIntentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.EngageHuman
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    private val client = OkHttpClient()
    private val serverUrl = "http://10.22.9.224:3000/health"
    private var qiContext: QiContext? = null
    
    // Store animations and animate actions
    private var raiseHandsAnimate: Animate? = null
    private var danceAnimate: Animate? = null
    private var currentAnimate: Animate? = null
    private var isAnimationRunning = false
    
    // Human engagement variables
    private var humanEngagementFuture: Future<Void>? = null
    private var engageHuman: EngageHuman? = null
    private var humanCheckTaskRunning = false
    private val TAG = "PepperEyeContact"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
        
        // Set up button click listeners
        raiseHandsButton.setOnClickListener {
            Log.d(TAG, "raiseHandsButton::setOnClickListener called")
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
        
        // Start the human engagement process
        startHumanEngagement()
        
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
        // Stop engaging with humans
        stopHumanEngagement()
        
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
        runOnUiThread {
            "Robot focus refused: $reason".also { responseTextView.text = it }
        }
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
            Log.e(TAG, "Error loading animations: ${e.message}")
            runOnUiThread {
                responseTextView.text = "Error loading animations: ${e.message}"
            }
        }
    }
    
    private fun playAnimation(animate: Animate?, animationName: String) {
        Log.d(TAG, "currently in playAnimation(Animate?, String)")

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
        
        // Mark animation as running
        isAnimationRunning = true
        
        // Run the animation asynchronously
        animate.async().run().thenConsume { future ->
            isAnimationRunning = false
            runOnUiThread {
                if (future.isSuccess) {
                    responseTextView.text = "$animationName animation completed"
                } else if (future.hasError()) {
                    val error = future.error
                    Log.e(TAG, "Error during animation: ${error.message}")
                    responseTextView.text = "Animation error: ${error.message}"
                }
                progressBar.visibility = View.GONE
                
                // Remove the listener after animation completes
                animate.removeAllOnStartedListeners()
            }
        }
    }
    
    // =============== Human engagement functions ===============
    
    private fun startHumanEngagement() {
        qiContext?.let { context ->
            humanCheckTaskRunning = true
            
            // Start a periodic task to find and engage with humans
            Thread {
                try {
                    while (humanCheckTaskRunning && qiContext != null) {
                        findMostEngagedHuman()
                        // Check for engaged humans every 3 seconds
                        TimeUnit.SECONDS.sleep(3)
                    }
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Human engagement check interrupted")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in human engagement check: ${e.message}")
                }
            }.start()
            
            runOnUiThread {
                responseTextView.text = "Looking for humans to engage with..."
            }
        }
    }
    
    private fun stopHumanEngagement() {
        // Stop the periodic human check
        humanCheckTaskRunning = false
        
        // Cancel any active engagement
        humanEngagementFuture?.requestCancellation()
        humanEngagementFuture = null
        
        // Clean up the engage human action
        engageHuman = null
    }
    
    private fun findMostEngagedHuman() {
        qiContext?.let { context ->
            try {
                // Get the human awareness service
                val humanAwareness = context.humanAwareness
                
                // Get humans around the robot
                val humansAround = humanAwareness.humansAround
                
                if (humansAround.isEmpty()) {
                    Log.d(TAG, "No humans detected")
                    runOnUiThread {
                        responseTextView.text = "No humans detected"
                    }
                    return
                }
                
                Log.d(TAG, "Found ${humansAround.size} humans around")
                
                // Find the human with the highest engagement intention
                val mostEngagedHuman = findHumanWithHighestEngagement(humansAround)
                
                if (mostEngagedHuman != null) {
                    engageWithHuman(mostEngagedHuman)
                } else {
                    Log.d(TAG, "No humans showing engagement")
                    runOnUiThread {
                        responseTextView.text = "No humans engaging"
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error finding humans: ${e.message}")
            }
        }
    }
    
    private fun findHumanWithHighestEngagement(humans: List<Human>): Human? {
        // Find humans that are engaged or want to engage
        val engagedHumans = humans.filter { 
            it.engagementIntention == EngagementIntentionState.INTERESTED
        }
        
        // If we have engaged humans, return the first one
        if (engagedHumans.isNotEmpty()) {
            Log.d(TAG, "Found ${engagedHumans.size} engaged humans")
            return engagedHumans.first()
        }
        
        // Otherwise, return the first human that at least has attention toward the robot
        return humans.firstOrNull { 
            it.attention.toString() == "LOOKING_AT" 
        }
    }
    
    private fun engageWithHuman(human: Human) {
        qiContext?.let { context ->
            // Cancel any existing engagement
            humanEngagementFuture?.requestCancellation()
            
            try {
                // Create a new engage human action
                engageHuman = EngageHumanBuilder.with(context)
                    .withHuman(human)
                    .build()
                
                // Start engaging with the human
                humanEngagementFuture = engageHuman?.async()?.run()
                
                Log.d(TAG, "Engaging with human: ${human.attention}, ${human.engagementIntention}")
                
                runOnUiThread {
                    responseTextView.text = "Engaging with human"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error engaging with human: ${e.message}")
                runOnUiThread {
                    responseTextView.text = "Error engaging: ${e.message}"
                }
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