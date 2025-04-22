import asyncio
import json
import logging
import socket
import os
from datetime import datetime
import sys
import websockets
import cv2
import numpy as np
import uuid
from aiohttp import web
import aiohttp
from speech_processor import SpeechProcessor
import io
from tempfile import NamedTemporaryFile
import pyaudio
import wave
import threading
import time
import queue

# Global settings
FACE_DETECTION_ENABLED = False
LOCAL_MIC_ENABLED = False  # Toggle for local microphone

# Check websockets version and add version-specific imports
WEBSOCKETS_VERSION = websockets.__version__
logger_setup = logging.getLogger("setup")
logger_setup.setLevel(logging.INFO)
handler = logging.StreamHandler()
handler.setLevel(logging.INFO)
logger_setup.addHandler(handler)
logger_setup.info(f"Using websockets version: {WEBSOCKETS_VERSION}")

# Try to import specific components for newer versions
try:
    from websockets.exceptions import ConnectionClosedOK, ConnectionClosedError
    logger_setup.info("Successfully imported newer websockets exception classes")
except ImportError:
    # Use base exception for older versions
    ConnectionClosedOK = websockets.exceptions.ConnectionClosed
    ConnectionClosedError = websockets.exceptions.ConnectionClosed
    logger_setup.info("Using legacy websockets exception classes")

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler(os.path.join("logs", f"server_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"))
    ]
)
logger = logging.getLogger(__name__)

# Ensure logs directory exists
os.makedirs("logs", exist_ok=True)

