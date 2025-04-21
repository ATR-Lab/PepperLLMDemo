import asyncio
import json
import logging
import socket
import os
from datetime import datetime
import sys
import websockets

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
    def __init__(self, host="0.0.0.0", port=5001):
        self.host = host
        self.port = port
        self.clients = set()
        self.pepper_connection = None
        self.latest_frame = None
    
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
        
        try:
            async for message in websocket:
                # If message is from Pepper, it's a camera frame
                if isinstance(message, bytes):
                    # Store the latest frame
                    self.latest_frame = message
                    
                    # Forward the frame to all connected clients
                    if self.clients:
                        await asyncio.gather(
                            *[client.send(message) for client in self.clients],
                            return_exceptions=True
                        )
                # Handle JSON messages from Pepper
                elif isinstance(message, str):
                    try:
                        data = json.loads(message)
                        logger.debug(f"Received message from Pepper: {data}")
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
                        
                        # Forward command to Pepper if connected
                        if self.pepper_connection:
                            await self.pepper_connection.send(json.dumps(command))
                        else:
                            logger.warning("Cannot forward command: Pepper not connected")
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
    
    # This handler will be used by the newer websockets library
    async def router(self, websocket, request_uri):
        """Route WebSocket connections based on the request URI"""
        # This method is no longer used, keeping the signature for documentation
        pass
    
    async def start_server(self):
        """Start the WebSocket server"""
        raise NotImplementedError("Use main() directly instead of start_server method")

# Function to handle connection debugging and incoming requests
async def process_request(request, path=None):
    """
    Custom request handler for websockets 15.0.1
    In newer versions, this receives a Request object as the first parameter
    """
    # This function is no longer used
    pass

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
        
        ip_address = server.get_ip_address()
        logger.info(f"Starting WebSocket server on ws://{ip_address}:{server.port}")
        logger.info(f"Pepper robot should connect to: ws://{ip_address}:{server.port}/pepper")
        logger.info(f"Webcam clients should connect to: ws://{ip_address}:{server.port}")
        
        # Use the simplest form of server creation, which should work across versions
        server_instance = await websockets.serve(handler, server.host, server.port)
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