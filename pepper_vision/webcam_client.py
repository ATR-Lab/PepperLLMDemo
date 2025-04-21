import cv2
import asyncio
import websockets
import json
import logging
import numpy as np
import socket
from datetime import datetime
import os
import sys

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler(os.path.join("logs", f"client_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"))
    ]
)
logger = logging.getLogger(__name__)

# Ensure logs directory exists
os.makedirs("logs", exist_ok=True)

class WebcamClient:
    def __init__(self, websocket_url=None):
        """
        Initialize the webcam client
        
        Args:
            websocket_url: WebSocket server URL. If None, will attempt to auto-detect.
        """
        self.websocket_url = websocket_url
        self.is_running = False
        self.websocket = None
        self.received_frame = None
        
        # Auto-detect server if URL not provided
        if not self.websocket_url:
            self.auto_detect_server()
            
    def auto_detect_server(self):
        """Try to auto-detect the server IP on the local network"""
        try:
            # Get local IP address for interface
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            s.close()
            
            # Generate IP prefix (first 3 parts of the IP)
            ip_parts = local_ip.split('.')
            if len(ip_parts) >= 3:
                ip_prefix = '.'.join(ip_parts[0:3])
                self.websocket_url = f"ws://{local_ip}:5001"
                logger.info(f"Auto-detected server at: {self.websocket_url}")
            else:
                raise ValueError("Could not parse local IP")
        except Exception as e:
            logger.error(f"Auto-detection failed: {e}")
            # Fall back to localhost
            self.websocket_url = "ws://localhost:5001"
            logger.info(f"Falling back to: {self.websocket_url}")
            
    async def connect(self):
        """Connect to the WebSocket server"""
        try:
            logger.info(f"Connecting to {self.websocket_url}...")
            self.websocket = await websockets.connect(self.websocket_url)
            logger.info("Connected to WebSocket server")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to WebSocket server: {e}")
            return False
            
    async def send_command(self, command_type, **params):
        """Send a command to the server"""
        if not self.websocket:
            logger.error("Cannot send command: Not connected to server")
            return False
            
        try:
            command = {
                "type": "command",
                "action": command_type,
                **params
            }
            
            await self.websocket.send(json.dumps(command))
            logger.info(f"Sent command: {command_type}")
            return True
        except Exception as e:
            logger.error(f"Failed to send command: {e}")
            return False
            
    async def receive_frame(self):
        """Receive a frame from the server"""
        if not self.websocket:
            return False
            
        try:
            # Receive binary data (camera frame)
            frame_data = await self.websocket.recv()
            
            # Check if it's binary data (camera frame)
            if isinstance(frame_data, bytes):
                # Decode JPEG frame
                frame_array = np.frombuffer(frame_data, dtype=np.uint8)
                self.received_frame = cv2.imdecode(frame_array, cv2.IMREAD_COLOR)
                return True
            # If it's a text message (command or other)
            elif isinstance(frame_data, str):
                try:
                    message = json.loads(frame_data)
                    logger.info(f"Received message: {message}")
                    if message.get("type") == "command":
                        self.handle_command(message)
                except json.JSONDecodeError:
                    logger.warning("Received invalid JSON")
                return False
        except Exception as e:
            logger.error(f"Error receiving data: {e}")
            return False
            
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
        self.is_running = True
        
        while self.is_running:
            try:
                # Connect if not connected
                if not self.websocket:
                    if not await self.connect():
                        await asyncio.sleep(5)  # Wait before retrying
                        continue
                
                # Receive frame
                await self.receive_frame()
                
                # Display frame if available
                if self.received_frame is not None:
                    cv2.imshow('Pepper Camera Feed', self.received_frame)
                    
                # Handle local window events
                key = cv2.waitKey(1) & 0xFF
                if key == ord('q'):
                    self.is_running = False
                
                # Small delay to control refresh rate
                await asyncio.sleep(0.033)  # ~30 FPS
                
            except (websockets.exceptions.ConnectionClosedOK, 
                    websockets.exceptions.ConnectionClosedError,
                    websockets.exceptions.ConnectionClosed) as e:
                # Handle all types of connection closed errors
                if isinstance(e, websockets.exceptions.ConnectionClosedError):
                    logger.warning(f"WebSocket connection closed with error: code={getattr(e, 'code', 'unknown')}, reason={getattr(e, 'reason', 'unknown')}")
                else:
                    logger.info("WebSocket connection closed")
                
                self.websocket = None
                logger.info("Attempting to reconnect...")
                await asyncio.sleep(1)  # Short delay before reconnection attempt
            except Exception as e:
                logger.error(f"Error in main loop: {e}")
                self.websocket = None
                await asyncio.sleep(2)  # Longer delay on general errors
                
    def cleanup(self):
        """Cleanup resources"""
        self.is_running = False
        cv2.destroyAllWindows()
        logger.info("Cleanup completed")

async def main():
    # Allow specifying server URL as command line argument
    websocket_url = sys.argv[1] if len(sys.argv) > 1 else None
    
    client = WebcamClient(websocket_url)
    try:
        await client.run()
    finally:
        client.cleanup()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Application terminated by user")