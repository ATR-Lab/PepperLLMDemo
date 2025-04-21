import cv2
import asyncio
import websockets
import json
import logging
import numpy as np
from datetime import datetime

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class WebcamClient:
    def __init__(self, websocket_url="ws://localhost:3000/ws/pepper"):
        self.websocket_url = websocket_url
        self.cap = None
        self.is_running = False
        self.websocket = None
        
    async def connect(self):
        """Connect to the WebSocket server"""
        try:
            self.websocket = await websockets.connect(self.websocket_url)
            logger.info("Connected to WebSocket server")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to WebSocket server: {e}")
            return False
            
    def init_camera(self, camera_id=0):
        """Initialize the webcam"""
        try:
            self.cap = cv2.VideoCapture(camera_id)
            if not self.cap.isOpened():
                raise Exception("Could not open webcam")
            logger.info("Webcam initialized successfully")
            return True
        except Exception as e:
            logger.error(f"Failed to initialize webcam: {e}")
            return False
            
    async def send_frame(self):
        """Capture and send a frame to the server"""
        if not self.cap or not self.websocket:
            return False
            
        ret, frame = self.cap.read()
        if not ret:
            logger.error("Failed to capture frame")
            return False
            
        try:
            # Encode frame to JPEG
            _, buffer = cv2.imencode('.jpg', frame)
            # Send binary frame data
            await self.websocket.send(buffer.tobytes())
            return True
        except Exception as e:
            logger.error(f"Failed to send frame: {e}")
            return False
            
    async def receive_commands(self):
        """Receive and handle commands from the server"""
        try:
            message = await self.websocket.recv()
            try:
                command = json.loads(message)
                if command.get("type") == "command":
                    self.handle_command(command)
            except json.JSONDecodeError:
                logger.warning("Received invalid JSON command")
        except Exception as e:
            logger.error(f"Error receiving command: {e}")
            
    def handle_command(self, command):
        """Handle received commands"""
        action = command.get("action")
        logger.info(f"Received command: {action}")
        
        if action == "say":
            text = command.get("text", "")
            logger.info(f"Should say: {text}")
        elif action == "animate":
            animation = command.get("animation", "")
            logger.info(f"Should perform animation: {animation}")
        elif action == "goto":
            x = command.get("x", 0)
            y = command.get("y", 0)
            theta = command.get("theta", 0)
            logger.info(f"Should move to: x={x}, y={y}, theta={theta}")
            
    async def run(self):
        """Main run loop"""
        if not self.init_camera():
            return
            
        self.is_running = True
        while self.is_running:
            try:
                if not self.websocket:
                    if not await self.connect():
                        await asyncio.sleep(5)  # Wait before retrying
                        continue
                
                # Send frame
                if not await self.send_frame():
                    continue
                    
                # Display frame locally
                ret, frame = self.cap.read()
                if ret:
                    cv2.imshow('Webcam Feed', frame)
                    
                # Check for commands
                await self.receive_commands()
                
                # Handle local window events
                key = cv2.waitKey(1) & 0xFF
                if key == ord('q'):
                    self.is_running = False
                    
                # Small delay to control frame rate
                await asyncio.sleep(0.1)
                
            except websockets.exceptions.ConnectionClosed:
                logger.warning("WebSocket connection closed. Attempting to reconnect...")
                self.websocket = None
            except Exception as e:
                logger.error(f"Error in main loop: {e}")
                await asyncio.sleep(1)
                
    def cleanup(self):
        """Cleanup resources"""
        self.is_running = False
        if self.cap:
            self.cap.release()
        cv2.destroyAllWindows()
        logger.info("Cleanup completed")

async def main():
    client = WebcamClient()
    try:
        await client.run()
    finally:
        client.cleanup()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Application terminated by user")