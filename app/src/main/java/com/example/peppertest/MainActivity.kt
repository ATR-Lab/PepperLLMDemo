package com.example.peppertest

import android.os.Bundle
import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.QiThreadPool
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
//import com.aldebaran.qi.sdk.builder.AnimateBuilder
//import com.aldebaran.qi.sdk.builder.AnimationBuilder
//import com.aldebaran.qi.sdk.builder.EngageHumanBuilder
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.aldebaran.qi.sdk.builder.LookAtBuilder
//import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
//import com.aldebaran.qi.sdk.`object`.actuation.Animate
//import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.actuation.LookAt
import com.aldebaran.qi.sdk.`object`.actuation.LookAtMovementPolicy
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.geometry.Vector3
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
//import com.aldebaran.qi.sdk.`object`.conversation.Phrase
//import com.aldebaran.qi.sdk.`object`.human.EngagementIntentionState
//import com.aldebaran.qi.sdk.`object`.human.Human
//import com.aldebaran.qi.sdk.`object`.humanawareness.EngageHuman
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlinx.android.synthetic.main.activity_main.*
//import okhttp3.*
//import java.io.IOException
//import java.util.concurrent.TimeUnit
import java.util.concurrent.Callable

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    //private val client = OkHttpClient()
    //private val serverUrl = "http://10.22.9.224:3000/health"
    private var qiContext: QiContext? = null
    
    // Store animations and animate actions
    //private var raiseHandsAnimate: Animate? = null
    //private var danceAnimate: Animate? = null
    //private var currentAnimate: Animate? = null
    //private var isAnimationRunning = false
    
    // Human engagement variables
    //private var humanEngagementFuture: Future<Void>? = null
    //private var engageHuman: EngageHuman? = null
    //private var humanCheckTaskRunning = false
    private val TAG = "PepperHeadControl"
    
    // Head movement variables
    private var lookAt: LookAt? = null
    private var lookAtFuture: Future<Void>? = null
    private var targetFrame: Frame? = null
    
    // Head position coordinates (relative to robot's frame)
    // X: Forward/backward, Y: Left/right, Z: Up/down
    private var headPositionX = 0.5  // Less extreme forward position
    private var headPositionY = 0.0  // Centered left-right
    private var headPositionZ = 0.5  // Slightly upward
    
    // Movement increments
    private val POSITION_INCREMENT = 0.2
    
    // Autonomous ability control
    private var autonomousAbilitiesHolder: Holder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Register the RobotLifecycleCallbacks
        QiSDK.register(this, this)
        
        // Set up head control button listeners
        // Z axis for up/down
        headUpButton.setOnClickListener {
            // Execute on a background thread
            QiThreadPool.execute(Callable {
                moveHead(0.0, 0.0, POSITION_INCREMENT)
                Log.d(TAG, "Up button pressed - increasing Z coordinate")
                null
            })
        }
        
        headDownButton.setOnClickListener {
            QiThreadPool.execute(Callable {
                moveHead(0.0, 0.0, -POSITION_INCREMENT)
                Log.d(TAG, "Down button pressed - decreasing Z coordinate")
                null
            })
        }
        
        // Y axis for left/right
        headLeftButton.setOnClickListener {
            QiThreadPool.execute(Callable {
                moveHead(0.0, POSITION_INCREMENT, 0.0)
                Log.d(TAG, "Left button pressed - increasing Y coordinate")
                null
            })
        }
        
        headRightButton.setOnClickListener {
            QiThreadPool.execute(Callable {
                moveHead(0.0, -POSITION_INCREMENT, 0.0)
                Log.d(TAG, "Right button pressed - decreasing Y coordinate")
                null
            })
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
        
        // Disable autonomous abilities that might interfere with head movement
        disableAutonomousAbilities(qiContext)
        
        // Initialize head movement
        initializeHeadControl(qiContext)
    }
    
    override fun onRobotFocusLost() {
        // Re-enable autonomous abilities
        releaseAutonomousAbilities()
        
        // Clean up head movement resources
        cancelHeadMovement()
        lookAt = null
        targetFrame = null
        
        // Reset QiContext
        this.qiContext = null
    }
    
    override fun onRobotFocusRefused(reason: String) {
        // Handle focus refused
        Log.e(TAG, "Robot focus refused: $reason")
    }
    
    // =============== Autonomous Abilities functions ===============
    
    private fun disableAutonomousAbilities(qiContext: QiContext) {
        try {
            // Create a holder for the autonomous abilities
            autonomousAbilitiesHolder = HolderBuilder.with(qiContext)
                .withAutonomousAbilities(
                    AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                    AutonomousAbilitiesType.BASIC_AWARENESS,
                    AutonomousAbilitiesType.AUTONOMOUS_BLINKING
                )
                .build()
            
            // Hold the abilities (disables them) asynchronously
            autonomousAbilitiesHolder?.async()?.hold()?.thenConsume { future ->
                if (future.isSuccess) {
                    Log.i(TAG, "Autonomous abilities have been disabled")
                } else if (future.hasError()) {
                    Log.e(TAG, "Error disabling autonomous abilities: ${future.error.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up autonomous abilities holder: ${e.message}")
        }
    }
    
    private fun releaseAutonomousAbilities() {
        try {
            // Release the holder to re-enable autonomous abilities
            autonomousAbilitiesHolder?.async()?.release()?.thenConsume { future ->
                if (future.isSuccess) {
                    Log.i(TAG, "Autonomous abilities have been re-enabled")
                } else if (future.hasError()) {
                    Log.e(TAG, "Error re-enabling autonomous abilities: ${future.error.message}")
                }
            }
            
            // Reset the holder
            autonomousAbilitiesHolder = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing autonomous abilities: ${e.message}")
        }
    }
    
    // =============== Head movement functions ===============

    private fun initializeHeadControl(qiContext: QiContext) {
        try {
            // All operations that use QiContext should be done asynchronously
            QiThreadPool.execute(Callable {
                // Get the Actuation service from QiContext
                val actuation = qiContext.actuation
                
                // Get the robot frame as the reference frame
                val robotFrame = actuation.robotFrame()
                
                // Store the robot frame for reuse
                targetFrame = robotFrame
                
                // Initial head positioning
                createLookAtAction(qiContext)
                
                // Enable buttons on UI thread
                runOnUiThread {
                    headUpButton.isEnabled = true
                    headDownButton.isEnabled = true
                    headLeftButton.isEnabled = true
                    headRightButton.isEnabled = true
                }
                
                Log.d(TAG, "Head control initialized successfully")
                Log.d(TAG, "Initial head position: X=${headPositionX}, Y=${headPositionY}, Z=${headPositionZ}")
                
                return@Callable null
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing head control: ${e.message}")
        }
    }
    
    private fun createLookAtAction(qiContext: QiContext) {
        try {
            // Get the Actuation service
            val actuation = qiContext.actuation
            
            // Get the robot frame
            val robotFrame = actuation.robotFrame()
            
            // Create a transform for the current head position
            // Use 3D vector for proper positioning including Z height
            val vector3 = Vector3(headPositionX, headPositionY, headPositionZ)
            Log.d(TAG, "Creating transform with vector: (${vector3.getX()}, ${vector3.getY()}, ${vector3.getZ()})")
            
            val transform = TransformBuilder.create()
                .fromTranslation(vector3)
            
            // Get the Mapping service
            val mapping = qiContext.mapping
            
            // Create a FreeFrame with the Mapping service
            val freeFrame = mapping.makeFreeFrame()
            
            // Update the target location
            freeFrame.update(robotFrame, transform, 0L)
            Log.d(TAG, "FreeFrame updated successfully")
            
            // Store the frame for later use
            targetFrame = freeFrame.frame()
            
            // Create a LookAt action
            lookAt = LookAtBuilder.with(qiContext)
                .withFrame(targetFrame)
                .build()
            
            // Set the policy to move only the head
            lookAt?.policy = LookAtMovementPolicy.HEAD_ONLY
            Log.d(TAG, "LookAt action created with HEAD_ONLY policy")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating LookAt action: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun moveHead(deltaX: Double, deltaY: Double, deltaZ: Double) {
        // Cancel any existing head movement
        cancelHeadMovement()
        
        qiContext?.let { context ->
            try {
                // Update position coordinates
                headPositionX += deltaX
                headPositionY += deltaY
                headPositionZ += deltaZ
                
                Log.d(TAG, "Moving head to position: X=${headPositionX}, Y=${headPositionY}, Z=${headPositionZ}")
                
                // Create a new LookAt action with updated position
                createLookAtAction(context)
                
                // Execute the LookAt action asynchronously
                lookAtFuture = lookAt?.async()?.run()
                
                // Add completion handler
                lookAtFuture?.thenConsume { future ->
                    if (future.hasError()) {
                        Log.e(TAG, "Error during head movement: ${future.error.message}")
                    } else if (future.isSuccess) {
                        Log.d(TAG, "Head movement completed successfully")
                    } else if (future.isCancelled) {
                        Log.d(TAG, "Head movement was canceled")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error moving head: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun cancelHeadMovement() {
        // Cancel any ongoing head movement
        lookAtFuture?.requestCancellation()
        lookAtFuture = null
        Log.d(TAG, "Canceled previous head movement")
    }
    
    /*
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
    */
}