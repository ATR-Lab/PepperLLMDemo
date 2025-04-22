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
// import com.example.peppertest.websocket.PepperWebSocketClient // Commented out WebSocket import
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    companion object {
        private const val TAG = "PepperHumanAwareness"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
    }

    override fun onDestroy() {
        // Release autonomous abilities (re-enable autonomous life) before unregistering
        releaseAutonomousAbilities()
        
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
    }
    
    override fun onRobotFocusLost() {
        // Stop engagement if running
        stopEngagement()
        
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
        
        try {
            // Build and run the say action
            val say = SayBuilder.with(ctx)
                .withText(text)
                .build()
                
            say.run()
        } catch (e: Exception) {
            Log.e(TAG, "Error saying text: ${e.message}", e)
        }
    }
}