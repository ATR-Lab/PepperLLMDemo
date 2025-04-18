from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.staticfiles import StaticFiles
import logging
import json
import time
import cv2
import numpy as np
from .image_processor import process_image
from .command_generator import generate_command
from .utils.logging import setup_logging
from .utils.metrics import track_latency, increment_frame_counter
import threading
import asyncio

# Setup logging
logger = setup_logging()

# Create FastAPI app
app = FastAPI(title="Pepper Vision Service")

# Mount static files for the viewer UI
app.mount("/static", StaticFiles(directory="static"), name="static")

# Store the latest image for the viewer
latest_image = None
latest_image_lock = threading.Lock()

@app.websocket("/ws/viewer")
async def viewer_websocket(websocket: WebSocket):
    await websocket.accept()
    client_id = f"viewer-{time.time()}"
    logger.info(f"New viewer connection established: {client_id}")
    
    try:
        while True:
            # Send the latest image if available
            with latest_image_lock:
                if latest_image is not None:
                    # Encode image to JPEG
                    _, buffer = cv2.imencode('.jpg', latest_image)
                    await websocket.send_bytes(buffer.tobytes())
            
            # Wait a bit before sending the next frame
            await asyncio.sleep(0.1)
            
            # Check for incoming messages (like viewer commands)
            try:
                message = await websocket.receive_json(mode="text")
                logger.debug(f"Received message from viewer: {message}")
            except:
                pass
    
    except WebSocketDisconnect:
        logger.info(f"Viewer disconnected: {client_id}")
    except Exception as e:
        logger.error(f"Viewer WebSocket error: {str(e)}")

@app.websocket("/ws/pepper")
async def pepper_websocket(websocket: WebSocket):
    await websocket.accept()
    client_id = f"pepper-{time.time()}"
    logger.info(f"New connection established: {client_id}")
    
    try:
        while True:
            # Receive message (binary or text)
            message = await websocket.receive()
            
            if "bytes" in message:
                # Start timing for latency tracking
                start_time = time.time()
                
                # Process image
                image_data = message["bytes"]
                
                # Decode JPEG to OpenCV image
                try:
                    image_array = np.frombuffer(image_data, dtype=np.uint8)
                    image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
                    
                    if image is None:
                        logger.warning(f"Failed to decode image from {client_id}")
                        continue
                    
                    # Process image and generate command
                    detection_result = process_image(image)
                    command = generate_command(detection_result)
                    
                    # Send command back
                    await websocket.send_json({
                        "type": "command",
                        **command  # This includes the "action" field and any other parameters
                    })
                    
                    # Track metrics
                    latency = time.time() - start_time
                    track_latency(latency)
                    increment_frame_counter()
                    
                    logger.debug(f"Processed frame from {client_id} in {latency:.3f}s")
                    
                    # Inside the image processing block, add:
                    with latest_image_lock:
                        latest_image = image.copy()
                    
                except Exception as e:
                    logger.error(f"Error processing image: {str(e)}")
                
            elif "text" in message:
                # Handle text messages
                try:
                    data = json.loads(message["text"])
                    logger.info(f"Received text message from {client_id}: {data}")
                    
                    # Handle different message types
                    if "type" in data:
                        if data["type"] == "status":
                            logger.info(f"Status update from {client_id}: {data}")
                        elif data["type"] == "ping":
                            await websocket.send_json({"type": "pong"})
                    
                except json.JSONDecodeError:
                    logger.warning(f"Received invalid JSON from {client_id}")
                except Exception as e:
                    logger.error(f"Error handling text message: {str(e)}")
    
    except WebSocketDisconnect:
        logger.info(f"Client disconnected: {client_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {str(e)}") 