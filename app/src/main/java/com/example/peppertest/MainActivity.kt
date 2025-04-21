package com.example.peppertest

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
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
import com.example.peppertest.camera.PepperCameraManager
import com.example.peppertest.command.CommandDispatcher
import com.example.peppertest.websocket.PepperWebSocketClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class MainActivity : RobotActivity(), RobotLifecycleCallbacks, 
                     PepperCameraManager.FrameListener,
                     PepperWebSocketClient.CommandListener,
                     PepperWebSocketClient.ConnectionStateListener {
                     
    companion object {
        private const val TAG = "PepperMainActivity"
        private const val WEBSOCKET_URL = "ws://10.22.25.94:5001/pepper"
    }
    
    private var qiContext: QiContext? = null
    
    // WebSocket client
    private lateinit var webSocketClient: PepperWebSocketClient
    
    // Camera manager
    private lateinit var cameraManager: PepperCameraManager
    
    // Command dispatcher
    private var commandDispatcher: CommandDispatcher? = null
    
    // Store animations and animate actions
    private var raiseHandsAnimate: Animate? = null
    private var danceAnimate: Animate? = null
    private var currentAnimate: Animate? = null
    private var isAnimationRunning = false
    
    // Human engagement variables
    private var humanEngagementFuture: Future<Void>? = null
    private var engageHuman: EngageHuman? = null
    private var humanCheckTaskRunning = false
    
    // Add reconnection timer
    private var reconnectionTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize WebSocket client
        webSocketClient = PepperWebSocketClient(WEBSOCKET_URL, this, this)
        
        // Load saved WebSocket URL from preferences
        val sharedPreferences = getSharedPreferences("PepperSettings", Context.MODE_PRIVATE)
        val savedUrl = sharedPreferences.getString("websocket_url", WEBSOCKET_URL)
        if (savedUrl != WEBSOCKET_URL) {
            // Use saved URL if different from default
            webSocketClient.setServerUrl(savedUrl!!)
            Log.d(TAG, "Using saved WebSocket URL: $savedUrl")
        }
        
        // Add a button to the layout for settings if needed
        // For example, you could add a long-press listener to an existing button:
        responseTextView.setOnLongClickListener {
            showServerSettings()
            true
        }
        
        // Initialize camera manager
        cameraManager = PepperCameraManager(this)
        
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
    
    private fun showServerSettings() {
        val input = EditText(this)
        input.setText(webSocketClient.getServerUrl())
        
        AlertDialog.Builder(this)
            .setTitle("Server Settings")
            .setMessage("Enter WebSocket Server URL:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val url = input.text.toString()
                // Save to preferences
                getSharedPreferences("PepperSettings", Context.MODE_PRIVATE)
                    .edit()
                    .putString("websocket_url", url)
                    .apply()
                
                // Apply the new URL immediately
                webSocketClient.setServerUrl(url)
                
                // Notify user
                runOnUiThread {
                    responseTextView.text = "Server URL updated to: $url"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroy() {
        // Stop reconnection attempts
        reconnectionTimer?.cancel()
        reconnectionTimer = null
        
        // Clean up resources
        webSocketClient.disconnect()
        cameraManager.release()
        commandDispatcher?.release()
        
        // Unregister the RobotLifecycleCallbacks
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
    
    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the QiContext for later use
        this.qiContext = qiContext
        
        // Initialize command dispatcher
        commandDispatcher = CommandDispatcher(qiContext)
        
        // Initialize camera
        cameraManager.initialize(qiContext)
        
        // Preload all animations in onRobotFocusGained
        preloadAnimations(qiContext)
        
        // Start the human engagement process
        startHumanEngagement()
        
        // Connect to WebSocket server
        webSocketClient.connect()
        
        runOnUiThread {
            responseTextView.text = "Robot ready. Connecting to server..."
        }
    }
    
    override fun onRobotFocusLost() {
        // Stop engaging with humans
        stopHumanEngagement()
        
        // Stop camera capture
        cameraManager.stopCapture()
        
        // Clean up resources
        raiseHandsAnimate?.removeAllOnStartedListeners()
        danceAnimate?.removeAllOnStartedListeners()
        currentAnimate = null
        raiseHandsAnimate = null
        danceAnimate = null
        isAnimationRunning = false
        
        // Reset QiContext
        this.qiContext = null
        commandDispatcher = null
    }
    
    override fun onRobotFocusRefused(reason: String) {
        // Handle focus refused
        runOnUiThread {
            "Robot focus refused: $reason".also { responseTextView.text = it }
        }
    }
    
    //
    // WebSocket Connection State Listener Implementation
    //
    
    override fun onConnected() {
        Log.d(TAG, "WebSocket connected")
//        runOnUiThread {
//            statusTextView.text = "Connected"
//            statusTextView.setTextColor(Color.GREEN)
//        }
        
        // Cancel reconnection timer if it's running
        reconnectionTimer?.cancel()
        reconnectionTimer = null
        
        // Start camera capture when connected
        cameraManager.startCapture()
        
        // Send a timestamp message to measure latency
        try {
            val timestamp = System.currentTimeMillis()
            val message = JSONObject().apply {
                put("type", "timestamp")
                put("timestamp", timestamp)
            }
            webSocketClient.sendMessage(message.toString())
            Log.d(TAG, "Sent timestamp message for latency measurement")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending timestamp message", e)
        }
    }
    
    override fun onDisconnected() {
        Log.d(TAG, "WebSocket disconnected")
//        runOnUiThread {
//            statusTextView.text = "Disconnected"
//            statusTextView.setTextColor(Color.RED)
//        }
        
        // Pause camera capture when disconnected
        cameraManager.pauseCapture()
        
        // Start reconnection timer if not already running
        if (reconnectionTimer == null) {
            reconnectionTimer = Timer()
            reconnectionTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // Try to reconnect
                    if (webSocketClient != null) {
                        Log.d(TAG, "Attempting to reconnect to WebSocket server...")
                        webSocketClient.connect()
                    }
                }
            }, 5000, 5000) // Try every 5 seconds
        }
    }
    
    override fun onReconnectFailed() {
        Log.d(TAG, "WebSocket reconnection failed")
//        runOnUiThread {
//            statusTextView.text = "Reconnection Failed"
//            statusTextView.setTextColor(Color.RED)
//        }
        
        // Stop camera capture when reconnection fails
        cameraManager.stopCapture()
    }
    
    //
    // Frame Listener Implementation
    //
    
    override fun onFrameCaptured(imageData: ByteArray, timestamp: Long) {
        // Send the frame over WebSocket
        webSocketClient.sendCameraFrame(imageData)
        
        // Send a timestamp message for latency tracking
        try {
            val message = JSONObject().apply {
                put("type", "frameTimestamp")
                put("timestamp", timestamp)
            }
            webSocketClient.sendMessage(message.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error sending timestamp", e)
        }
    }
    
    //
    // Command Listener Implementation
    //
    
    override fun onCommandReceived(command: JSONObject) {
        Log.d(TAG, "Command received in MainActivity: $command")
        
        // Dispatch the command to the CommandDispatcher
        commandDispatcher?.dispatch(command)
    }
    
    //
    // Animation Methods
    //
    
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
    
    //
    // Human Engagement Methods
    //
    
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
}