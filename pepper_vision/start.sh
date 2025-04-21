#!/bin/bash

# Ensure the script is run from the correct directory
cd "$(dirname "$0")"

# Create logs directory if it doesn't exist
mkdir -p logs

# Activate virtual environment if it exists
if [ -d "venv" ]; then
    echo "Activating virtual environment..."
    source venv/bin/activate
fi

# Check for required dependencies
echo "Checking for required dependencies..."
python3 -c "import websockets; print(f'websockets version: {websockets.__version__}')"
if [ $? -ne 0 ]; then
    echo "Error: websockets package not found. Please install it with: pip install websockets>=11.0.2"
    exit 1
fi

python3 -c "import cv2; print('OpenCV installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: opencv-python package might be missing. Install it with: pip install opencv-python"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Get the local IP address
LOCAL_IP=$(hostname -I | awk '{print $1}')
echo "Local IP address: $LOCAL_IP"
echo "Use this IP address to connect from Pepper robot and clients"
echo "Pepper robot should connect to: ws://$LOCAL_IP:5001/pepper"
echo "Web clients can view at: http://$LOCAL_IP:8000"

# Start the WebSocket server in the background
echo "Starting WebSocket server on port 5001..."
python3 server.py &
WS_SERVER_PID=$!

# Start the static file server in the background
echo "Starting static file server on port 8000..."
python3 static_server.py &
STATIC_SERVER_PID=$!

# Function to handle graceful shutdown
function cleanup {
    echo "Stopping servers..."
    kill $WS_SERVER_PID 2>/dev/null
    kill $STATIC_SERVER_PID 2>/dev/null
    echo "Done."
    exit
}

# Trap Ctrl+C and call cleanup
trap cleanup INT

# Wait for user to press Ctrl+C
echo ""
echo "Servers started. Press Ctrl+C to stop."
wait 