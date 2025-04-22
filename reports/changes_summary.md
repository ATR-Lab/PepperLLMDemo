# PepperLLMDemo Changes Summary

This document summarizes the changes made to the PepperLLMDemo project to improve its functionality and fix issues.

## Overview of Changes

The following files have been modified:
1. `app/src/main/java/com/example/peppertest/MainActivity.kt`
2. `app/src/main/java/com/example/peppertest/websocket/PepperWebSocketClient.kt`
3. `app/src/main/res/layout/activity_main.xml`
4. `pepper_vision/requirements.txt`
5. `pepper_vision/server.py`
6. `pepper_vision/start.sh`

New files added:
1. `pepper_vision/speech_processor.py`
2. `pepper_vision/toggle_mic.py`

## Detailed Changes

### 1. Android Application Changes

#### MainActivity.kt
- **Added conversation tracking capabilities**:
  - Imported `ConversationStatus`, `Listen`, and `ListenResult` classes
  - Added properties to track speaking humans and speech events
  - Implemented engagement with humans based on speech detection
  - Added methods to identify which human is speaking
- **WebSocket connection improvements**:
  - Updated default WebSocket URL to use port 5003 instead of 5001
  - Added toggle functionality to enable/disable WebSocket connectivity
  - Enhanced WebSocket message handling for "speech" type messages
- **UI Enhancements**:
  - Added a status display for WebSocket connection
  - Improved error handling and status reporting

#### PepperWebSocketClient.kt
- **Enhanced message handling**:
  - Added support for "speech" type WebSocket messages
  - Improved logging of WebSocket events
  - Added fallback handling for unknown message types
  - Fixed WebSocket communication to properly relay messages from the server to Pepper

#### activity_main.xml
- **Added UI components**:
  - Added WebSocket toggle switch
  - Added container for the toggle
  - Adjusted layout constraints to accommodate new UI elements

### 2. Backend Server Changes

#### server.py
- **Fixed port conflict issues**:
  - Changed the default port from 5001 to 5002 for HTTP API
  - Configured WebSocket server to use port 5003
  - Prevented multiple servers from trying to use the same port
- **Added local microphone input support**:
  - Implemented continuous recording from the server's microphone
  - Added speech processing with Whisper for speech-to-text
  - Added LLM integration for response generation
  - Implemented thread-safe message queue for communication between threads
- **Improved error handling**:
  - Added detailed logging for better debugging
  - Added proper error recovery mechanisms

#### speech_processor.py (New)
- **Added speech processing capabilities**:
  - Voice activity detection
  - Speech-to-text transcription with Whisper
  - Integration with LLM (Language Model) for responses
  - Enhanced error logging and diagnostics
  - Fixed model name to match available Ollama models

#### toggle_mic.py (New)
- **Command-line utility to control microphone**:
  - Simple script to toggle microphone recording on/off
  - Support for enabling/disabling microphone via API
  - Command-line arguments for flexibility
  - Error handling and status reporting

#### start.sh
- **Updated server startup**:
  - Updated port references from 5001 to 5002/5003
  - Added check for PyAudio dependency
  - Added information about the new microphone toggle feature
  - Improved error handling during startup

#### requirements.txt
- **Updated dependencies**:
  - Added required version specifications for dependencies

## Summary of Key Improvements

1. **Speaker Tracking**:
   - Pepper can now focus on the person who is actively speaking
   - Uses audio, visual cues, and engagement signals to identify speakers
   - Dynamically adjusts engagement based on speaking activity

2. **Local Microphone Input**:
   - Server can now use its local microphone to capture speech
   - Processes speech through LLM pipeline and sends to Pepper
   - Provides toggle capability to enable/disable this feature

3. **WebSocket Communication**:
   - Fixed message handling for all message types
   - Added toggle to control connectivity
   - Improved error handling and reconnection logic

4. **Port Conflict Resolution**:
   - Separated HTTP and WebSocket services to different ports
   - Fixed server initialization to prevent double binding
   - Updated all references to use the new port configuration

5. **Improved Error Handling**:
   - Added detailed logging throughout the application
   - Enhanced error recovery mechanisms
   - Improved diagnostics for LLM connection issues

These changes significantly improve the reliability and functionality of the PepperLLMDemo project, enabling better human-robot interaction and more flexible deployment options. 