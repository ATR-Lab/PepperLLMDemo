package com.example.peppertest.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * WebSocket client for Pepper robot that handles:
 * - Connection management with automatic reconnection
 * - Sending camera frames as binary data
 * - Receiving and dispatching commands
 */
class PepperWebSocketClient(
    private var serverUrl: String,
    private val commandListener: CommandListener,
    private val connectionStateListener: ConnectionStateListener
) {
    companion object {
        private const val TAG = "PepperWebSocketClient"
        private const val NORMAL_CLOSURE_STATUS = 1000
        private const val MAX_RETRY_COUNT = 5
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    private val retryCount = AtomicInteger(0)
    private var reconnectJob: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastPongTime: Long = 0
    
    /**
     * Get the current server URL
     */
    fun getServerUrl(): String {
        return serverUrl
    }
    
    /**
     * Set a new server URL
     * Note: This requires disconnecting and reconnecting to take effect
     */
    fun setServerUrl(url: String) {
        Log.d(TAG, "Changing server URL from $serverUrl to $url")
        
        // Disconnect from current server if connected
        if (isConnected.get()) {
            disconnect()
        }
        
        // Update URL
        serverUrl = url
        
        // Reconnect to new URL
        connect()
    }
    
    /**
     * Connect to the WebSocket server
     */
    fun connect() {
        if (isConnected.get() || isConnecting.get()) {
            Log.d(TAG, "Already connected or connecting")
            return
        }
        
        isConnecting.set(true)
        
        // Build the request with proper headers for WebSocket connection
        val request = Request.Builder()
            .url(serverUrl)
            .header("Connection", "Upgrade")
            .header("Upgrade", "websocket")
            .header("Sec-WebSocket-Protocol", "pepper")  // Add subprotocol to help identify the client
            .build()
            
        Log.d(TAG, "Connecting to WebSocket: $serverUrl with headers: ${request.headers}")
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting from WebSocket")
        cancelReconnect()
        webSocket?.close(NORMAL_CLOSURE_STATUS, "Disconnect requested")
        webSocket = null
        isConnected.set(false)
        isConnecting.set(false)
    }
    
    /**
     * Send a camera frame as binary data
     */
    fun sendCameraFrame(imageData: ByteArray): Boolean {
        if (!isConnected.get()) {
            return false
        }
        
        return try {
            webSocket?.send(ByteString.of(*imageData)) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error sending camera frame", e)
            false
        }
    }
    
    /**
     * Send a text message to the server
     */
    fun sendMessage(message: String): Boolean {
        if (!isConnected.get()) {
            return false
        }
        
        return try {
            webSocket?.send(message) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            false
        }
    }
    
    /**
     * Create the WebSocket listener
     */
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connection opened. Response: ${response.code} ${response.message}")
                Log.d(TAG, "Response headers: ${response.headers}")
                isConnected.set(true)
                isConnecting.set(false)
                retryCount.set(0)
                
                // Send initial ping to verify connection is working
                try {
                    val pingMessage = JSONObject().apply {
                        put("type", "ping")
                        put("timestamp", System.currentTimeMillis())
                    }
                    webSocket.send(pingMessage.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending initial ping", e)
                }
                
                connectionStateListener.onConnected()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d(TAG, "Received message: $text")
                    val json = JSONObject(text)
                    
                    // Check message type
                    if (json.has("type")) {
                        val type = json.getString("type")
                        
                        // Handle different message types
                        when (type) {
                            "command" -> {
                                Log.d(TAG, "Received command: ${json.getString("action")}")
                                commandListener.onCommandReceived(json)
                            }
                            "speech" -> {
                                Log.d(TAG, "Received speech command: ${json.optString("action", "")}")
                                commandListener.onCommandReceived(json)
                            }
                            "face_detection" -> {
                                Log.d(TAG, "Received face detection command: ${json.optString("action", "")}")
                                commandListener.onCommandReceived(json)
                            }
                            "pong" -> {
                                // Handle pong response
                                Log.d(TAG, "Received pong response")
                                lastPongTime = System.currentTimeMillis()
                            }
                            else -> {
                                Log.d(TAG, "Received unknown message type: $type")
                                // Forward to command listener anyway for future compatibility
                                commandListener.onCommandReceived(json)
                            }
                        }
                    } else {
                        Log.d(TAG, "Received message without type")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // We don't expect binary messages from the server
                Log.d(TAG, "Received binary message from server: ${bytes.size} bytes")
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: code=$code, reason='$reason'")
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: code=$code, reason='$reason'")
                isConnected.set(false)
                isConnecting.set(false)
                connectionStateListener.onDisconnected()
                
                if (code != NORMAL_CLOSURE_STATUS) {
                    scheduleReconnect()
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                if (response != null) {
                    Log.e(TAG, "Response: ${response.code} ${response.message}")
                    Log.e(TAG, "Response headers: ${response.headers}")
                }
                isConnected.set(false)
                isConnecting.set(false)
                connectionStateListener.onDisconnected()
                scheduleReconnect()
            }
        }
    }
    
    /**
     * Schedule a reconnection attempt with exponential backoff
     */
    private fun scheduleReconnect() {
        cancelReconnect()
        
        val currentRetry = retryCount.incrementAndGet()
        if (currentRetry > MAX_RETRY_COUNT) {
            Log.d(TAG, "Max retry count reached, giving up")
            connectionStateListener.onReconnectFailed()
            return
        }
        
        val backoffMs = INITIAL_BACKOFF_MS * (1 shl (currentRetry - 1))
        Log.d(TAG, "Scheduling reconnect in $backoffMs ms (attempt $currentRetry)")
        
        val runnable = Runnable {
            if (!isConnected.get() && !isConnecting.get()) {
                connect()
            }
        }
        
        reconnectJob = runnable
        handler.postDelayed(runnable, backoffMs)
    }
    
    /**
     * Cancel any pending reconnection attempt
     */
    private fun cancelReconnect() {
        reconnectJob?.let {
            handler.removeCallbacks(it)
            reconnectJob = null
        }
    }
    
    /**
     * Interface for command listeners
     */
    interface CommandListener {
        fun onCommandReceived(command: JSONObject)
    }
    
    /**
     * Interface for connection state listeners
     */
    interface ConnectionStateListener {
        fun onConnected()
        fun onDisconnected()
        fun onReconnectFailed()
    }
} 