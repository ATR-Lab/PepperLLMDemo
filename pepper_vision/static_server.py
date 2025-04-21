import http.server
import socketserver
import logging
import os
import socket
from datetime import datetime

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Configuration
PORT = 8000
DIRECTORY = "static"

def get_ip_address():
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

class Handler(http.server.SimpleHTTPRequestHandler):
    """Custom request handler for serving files from static directory"""
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def log_message(self, format, *args):
        """Override log_message to use our logger"""
        logger.info(f"{self.client_address[0]} - {format % args}")

def main():
    # Ensure the static directory exists
    if not os.path.exists(DIRECTORY):
        logger.error(f"Static directory '{DIRECTORY}' not found. Creating it.")
        os.makedirs(DIRECTORY, exist_ok=True)
    
    # Check if index.html exists
    index_path = os.path.join(DIRECTORY, "index.html")
    if not os.path.exists(index_path):
        logger.warning(f"index.html not found in {DIRECTORY}. Please create it first.")
    
    # Get the server's IP address
    ip_address = get_ip_address()
    
    # Start the HTTP server
    with socketserver.TCPServer(("0.0.0.0", PORT), Handler) as httpd:
        logger.info(f"Static file server started at http://{ip_address}:{PORT}")
        logger.info(f"Open this URL in your browser to view the camera feed")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            logger.info("Server stopped by user")

if __name__ == "__main__":
    main() 