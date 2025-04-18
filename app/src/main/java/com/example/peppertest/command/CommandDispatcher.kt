package com.example.peppertest.command

import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.GoToBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.builder.TransformBuilder
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.actuation.GoTo
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Dispatches commands to appropriate QiSDK actions
 */
class CommandDispatcher(private val qiContext: QiContext) {
    companion object {
        private const val TAG = "CommandDispatcher"
        
        // Command types
        private const val CMD_SAY = "say"
        private const val CMD_ANIMATE = "animate"
        private const val CMD_GO_TO = "goto"
    }
    
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val animationCache = ConcurrentHashMap<String, Animation>()
    
    /**
     * Dispatch a command to the appropriate handler
     */
    fun dispatch(command: JSONObject) {
        try {
            if (!command.has("action")) {
                Log.e(TAG, "Command missing 'action' field: $command")
                return
            }
            
            val action = command.getString("action")
            
            executor.submit {
                try {
                    when (action) {
                        CMD_SAY -> handleSayCommand(command)
                        CMD_ANIMATE -> handleAnimateCommand(command)
                        CMD_GO_TO -> handleGoToCommand(command)
                        else -> Log.w(TAG, "Unknown command action: $action")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing command: $action", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dispatching command", e)
        }
    }
    
    /**
     * Handle a say command
     */
    private fun handleSayCommand(command: JSONObject) {
        if (!command.has("text")) {
            Log.e(TAG, "Say command missing 'text' field")
            return
        }
        
        val text = command.getString("text")
        Log.d(TAG, "Executing say command: $text")
        
        try {
            val phrase = Phrase(text)
            val say = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .build()
                
            say.run()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing say command", e)
        }
    }
    
    /**
     * Handle an animate command
     */
    private fun handleAnimateCommand(command: JSONObject) {
        if (!command.has("animation")) {
            Log.e(TAG, "Animate command missing 'animation' field")
            return
        }
        
        val animationName = command.getString("animation")
        Log.d(TAG, "Executing animate command: $animationName")
        
        try {
            // Get or create the animation
            val animation = getOrCreateAnimation(animationName)
            if (animation == null) {
                Log.e(TAG, "Animation not found: $animationName")
                return
            }
            
            // Create and run the animate action
            val animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build()
                
            animate.run()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing animate command", e)
        }
    }
    
    /**
     * Handle a go to command
     */
    private fun handleGoToCommand(command: JSONObject) {
        try {
            if (!command.has("x") || !command.has("y") || !command.has("theta")) {
                Log.e(TAG, "GoTo command missing coordinates")
                return
            }
            
            val x = command.getDouble("x")
            val y = command.getDouble("y")
            val theta = command.getDouble("theta")
            
            Log.d(TAG, "Executing goto command: x=$x, y=$y, theta=$theta")

            // TODO: Implement
            // Create the transform
//            val transform = TransformBuilder.create()
//                .from2DTransform(x, y, theta)
//                .build()
//
//            // Create the frame
//            val mapping = qiContext.mapping
//            val robotFrame = qiContext.actuation.robotFrame()
//            val targetFrame = mapping.makeFreeFrame()
//            targetFrame.update(robotFrame, transform, 0L)
//
//            // Create and run the go to action
//            val goTo = GoToBuilder.with(qiContext)
//                .withFrame(targetFrame.frame())
//                .build()
//
//            goTo.run()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing goto command", e)
        }
    }
    
    /**
     * Get or create an animation from the cache
     */
    private fun getOrCreateAnimation(animationName: String): Animation? {
        // Check if the animation is already cached
        if (animationCache.containsKey(animationName)) {
            return animationCache[animationName]
        }
        
        // Try to load the animation from resources
        try {
            val resourceId = getAnimationResourceId(animationName)
            if (resourceId != 0) {
                val animation = AnimationBuilder.with(qiContext)
                    .withResources(resourceId)
                    .build()
                    
                animationCache[animationName] = animation
                return animation
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading animation: $animationName", e)
        }
        
        return null
    }
    
    /**
     * Get the resource ID for an animation name
     */
    private fun getAnimationResourceId(animationName: String): Int {
        // This would be implemented to map animation names to resource IDs
        // For example, "dance" -> R.raw.dance_b001
        
        // This is a placeholder implementation
        return when (animationName) {
            "dance" -> com.example.peppertest.R.raw.dance_b001
            "raiseHands" -> com.example.peppertest.R.raw.raise_both_hands_b001
            // Add more animations as needed
            else -> 0
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        executor.shutdown()
        animationCache.clear()
    }
} 