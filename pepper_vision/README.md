# Pepper Vision System

A system for streaming camera feed from a Pepper robot to Python clients and web browsers with face detection capabilities.

## System Overview

This system consists of several components:

1. **WebSocket Server** (server.py): 
   - Acts as the central hub that receives camera frames from Pepper
   - Forwards frames to connected clients
   - Handles bidirectional communication for commands
   - Processes and forwards face detection data from Pepper

2. **Android App** (Pepper Robot):
   - Captures camera frames from Pepper's camera
   - Performs face detection using Pepper's built-in SDK
   - Sends both frames and face detection data to the server
   - Executes commands received from the server

3. **Web Interface** (static/index.html):
   - Allows viewing the camera feed in a browser
   - Provides a GUI for sending commands to Pepper
   - Displays face detection results with visual overlays
   - Controls for enabling/disabling face detection

4. **Static File Server** (static_server.py):
   - Serves the web interface

## Features

### Real-time Face Detection

The system includes real-time face detection functionality:

- **Pepper SDK processing**: Face detection runs on the Pepper robot using its built-in SDK
- **Client-side visualization**: Detected faces are highlighted on the video feed
- **Additional face data**: Shows age, gender, attention state, and smile state when available
- **Face tracking**: Each detected face is assigned a consistent ID for tracking
- **Toggle on/off**: Enable or disable face detection as needed

## Setup Instructions

### 1. Install Dependencies

```bash
# Clone the repository
git clone [repository-url]
cd pepper_vision

# Create a virtual environment (optional but recommended)
python -m venv venv

# Activate the virtual environment
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### 2. Start the Servers

The easiest way to start all components is using the provided script:

```bash
# Make the script executable (if not already)
chmod +x start.sh

# Run the script
./start.sh
```

This will:
- Start the WebSocket server on port 5001
- Start the static file server on port 8000
- Display the IP address to use for connections

### 3. Connect the Pepper Robot

To connect the Pepper robot, you need to modify the Android app to use the correct WebSocket URL:

1. Open `app/src/main/java/com/example/peppertest/MainActivity.kt`
2. Find the WebSocket URL constant:
   ```kotlin
   private const val WEBSOCKET_URL = "ws://10.22.9.224:3000/pepper"
   ```
3. Change it to point to your server IP and port 5001:
   ```kotlin
   private const val WEBSOCKET_URL = "ws://YOUR_SERVER_IP:5001/pepper"
   ```

Refer to `MainActivity.kt.guide` for more detailed instructions on modifying the Android app.

### 4. Connect from a Web Browser

To view the camera feed in a web browser:
1. Open a browser on any device on the same network
2. Navigate to: `http://YOUR_SERVER_IP:8000`
3. Click "Connect" to establish the WebSocket connection
4. You should see the Pepper camera feed and be able to send commands
5. Use the "Enable Face Detection" button to activate face detection

### 5. Use the Python Client

To run the Python client:

```bash
# Activate the virtual environment if not already activated
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate

# Run the client (it will auto-detect the server)
python webcam_client.py
```

You can also specify a server URL manually:

```bash
python webcam_client.py ws://YOUR_SERVER_IP:5001
```

## System Architecture

```
┌────────────────┐   WebSocket   ┌───────────────────────┐   WebSocket   ┌───────────────┐
│  Pepper Robot  │ ──────────► │ WebSocket Server      │ ──────────► │ Python Client  │
│  (Face Detection)│   (frames +   └───────────────────────┘   (frames)    └───────────────┘
└────────────────┘   face data)          ▲  │
                                         │  │ WebSocket
                                         │  ▼
                                   ┌───────────────────────────┐
                                   │  Web Browser + Face Display │
                                   └───────────────────────────┘
```

## Face Detection Details

The face detection system uses Pepper's built-in capabilities:

- **HumanAwareness API**: Uses Pepper's human awareness feature to detect and track people
- **3D to 2D Conversion**: Converts Pepper's 3D coordinates to 2D screen coordinates
- **Additional Attributes**: Extracts age, gender, smile state, and attention state from Pepper's SDK
- **WebSocket Protocol**: Sends face data as JSON alongside binary frame data

## Customizing

- **Frame Rate**: Adjust the frame rate in the Python client by changing the `asyncio.sleep()` value.
- **Image Quality**: Adjust JPEG compression in the Pepper app to balance between quality and bandwidth.
- **Commands**: Add new command types and handlers as needed.
- **Face Detection**: Adjust face detection interval by modifying the `FACE_DETECTION_INTERVAL_MS` constant in MainActivity.kt.

## Troubleshooting

- If the connection fails, check that all devices are on the same network.
- Ensure no firewalls are blocking the WebSocket connections on port 5001.
- Check the log files in the `logs` directory for error messages.
- If the Pepper robot can't connect, verify the correct IP address is being used.
- If face detection doesn't work, check that the QiSDK is properly initialized and the robot has focus.

## License

[Your license information here] 