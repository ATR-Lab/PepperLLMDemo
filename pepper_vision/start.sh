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

# Check for speech processing dependencies
echo "Checking for speech processing dependencies..."
python3 -c "import whisper; print('Whisper installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: openai-whisper package is missing. Speech-to-text functionality will not work."
    echo "Install it with: pip install openai-whisper"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

python3 -c "import torch; print('PyTorch installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: torch package is missing. Voice activity detection will not work."
    echo "Install it with: pip install torch"
fi

python3 -c "import soundfile; print('SoundFile installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: soundfile package is missing. Audio processing will not work."
    echo "Install it with: pip install soundfile"
fi

python3 -c "import pydub; print('PyDub installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: pydub package is missing. Audio format conversion will not work."
    echo "Install it with: pip install pydub"
fi

python3 -c "import pyaudio; print('PyAudio installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: pyaudio package is missing. Local microphone input will not work."
    echo "Install it with: pip install pyaudio"
    echo "Note: On Ubuntu/Debian, you may need to install portaudio first: sudo apt-get install portaudio19-dev"
fi

python3 -c "import aiohttp; print('aiohttp installed')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Warning: aiohttp package is missing. HTTP API will not work."
    echo "Install it with: pip install aiohttp"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if Ollama is running
echo "Checking if Ollama is running..."
curl -s http://localhost:11434/api/version > /dev/null
if [ $? -ne 0 ]; then
    echo "Warning: Ollama does not appear to be running or is not accessible at http://localhost:11434"
    echo "LLM functionality will not work. Please start Ollama before using speech features."
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "Ollama is running. Good!"
fi

# Get the local IP address
LOCAL_IP=$(hostname -I | awk '{print $1}')
echo "Local IP address: $LOCAL_IP"
echo "Use this IP address to connect from Pepper robot and clients"
echo "Pepper robot should connect to: ws://$LOCAL_IP:5003/pepper"
echo "Web clients can view at: http://$LOCAL_IP:8000"
echo "HTTP API endpoint: http://$LOCAL_IP:5002/api/speech"
echo "Receive API endpoint: http://$LOCAL_IP:5002/api/receive"

# Start the WebSocket server in the background
echo "Starting WebSocket server on port 5002 (WebSocket on 5003)..."
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
echo "--------------------------------------"
echo "Speech Processing Endpoints:"
echo "- To send audio for processing: POST http://$LOCAL_IP:5002/api/speech"
echo "  (Send audio file in multipart/form-data with field name 'audio')"
echo "- To send a text response to Pepper: POST http://$LOCAL_IP:5002/api/receive"
echo "  (Send JSON with 'response' and 'input_text' fields)"
echo "- To toggle local microphone: POST http://$LOCAL_IP:5002/api/toggle_mic"
echo "  (Send JSON with 'enable' field set to true or false)"
echo ""
echo "WebSocket Commands for Speech:"
echo "- To make Pepper speak: {\"type\": \"speech\", \"action\": \"say\", \"text\": \"Hello!\"}"
echo "- To start listening: {\"type\": \"speech\", \"action\": \"start_listening\"}"
echo ""
wait 