class PepperServer:
    def __init__(self, host="0.0.0.0", port=5002):
        self.host = host
        self.port = port
        self.clients = set()
        self.pepper_connection = None
        self.latest_frame = None
        self.latest_face_data = None
        
        # Initialize the speech processor
        self.speech_processor = SpeechProcessor()
        
        # Initialize the web app for HTTP API
        self.app = web.Application()
        self.setup_routes()
        
        # Microphone recording settings
        self.mic_thread = None
        self.stop_mic_recording = threading.Event()
        self.mic_recording = False
        
        # Message queue for thread-safe communication
        self.message_queue = queue.Queue()
        
        # Flag to track if message processing is active
        self.processing_messages = False
    
    def setup_routes(self):
        """Set up the routes for the HTTP API"""
        self.app.router.add_post('/api/speech', self.handle_speech_api)
        self.app.router.add_post('/api/receive', self.handle_receive_api)
        self.app.router.add_post('/api/toggle_mic', self.handle_toggle_mic_api)
    
    async def handle_speech_api(self, request):
        """
        Handle POST requests to the /api/speech endpoint.
        This endpoint accepts audio files and processes them using the speech processor.
        """
        try:
            # Check if the request has multipart form data
            if not request.content_type.startswith('multipart/'):
                return web.json_response({"error": "Expected multipart form data"}, status=400)
            
            # Parse the multipart form data
            reader = await request.multipart()
            
            # Get the audio file field
            field = await reader.next()
            if field is None or field.name != 'audio':
                return web.json_response({"error": "No audio file provided"}, status=400)
            
            # Create a temporary file to save the audio
            with NamedTemporaryFile(suffix='.webm', delete=False) as temp_file:
                temp_path = temp_file.name
                
                # Read the audio file in chunks and write to temp file
                while True:
                    chunk = await field.read_chunk()
                    if not chunk:
                        break
                    temp_file.write(chunk)
            
            # Create a file-like object from the temp file for the speech processor
            class AudioFile:
                def __init__(self, path):
                    self.path = path
                
                def save(self, path):
                    with open(self.path, 'rb') as src, open(path, 'wb') as dst:
                        dst.write(src.read())
            
            audio_file = AudioFile(temp_path)
            
            # Process the audio file
            result = self.speech_processor.process_audio_file(audio_file)
            
            # Clean up the temp file
            os.unlink(temp_path)
            
            # Check if we have an LLM response
            if "llm_response" in result and self.pepper_connection:
                # Send the response to Pepper for TTS
                await self.pepper_connection.send(json.dumps({
                    "type": "speech",
                    "action": "say",
                    "text": result["llm_response"]
                }))
            
            # Return the result
            return web.json_response(result)
            
        except Exception as e:
            logger.error(f"Error processing speech API request: {e}")
            return web.json_response({"error": str(e)}, status=500)
    
    async def handle_receive_api(self, request):
        """
        Handle POST requests to the /api/receive endpoint.
        This endpoint accepts JSON with response and input_text fields.
        """
        try:
            # Get the JSON data
            data = await request.json()
            
            # Extract the response and input text
            response_text = data.get("response")
            input_text = data.get("input_text")
            
            if not response_text:
                return web.json_response({"error": "No response text provided"}, status=400)
            
            logger.info(f"Received response via API: '{response_text}' (input: '{input_text}')")
            
            # If Pepper is connected, send the response for TTS
            if self.pepper_connection:
                await self.pepper_connection.send(json.dumps({
                    "type": "speech",
                    "action": "say",
                    "text": response_text
                }))
                return web.json_response({"status": "success", "message": "Response sent to Pepper"})
            else:
                return web.json_response({"status": "error", "message": "Pepper not connected"}, status=503)
                
        except Exception as e:
            logger.error(f"Error processing receive API request: {e}")
            return web.json_response({"error": str(e)}, status=500)
    
    async def handle_toggle_mic_api(self, request):
        """
        Handle POST requests to the /api/toggle_mic endpoint.
        This endpoint toggles the local microphone recording.
        """
        global LOCAL_MIC_ENABLED
        
        try:
            data = await request.json()
            enable = data.get("enable")
            
            if enable is None:
                # Toggle if not specified
                enable = not LOCAL_MIC_ENABLED
            
            if enable:
                if not self.mic_recording:
                    # Start microphone recording
                    self.start_microphone_recording()
                    LOCAL_MIC_ENABLED = True
                    return web.json_response({"status": "success", "mic_enabled": True})
                else:
                    return web.json_response({"status": "info", "message": "Microphone already recording", "mic_enabled": True})
            else:
                if self.mic_recording:
                    # Stop microphone recording
                    self.stop_microphone_recording()
                    LOCAL_MIC_ENABLED = False
                    return web.json_response({"status": "success", "mic_enabled": False})
                else:
                    return web.json_response({"status": "info", "message": "Microphone already stopped", "mic_enabled": False})
                
        except Exception as e:
            logger.error(f"Error toggling microphone: {e}")
            return web.json_response({"error": str(e)}, status=500)
    
    def start_microphone_recording(self):
        """Start recording from the local microphone"""
        if self.mic_recording:
            logger.warning("Microphone recording already active")
            return
            
        # Reset the stop event
        self.stop_mic_recording.clear()
        
        # Start a new thread for microphone recording
        self.mic_thread = threading.Thread(target=self._microphone_recording_thread)
        self.mic_thread.daemon = True
        self.mic_thread.start()
        
        self.mic_recording = True
        logger.info("Started local microphone recording")
    
    def stop_microphone_recording(self):
        """Stop recording from the local microphone"""
        if not self.mic_recording:
            logger.warning("No microphone recording to stop")
            return
            
        # Signal the recording thread to stop
        self.stop_mic_recording.set()
        
        # Wait for the thread to finish
        if self.mic_thread:
            self.mic_thread.join(timeout=2.0)
            self.mic_thread = None
        
        self.mic_recording = False
        logger.info("Stopped local microphone recording")
    
    def _microphone_recording_thread(self):
        """Thread function for recording from the microphone"""
        try:
            # PyAudio setup
            CHUNK = 1024
            FORMAT = pyaudio.paInt16
            CHANNELS = 1
            RATE = 16000
            RECORD_SECONDS = 5  # Record in 5-second chunks
            
            p = pyaudio.PyAudio()
            
            # Open stream
            stream = p.open(format=FORMAT,
                            channels=CHANNELS,
                            rate=RATE,
                            input=True,
                            frames_per_buffer=CHUNK)
            
            logger.info("Microphone stream opened, listening...")
            
            while not self.stop_mic_recording.is_set():
                # Record audio for RECORD_SECONDS
                frames = []
                
                for _ in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
                    # Check if we should stop
                    if self.stop_mic_recording.is_set():
                        break
                        
                    # Read audio data
                    data = stream.read(CHUNK, exception_on_overflow=False)
                    frames.append(data)
                
                # Process the recording if we have enough data
                if len(frames) > 0:
                    # Save to temp file
                    with NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
                        temp_path = temp_file.name
                    
                    wf = wave.open(temp_path, 'wb')
                    wf.setnchannels(CHANNELS)
                    wf.setsampwidth(p.get_sample_size(FORMAT))
                    wf.setframerate(RATE)
                    wf.writeframes(b''.join(frames))
                    wf.close()
                    
                    # Create a file-like object for the speech processor
                    class AudioFile:
                        def __init__(self, path):
                            self.path = path
                        
                        def save(self, path):
                            with open(self.path, 'rb') as src, open(path, 'wb') as dst:
                                dst.write(src.read())
                    
                    audio_file = AudioFile(temp_path)
                    
                    # Process the audio in a separate thread to not block microphone recording
                    threading.Thread(
                        target=self._process_microphone_audio,
                        args=(audio_file,),
                        daemon=True
                    ).start()
            
            # Clean up when stopped
            stream.stop_stream()
            stream.close()
            p.terminate()
            logger.info("Microphone stream closed")
            
        except Exception as e:
            logger.error(f"Error in microphone recording thread: {e}")
            self.mic_recording = False
    
    def _process_microphone_audio(self, audio_file):
        """Process audio from the local microphone"""
        try:
            # Process the audio
            result = self.speech_processor.process_audio_file(audio_file)
            
            # Clean up temp file
            if hasattr(audio_file, 'path') and os.path.exists(audio_file.path):
                os.unlink(audio_file.path)
            
            # If no speech detected, just return
            if result.get("status") == "no_speech" or "error" in result:
                return
            
            # Log the result
            logger.info(f"Local microphone speech detected: {result.get('input_text')}")
            
            # If we have an LLM response, queue it for sending to Pepper
            if "llm_response" in result:
                message = {
                    "type": "speech",
                    "action": "say",
                    "text": result["llm_response"]
                }
                
                # Add message to the queue for the main thread to process
                self.message_queue.put(message)
                logger.info(f"Queued message for Pepper: {result.get('llm_response')}")
                
        except Exception as e:
            logger.error(f"Error processing microphone audio: {e}")
            logger.exception("Full traceback:")
    
    async def process_message_queue(self):
        """Process messages from the queue and send them to Pepper"""
        self.processing_messages = True
        
        try:
            while True:
                # Check if there's a message in the queue
                try:
                    # Non-blocking get with timeout
                    message = self.message_queue.get(block=False)
                    
                    # Send the message if Pepper is connected
                    if self.pepper_connection:
                        try:
                            await self.pepper_connection.send(json.dumps(message))
                            logger.info(f"Sent message to Pepper: {message.get('text')}")
                        except Exception as e:
                            logger.error(f"Error sending message to Pepper: {e}")
                    else:
                        logger.warning(f"Pepper not connected, couldn't send: {message.get('text')}")
                    
                    # Mark the task as done
                    self.message_queue.task_done()
                    
                except queue.Empty:
                    # No messages in queue, wait before checking again
                    pass
                
                # Small delay to prevent CPU hogging
                await asyncio.sleep(0.1)
                
        except asyncio.CancelledError:
            # Task was cancelled, clean up
            self.processing_messages = False
            logger.info("Message queue processing stopped")
        except Exception as e:
            self.processing_messages = False
            logger.error(f"Error in message queue processing: {e}")
            logger.exception("Full traceback:")
    
    def get_ip_address(self):
        """Get the local IP address for network connectivity"""
        try:
            # Create a temporary socket to determine IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            return ip
        except Exception as e:
            logger.error(f"Failed to get IP address: {e}")
            return "localhost"
    
    async def handle_pepper(self, websocket):
        """Handle WebSocket connections from Pepper robot"""
        client_id = f"pepper-{id(websocket)}"
        logger.info(f"Pepper robot connected: {client_id}")
        
        # Store the Pepper connection
        self.pepper_connection = websocket
        
        # Start processing the message queue if not already running
        if not self.processing_messages:
            asyncio.create_task(self.process_message_queue())
        
        try:
            async for message in websocket:
                # If message is from Pepper, it's a camera frame or JSON data
                if isinstance(message, bytes):
                    # Check if it's an audio message (we need to implement a protocol for this)
                    # For now, assume all binary messages are camera frames
                    
                    # Store the latest frame
                    self.latest_frame = message
                    
                    # Forward the frame to all connected clients
                    if self.clients:
                        await asyncio.gather(
                            *[client.send(message) for client in self.clients],
                            return_exceptions=True
                        )
                        
                        # If we have face data from Pepper, send it after the frame
                        if self.latest_face_data and FACE_DETECTION_ENABLED:
                            await asyncio.gather(
                                *[client.send(json.dumps(self.latest_face_data)) for client in self.clients],
                                return_exceptions=True
                            )
                
                # Handle JSON messages from Pepper (commands and face detection data)
                elif isinstance(message, str):
                    try:
                        data = json.loads(message)
                        logger.debug(f"Received message from Pepper: {data}")
                        
                        # If message is face detection data
                        if data.get("type") == "face_detection" and data.get("source") == "pepper_sdk":
                            logger.info(f"Received face detection data from Pepper: {len(data.get('faces', []))} faces")
                            
                            # Store the latest face data
                            self.latest_face_data = data
                            
                            # Forward face data to clients if face detection is enabled
                            if self.clients and FACE_DETECTION_ENABLED:
                                await asyncio.gather(
                                    *[client.send(message) for client in self.clients],
                                    return_exceptions=True
                                )
                        # If it's an audio message with text (from speech recognition on Pepper)
                        elif data.get("type") == "speech" and data.get("action") == "recognized":
                            text_input = data.get("text")
                            if text_input:
                                logger.info(f"Received recognized speech from Pepper: '{text_input}'")
                                
                                # Process with LLM
                                llm_response = self.speech_processor.get_llm_response(
                                    text_input, 
                                    prompt_prefix="IMPORTANT: Be extremely brief. Respond with only 1-2 very short sentences. No greetings or explanations. Question: "
                                )
                                
                                if llm_response:
                                    # Send the response back to Pepper for TTS
                                    await websocket.send(json.dumps({
                                        "type": "speech",
                                        "action": "say",
                                        "text": llm_response
                                    }))
                        # Other types of messages
                        else:
                            logger.debug(f"Received other message type: {data.get('type')}")
                    except json.JSONDecodeError:
                        logger.warning(f"Received invalid JSON from Pepper")
        
        except ConnectionClosedOK:
            logger.info(f"Pepper connection closed gracefully: {client_id}")
        except ConnectionClosedError as e:
            logger.warning(f"Pepper connection closed with error: {client_id}, code: {getattr(e, 'code', 'unknown')}, reason: {getattr(e, 'reason', 'unknown')}")
        except Exception as e:
            logger.error(f"Error handling Pepper client {client_id}: {e}")
            logger.exception("Full error traceback:")
        finally:
            # Clean up on disconnect
            logger.info("Pepper robot disconnected")
            self.pepper_connection = None
    
    async def handle_client(self, websocket):
        """Handle WebSocket connections from regular clients"""
        global FACE_DETECTION_ENABLED
        
        client_id = f"client-{id(websocket)}"
        logger.info(f"Webcam client connected: {client_id}")
        
        # Add to regular clients
        self.clients.add(websocket)
        
        try:
            async for message in websocket:
                # Handle messages from clients (commands to send to Pepper)
                try:
                    if isinstance(message, str):
                        command = json.loads(message)
                        logger.info(f"Received command from client {client_id}: {command}")
                        
                        # Handle face detection toggle command
                        if command.get("type") == "face_detection":
                            action = command.get("action")
                            if action == "enable":
                                FACE_DETECTION_ENABLED = True
                                logger.info("Face detection enabled")
                                # Send confirmation to the client
                                await websocket.send(json.dumps({"type": "face_detection", "status": "enabled"}))
                                
                                # Forward the face detection enable command to Pepper
                                if self.pepper_connection:
                                    logger.info("Forwarding face detection enable command to Pepper")
                                    await self.pepper_connection.send(json.dumps(command))
                                
                                # If we have face data and just enabled detection, send it
                                if self.latest_face_data:
                                    await websocket.send(json.dumps(self.latest_face_data))
                            elif action == "disable":
                                FACE_DETECTION_ENABLED = False
                                logger.info("Face detection disabled")
                                # Send confirmation to the client
                                await websocket.send(json.dumps({"type": "face_detection", "status": "disabled"}))
                                
                                # Forward the face detection disable command to Pepper
                                if self.pepper_connection:
                                    logger.info("Forwarding face detection disable command to Pepper")
                                    await self.pepper_connection.send(json.dumps(command))
                        
                        # Handle speech commands
                        elif command.get("type") == "speech":
                            action = command.get("action")
                            
                            # Command to make Pepper say something
                            if action == "say" and command.get("text"):
                                text = command.get("text")
                                logger.info(f"Received say command: '{text}'")
                                
                                # Forward to Pepper if connected
                                if self.pepper_connection:
                                    logger.info(f"Forwarding say command to Pepper: '{text}'")
                                    await self.pepper_connection.send(json.dumps(command))
                                else:
                                    logger.warning("Cannot forward say command: Pepper not connected")
                            
                            # Command to start listening
                            elif action == "start_listening":
                                logger.info("Received start_listening command")
                                
                                # Forward to Pepper if connected
                                if self.pepper_connection:
                                    logger.info("Forwarding start_listening command to Pepper")
                                    await self.pepper_connection.send(json.dumps(command))
                                else:
                                    logger.warning("Cannot forward start_listening command: Pepper not connected")
                        
                        # Forward other commands to Pepper if connected
                        elif self.pepper_connection:
                            await self.pepper_connection.send(json.dumps(command))
                        else:
                            logger.warning("Cannot forward command: Pepper not connected")
                    # Handle binary messages (likely audio data)
                    elif isinstance(message, bytes):
                        logger.info(f"Received binary data from client {client_id}")
                        
                        # Save to temp file
                        with NamedTemporaryFile(suffix='.webm', delete=False) as temp_file:
                            temp_path = temp_file.name
                            temp_file.write(message)
                        
                        # Create a file-like object
                        class AudioFile:
                            def __init__(self, path):
                                self.path = path
                            
                            def save(self, path):
                                with open(self.path, 'rb') as src, open(path, 'wb') as dst:
                                    dst.write(src.read())
                        
                        audio_file = AudioFile(temp_path)
                        
                        # Process asynchronously to not block WebSocket
                        asyncio.create_task(self.process_client_audio(audio_file, websocket))
                        
                except json.JSONDecodeError:
                    logger.warning(f"Received invalid command from client {client_id}")
                except Exception as e:
                    logger.error(f"Error processing client message: {e}")
        
        except ConnectionClosedOK:
            logger.info(f"Client connection closed gracefully: {client_id}")
        except ConnectionClosedError as e:
            logger.warning(f"Client connection closed with error: {client_id}, code: {getattr(e, 'code', 'unknown')}, reason: {getattr(e, 'reason', 'unknown')}")
        except Exception as e:
            logger.error(f"Error handling client {client_id}: {e}")
            logger.exception("Full error traceback:")
        finally:
            # Clean up on disconnect
            self.clients.remove(websocket)
            logger.info(f"Client {client_id} disconnected, {len(self.clients)} clients remaining")
    
    async def process_client_audio(self, audio_file, client_websocket):
        """Process audio data from a client asynchronously"""
        try:
            # Process the audio
            result = self.speech_processor.process_audio_file(audio_file)
            
            # Clean up temp file
            if hasattr(audio_file, 'path') and os.path.exists(audio_file.path):
                os.unlink(audio_file.path)
            
            # Send the result back to the client
            await client_websocket.send(json.dumps(result))
            
            # If we have an LLM response and Pepper is connected, send to Pepper
            if "llm_response" in result and self.pepper_connection:
                await self.pepper_connection.send(json.dumps({
                    "type": "speech",
                    "action": "say",
                    "text": result["llm_response"]
                }))
                
        except Exception as e:
            logger.error(f"Error processing client audio: {e}")
            # Try to send error back to client
            try:
                await client_websocket.send(json.dumps({"error": str(e)}))
            except:
                pass
    
    # This handler will be used by the newer websockets library
    async def router(self, websocket, request_uri):
        """Route WebSocket connections based on the request URI"""
        # This method is no longer used, keeping the signature for documentation
        pass
    
    async def start_server(self):
        """Start the WebSocket server and HTTP API server"""
        ip_address = self.get_ip_address()
        
        # Create a runner for the web app
        runner = web.AppRunner(self.app)
        await runner.setup()
        
        # Create a site for the app
        site = web.TCPSite(runner, self.host, self.port)
        
        # Start the HTTP server
        await site.start()
        logger.info(f"HTTP API server started on http://{ip_address}:{self.port}")
        
        # Start the WebSocket server
        async def websocket_handler(websocket, path):
            # Determine if this is a Pepper connection or a client
            if path == "/pepper":
                await self.handle_pepper(websocket)
            else:
                await self.handle_client(websocket)
        
        # Get the version of websockets
        major_version = int(WEBSOCKETS_VERSION.split('.')[0])
        
        if major_version >= 10:
            # For newer versions of websockets
            async with websockets.serve(websocket_handler, self.host, self.port + 1):
                logger.info(f"WebSocket server started on ws://{ip_address}:{self.port + 1}")
                logger.info(f"Pepper robot should connect to: ws://{ip_address}:{self.port + 1}/pepper")
                logger.info(f"Webcam clients should connect to: ws://{ip_address}:{self.port + 1}")
                
                # Keep the server running
                await asyncio.Future()
        else:
            # For older versions of websockets
            server = await websockets.serve(websocket_handler, self.host, self.port + 1)
            logger.info(f"WebSocket server started on ws://{ip_address}:{self.port + 1}")
            logger.info(f"Pepper robot should connect to: ws://{ip_address}:{self.port + 1}/pepper")
            logger.info(f"Webcam clients should connect to: ws://{ip_address}:{self.port + 1}")
            
            # Keep the server running
            await asyncio.Future()

        # Start local microphone if enabled
        global LOCAL_MIC_ENABLED
        if LOCAL_MIC_ENABLED:
            self.start_microphone_recording()

