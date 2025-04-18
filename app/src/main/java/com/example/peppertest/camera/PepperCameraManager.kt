package com.example.peppertest.camera

//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
import android.util.Log
//import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.camera.TakePicture
import com.aldebaran.qi.sdk.builder.TakePictureBuilder
import com.aldebaran.qi.sdk.`object`.image.TimestampedImageHandle
//import java.io.ByteArrayOutputStream
//import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages camera capture for Pepper robot
 */
class PepperCameraManager(private val frameListener: FrameListener) {
    companion object {
        private const val TAG = "PepperCameraManager"
        private const val TARGET_FPS = 10
        private const val FRAME_INTERVAL_MS = 1000L / TARGET_FPS
    }
    
    private var takePicture: TakePicture? = null
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var captureTask: ScheduledFuture<*>? = null
    private val isCapturing = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    
    /**
     * Initialize the camera with QiContext
     */
    fun initialize(qiContext: QiContext) {
        Log.d(TAG, "Initializing camera")
        
        // Build the take picture action
        TakePictureBuilder.with(qiContext)
            .buildAsync()
            .andThenConsume { takePicture ->
                this.takePicture = takePicture
                Log.d(TAG, "Camera initialized successfully")
            }
    }
    
    /**
     * Start capturing frames
     */
    fun startCapture() {
        if (isCapturing.getAndSet(true)) {
            Log.d(TAG, "Capture already running")
            return
        }
        
        if (takePicture == null) {
            Log.e(TAG, "Cannot start capture: camera not initialized")
            isCapturing.set(false)
            return
        }
        
        isPaused.set(false)
        
        Log.d(TAG, "Starting camera capture at $TARGET_FPS FPS")
        captureTask = executor.scheduleAtFixedRate(
            { captureFrame() },
            0,
            FRAME_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        )
    }
    
    /**
     * Stop capturing frames
     */
    fun stopCapture() {
        Log.d(TAG, "Stopping camera capture")
        captureTask?.cancel(false)
        captureTask = null
        isCapturing.set(false)
    }
    
    /**
     * Pause capturing frames (without canceling the scheduled task)
     */
    fun pauseCapture() {
        Log.d(TAG, "Pausing camera capture")
        isPaused.set(true)
    }
    
    /**
     * Resume capturing frames
     */
    fun resumeCapture() {
        Log.d(TAG, "Resuming camera capture")
        isPaused.set(false)
    }
    
    /**
     * Release resources
     */
    fun release() {
        Log.d(TAG, "Releasing camera resources")
        stopCapture()
        executor.shutdown()
        takePicture = null
    }
    
    /**
     * Capture a single frame
     */
    private fun captureFrame() {
        if (isPaused.get() || takePicture == null) {
            return
        }
        
        try {
            takePicture?.async()?.run()?.thenConsume { future ->
                if (!future.isCancelled) {
                    if (!future.hasError()) {
                        // Process the image if successful
                        processImage(future.get())
                    } else {
                        Log.e(TAG, "Error taking picture: ${future.error.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during frame capture", e)
        }
    }
    
    /**
     * Process the captured image
     */
    private fun processImage(timestampedImageHandle: TimestampedImageHandle) {
        try {
            val encodedImage = timestampedImageHandle.image.value
            val buffer = encodedImage.data
            
            // Extract the JPEG data
            buffer.rewind()
            val size = buffer.remaining()
            val imageData = ByteArray(size)
            buffer.get(imageData)
            
            // Notify the listener
            frameListener.onFrameCaptured(imageData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        }
    }
    
    /**
     * Interface for frame listeners
     */
    interface FrameListener {
        fun onFrameCaptured(imageData: ByteArray)
    }
} 