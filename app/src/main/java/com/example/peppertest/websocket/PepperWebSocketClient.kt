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
    private val serverUrl: String,
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
        .build()
    
    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    private val retryCount = AtomicInteger(0)
    private var reconnectJob: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Connect to the WebSocket server
     */
    fun connect() {
        if (isConnected.get() || isConnecting.get()) {
            Log.d(TAG, "Already connected or connecting")
            return
        }
        
        isConnecting.set(true)
        
        val request = Request.Builder()
            .url(serverUrl)
            .build()
            
        Log.d(TAG, "Connecting to WebSocket: $serverUrl")
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
                Log.d(TAG, "WebSocket connection opened")
                isConnected.set(true)
                isConnecting.set(false)
                retryCount.set(0)
                connectionStateListener.onConnected()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d(TAG, "Received message: $text")
                    val json = JSONObject(text)
                    
                    // Check if this is a command message
                    if (json.has("type") && json.getString("type") == "command") {
                        Log.d(TAG, "Received command: ${json.getString("action")}")
                        commandListener.onCommandReceived(json)
                    } else if (json.has("type") && json.getString("type") == "pong") {
                        // Handle pong response
                        Log.d(TAG, "Received pong response")
                        lastPongTime = System.currentTimeMillis()
                    } else {
                        Log.d(TAG, "Received unknown message type")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // We don't expect binary messages from the server
                Log.d(TAG, "Received binary message from server")
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(NORMAL_CLOSURE_STATUS, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected.set(false)
                isConnecting.set(false)
                connectionStateListener.onDisconnected()
                
                if (code != NORMAL_CLOSURE_STATUS) {
                    scheduleReconnect()
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure", t)
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