package com.example.peppertest

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.aldebaran.qi.Consumer
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.Qi
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.`object`.human.EngagementIntentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.EngageHuman
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.human.ExcitementState
import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.humanawareness.EngagementPolicy
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.example.peppertest.camera.PepperCameraManager
import com.example.peppertest.command.CommandDispatcher
import com.example.peppertest.websocket.PepperWebSocketClient // Uncommented WebSocket import
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : RobotActivity(), RobotLifecycleCallbacks, 
                     PepperWebSocketClient.CommandListener,
                     PepperWebSocketClient.ConnectionStateListener {
    companion object {
        private const val TAG = "PepperHumanAwareness"
        private const val DEFAULT_WEBSOCKET_URL = "ws://10.22.25.94:5001/pepper" // Default WebSocket URL
    }
    
    private var qiContext: QiContext? = null
    
    // Autonomous abilities holder
    private var autonomousAbilitiesHolder: Holder? = null
    
    // Human awareness properties
    private var humanAwareness: HumanAwareness? = null
    private var currentEngagedHuman: Human? = null
    private var engageHumanAction: EngageHuman? = null
    private var engageHumanFuture: Future<Void>? = null
    private var isEngagementRunning = false
    private var humanAwarenessInitialized = false
    
    // WebSocket client
    private var webSocketClient: PepperWebSocketClient? = null
    private var websocketServerUrl = DEFAULT_WEBSOCKET_URL
    private var isSpeaking = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
        
        // Initialize WebSocket URL from preferences if available
        websocketServerUrl = getPreferences(Context.MODE_PRIVATE)
            .getString("websocket_url", DEFAULT_WEBSOCKET_URL) ?: DEFAULT_WEBSOCKET_URL
    }

    override fun onDestroy() {
        // Release autonomous abilities (re-enable autonomous life) before unregistering
        releaseAutonomousAbilities()
        
        // Disconnect WebSocket if connected
        disconnectWebSocket()
        
        // Unregister the RobotLifecycleCallbacks
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
    
    override fun onRobotFocusGained(qiContext: QiContext) {
        // Store the QiContext for later use
        this.qiContext = qiContext
        
        // Hold autonomous abilities (disable autonomous life)
        holdAutonomousAbilities(qiContext)
        
        // Initialize human awareness
        initializeHumanAwareness(qiContext)
        
        // Connect to WebSocket server
        connectWebSocket()
    }
    
    override fun onRobotFocusLost() {
        // Stop engagement if running
        stopEngagement()
        
        // Disconnect WebSocket if connected
        disconnectWebSocket()
        
        // Release autonomous abilities (re-enable autonomous life)
        releaseAutonomousAbilities()

        // Reset QiContext and human awareness
        humanAwareness = null
        humanAwarenessInitialized = false
        this.qiContext = null
    }
    
    override fun onRobotFocusRefused(reason: String) {
        // Handle focus refused
        Log.e(TAG, "Robot focus refused: $reason")
        runOnUiThread {
            responseTextView.text = "Robot focus refused: $reason"
        }
    }
    
    /**
     * Initialize WebSocket connection
     */
    private fun connectWebSocket() {
        if (webSocketClient == null) {
            webSocketClient = PepperWebSocketClient(
                websocketServerUrl,
                this,  // CommandListener
                this   // ConnectionStateListener
            )
            
            webSocketClient?.connect()
            Log.i(TAG, "Connecting to WebSocket server: $websocketServerUrl")
            updateStatus("Connecting to WebSocket server...")
        }
    }
    
    /**
     * Disconnect WebSocket connection
     */
    private fun disconnectWebSocket() {
        webSocketClient?.disconnect()
        webSocketClient = null
        Log.i(TAG, "Disconnected from WebSocket server")
    }
    
    /**
     * Handle WebSocket Command events
     */
    override fun onCommandReceived(command: JSONObject) {
        Log.d(TAG, "Command received: $command")
        
        try {
            val type = command.getString("type")
            
            when (type) {
                "command" -> {
                    // Process command with action field
                    if (command.has("action")) {
                        val action = command.getString("action")
                        
                        when (action) {
                            "say" -> {
                                // Handle text-to-speech command
                                if (command.has("text")) {
                                    val text = command.getString("text")
                                    if (text.isNotEmpty()) {
                                        Log.i(TAG, "Speaking text: $text")
                                        runOnUiThread {
                                            updateStatus("Speaking: $text")
                                        }
                                        
                                        // Check for animation flag in command
                                        val withAnimation = command.optBoolean("with_animation", false)
                                        
                                        if (withAnimation) {
                                            sayTextWithAnimation(text)
                                        } else {
                                            sayText(text)
                                        }
                                    }
                                }
                            }
                            else -> {
                                Log.d(TAG, "Unhandled action type: $action")
                            }
                        }
                    }
                }
                "speak" -> {
                    // Original format for backward compatibility
                    if (command.has("text")) {
                        val text = command.getString("text")
                        if (text.isNotEmpty()) {
                            Log.i(TAG, "Speaking text: $text")
                            runOnUiThread {
                                updateStatus("Speaking: $text")
                            }
                            
                            // Check for additional parameters
                            val withAnimation = command.optBoolean("with_animation", false)
                            
                            // If animation requested, perform animated speech
                            if (withAnimation) {
                                sayTextWithAnimation(text)
                            } else {
                                sayText(text)
                            }
                        }
                    }
                }
                "config" -> {
                    // Handle configuration commands
                    if (command.has("websocket_url")) {
                        val newUrl = command.getString("websocket_url")
                        configureWebSocketUrl(newUrl)
                    }
                }
                else -> {
                    Log.d(TAG, "Unhandled command type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing command: ${e.message}", e)
        }
    }
    
    /**
     * Configure WebSocket server URL
     */
    fun configureWebSocketUrl(url: String) {
        if (url.isEmpty() || url == websocketServerUrl) {
            return
        }
        
        // Save the new URL to preferences
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString("websocket_url", url)
            .apply()
        
        websocketServerUrl = url
        Log.i(TAG, "WebSocket URL updated: $url")
        
        // Reconnect with new URL if connected
        if (webSocketClient != null) {
            disconnectWebSocket()
            connectWebSocket()
        }
    }
    
    /**
     * Send robot information to WebSocket server after connection
     */
    private fun sendRobotInfo() {
        try {
            val infoJson = JSONObject().apply {
                put("type", "robot_info")
                put("name", "Pepper")
                put("version", "1.0")
                put("capabilities", JSONArray().apply {
                    put("speech")
                    put("human_awareness")
                })
                put("timestamp", System.currentTimeMillis())
            }
            
            webSocketClient?.sendMessage(infoJson.toString())
            Log.d(TAG, "Sent robot info to server")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending robot info: ${e.message}", e)
        }
    }
    
    /**
     * Handle WebSocket Connection events
     */
    override fun onConnected() {
        Log.i(TAG, "Connected to WebSocket server")
        runOnUiThread {
            updateStatus("Connected to WebSocket server")
        }
        
        // Send robot information
        sendRobotInfo()
    }
    
    override fun onDisconnected() {
        Log.i(TAG, "Disconnected from WebSocket server")
        runOnUiThread {
            updateStatus("Disconnected from WebSocket server")
        }
    }
    
    override fun onReconnectFailed() {
        Log.e(TAG, "Failed to reconnect to WebSocket server")
        runOnUiThread {
            updateStatus("Failed to connect to WebSocket server")
        }
    }
    
    /**
     * Hold autonomous abilities to disable Pepper's autonomous life
     */
    private fun holdAutonomousAbilities(qiContext: QiContext) {
        try {
            // Build the holder for the autonomous abilities
            val holder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                    AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                    AutonomousAbilitiesType.BASIC_AWARENESS,
                    AutonomousAbilitiesType.AUTONOMOUS_BLINKING
                )
                .build()
                
            // Store the holder
            autonomousAbilitiesHolder = holder
            
            // Hold the abilities asynchronously
            holder.async().hold().andThenConsume(Qi.onUiThread(Consumer<Void> {
                Log.i(TAG, "Autonomous abilities held successfully")
            }))
        } catch (e: Exception) {
            Log.e(TAG, "Error holding autonomous abilities: ${e.message}", e)
        }
    }
    
    /**
     * Release autonomous abilities to re-enable Pepper's autonomous life
     */
    private fun releaseAutonomousAbilities() {
        try {
            // Get the holder
            val holder = autonomousAbilitiesHolder ?: return
            
            // Release the holder asynchronously
            holder.async().release().andThenConsume(Qi.onUiThread(Consumer<Void> {
                Log.i(TAG, "Autonomous abilities released successfully")
                autonomousAbilitiesHolder = null
            }))
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing autonomous abilities: ${e.message}", e)
        }
    }
    
    /**
     * Initialize human awareness and set up human detection
     */
    private fun initializeHumanAwareness(qiContext: QiContext) {
        try {
            // Get the HumanAwareness service
            humanAwareness = qiContext.humanAwareness
            
            // Set up listeners for humans
            humanAwareness?.addOnHumansAroundChangedListener { humans ->
                processHumansAround(humans)
            }
            
            humanAwarenessInitialized = true
            Log.i(TAG, "Human awareness initialized successfully")
            updateStatus("Human awareness active - looking for humans")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing human awareness: ${e.message}", e)
            updateStatus("Failed to initialize human awareness")
        }
    }
    
    /**
     * Process the list of humans detected around the robot
     */
    private fun processHumansAround(humans: List<Human>) {
        // Debug output showing number of humans detected
        Log.d(TAG, "Humans detected: ${humans.size}")
        
        if (humans.isEmpty()) {
            return
        } else {
            // Found at least one engaged human
            engageWithHuman(humans.first())
        }
    }
    
    /**
     * Engage with a specific human
     */
    private fun engageWithHuman(human: Human) {
        val ctx = qiContext ?: return
        
        try {
            // Stop any existing engagement
            stopEngagement()
            
            // Store the human we're engaging with
            currentEngagedHuman = human
            
            // Debug additional information about the human
            Log.d(TAG, "Engaging with human: attention=${human.attention}, excitement=${human.emotion?.excitement}")
            
            // Build the engage human action
            engageHumanAction = EngageHumanBuilder.with(ctx)
                .withHuman(human)
                .build()
            
            // Run the engagement
            Log.i(TAG, "Starting engagement with human")
            isEngagementRunning = true
            
            // Run engagement asynchronously
            engageHumanFuture = engageHumanAction?.async()?.run()
            
            // Add a listener for when engagement completes or fails
            engageHumanFuture?.thenConsume { future ->
                if (future.isSuccess) {
                    Log.i(TAG, "Engagement completed successfully")
                } else if (future.isCancelled) {
                    Log.i(TAG, "Engagement was cancelled")
                } else {
                    Log.e(TAG, "Engagement error: ${future.error.message}")
                }
                isEngagementRunning = false
                currentEngagedHuman = null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error engaging with human: ${e.message}", e)
            isEngagementRunning = false
            currentEngagedHuman = null
        }
    }
    
    /**
     * Stop the current engagement action
     */
    private fun stopEngagement() {
        if (isEngagementRunning) {
            try {
                // Cancel the future
                engageHumanFuture?.requestCancellation()
                engageHumanFuture = null
                
                // Reset engagement state
                engageHumanAction = null
                currentEngagedHuman = null
                isEngagementRunning = false
                
                Log.i(TAG, "Engagement stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping engagement: ${e.message}", e)
            }
        }
    }

    /**
     * Update the status text on the UI thread
     */
    private fun updateStatus(message: String) {
        Log.i(TAG, "Status update: $message")
        runOnUiThread {
            responseTextView.text = message
        }
    }
    
    /**
     * Say text using the robot's speech synthesis
     */
    private fun sayText(text: String) {
        val ctx = qiContext ?: return
        
        // Check if already speaking
        if (isSpeaking.getAndSet(true)) {
            Log.d(TAG, "Already speaking, queueing text: $text")
            // Queue the text for later (not implemented in this simple example)
            return
        }
        
        try {
            // Send speaking started status to server
            sendSpeakingStatus("started", text)
            
            // Build and run the say action
            val say = SayBuilder.with(ctx)
                .withText(text)
                .build()
            
            // Run the say action and handle completion
            say.async().run().thenConsume { future ->
                isSpeaking.set(false) // Reset speaking state
                
                if (future.isSuccess) {
                    Log.d(TAG, "Speech completed successfully: $text")
                    sendSpeakingStatus("completed", text)
                } else if (future.isCancelled) {
                    Log.d(TAG, "Speech was cancelled: $text")
                    sendSpeakingStatus("cancelled", text)
                } else {
                    Log.e(TAG, "Speech error: ${future.error.message}")
                    sendSpeakingStatus("error", text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saying text: ${e.message}", e)
            isSpeaking.set(false) // Reset speaking state on error
            sendSpeakingStatus("error", text)
        }
    }
    
    /**
     * Send speaking status back to the WebSocket server
     */
    private fun sendSpeakingStatus(status: String, text: String) {
        try {
            val statusJson = JSONObject().apply {
                put("type", "command_status")
                put("action", "say")
                put("status", status)
                put("text", text)
                put("timestamp", System.currentTimeMillis())
            }
            
            webSocketClient?.sendMessage(statusJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error sending speaking status: ${e.message}", e)
        }
    }
    
    /**
     * Say text with accompanying animation
     */
    private fun sayTextWithAnimation(text: String) {
        val ctx = qiContext ?: return
        
        // Check if already speaking
        if (isSpeaking.getAndSet(true)) {
            Log.d(TAG, "Already speaking, queueing text: $text")
            return
        }
        
        try {
            // Send speaking started status to server
            sendSpeakingStatus("started", text)
            
            // Build the say action
            val say = SayBuilder.with(ctx)
                .withText(text)
                .build()
            
            // Run the speech
            say.async().run().thenConsume { sayFuture ->
                isSpeaking.set(false)
                
                if (sayFuture.isSuccess) {
                    Log.d(TAG, "Animated speech completed successfully")
                    sendSpeakingStatus("completed", text)
                } else if (sayFuture.isCancelled) {
                    Log.d(TAG, "Animated speech was cancelled")
                    sendSpeakingStatus("cancelled", text)
                } else {
                    Log.e(TAG, "Animated speech error: ${sayFuture.error.message}")
                    sendSpeakingStatus("error", text)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with animated speech: ${e.message}", e)
            isSpeaking.set(false)
            sendSpeakingStatus("error", text)
        }
    }
}