async def main():
    server = PepperServer()
    
    try:
        # More sophisticated request handler
        async def handler(websocket):
            try:
                # In websockets 15.0.1, paths are accessed differently
                path = None
                
                # Try different ways to get the path based on the websocket object type
                if hasattr(websocket, 'path'):
                    path = websocket.path
                elif hasattr(websocket, 'request'):
                    path = websocket.request.path
                elif hasattr(websocket, 'request_uri'):
                    path = websocket.request_uri.path
                
                # If we couldn't determine the path, check protocol
                if path is None and hasattr(websocket, 'subprotocols') and websocket.subprotocols:
                    if 'pepper' in websocket.subprotocols:
                        path = '/pepper'
                    else:
                        path = '/'
                
                # Default to root if we couldn't determine the path
                if path is None:
                    path = '/'
                    
                logger.info(f"New WebSocket connection established for path: {path}")
                
                # Process based on path
                if path == "/pepper":
                    logger.info(f"Processing Pepper robot connection")
                    await server.handle_pepper(websocket)
                else:
                    logger.info(f"Processing client connection for path: {path}")
                    await server.handle_client(websocket)
            except AttributeError as e:
                # If we can't determine the path at all, default to client handler
                logger.error(f"Connection error (likely websocket version incompatibility): {e}")
                logger.info("Falling back to default client handler")
                await server.handle_client(websocket)
            except Exception as e:
                logger.error(f"Unexpected error in connection handler: {e}")
                logger.exception("Full traceback:")
        
        # Get the ip address for display purposes
        ip_address = server.get_ip_address()
        
        # Start the HTTP API server
        runner = web.AppRunner(server.app)
        await runner.setup()
        site = web.TCPSite(runner, server.host, server.port)
        await site.start()
        
        logger.info(f"HTTP API server started on http://{ip_address}:{server.port}")
        logger.info(f"  - Speech API endpoint: http://{ip_address}:{server.port}/api/speech")
        logger.info(f"  - Receive API endpoint: http://{ip_address}:{server.port}/api/receive")
        
        # Start WebSocket server on a different port (port+1)
        ws_port = server.port + 1
        logger.info(f"Starting WebSocket server on ws://{ip_address}:{ws_port}")
        logger.info(f"Pepper robot should connect to: ws://{ip_address}:{ws_port}/pepper")
        logger.info(f"Webcam clients should connect to: ws://{ip_address}:{ws_port}")
        
        # Use the simplest form of server creation, which should work across versions
        server_instance = await websockets.serve(handler, server.host, ws_port)
        logger.info(f"Server started successfully using websockets {WEBSOCKETS_VERSION}")
        
        # Keep the server running
        await asyncio.Future()
    except Exception as e:
        logger.error(f"Error starting server: {e}")
        logger.exception("Full error traceback:")
        sys.exit(1)

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Server stopped by user")
    except Exception as e:
        logger.error(f"Server error: {e}")
        sys.exit(1) 