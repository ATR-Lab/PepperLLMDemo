#!/usr/bin/env python3
"""
Simple script to toggle the local microphone input for the Pepper Vision server.
This sends a request to the /api/toggle_mic endpoint.
"""

import requests
import json
import sys
import argparse

def toggle_microphone(server_url, enable=None):
    """
    Toggle the microphone input on the server.
    
    Args:
        server_url: URL of the server
        enable: True to enable, False to disable, None to toggle
    
    Returns:
        True if successful, False otherwise
    """
    api_url = f"{server_url}/api/toggle_mic"
    
    # Prepare the request payload
    payload = {}
    if enable is not None:
        payload["enable"] = enable
    
    try:
        # Send the request
        response = requests.post(api_url, json=payload)
        
        # Parse the response
        result = response.json()
        
        if response.status_code == 200:
            status = result.get("status")
            mic_enabled = result.get("mic_enabled", False)
            
            if status == "success":
                print(f"Microphone {'enabled' if mic_enabled else 'disabled'} successfully")
            else:
                print(f"Server message: {result.get('message')}")
                
            return True
        else:
            print(f"Error: {result.get('error', 'Unknown error')}")
            return False
            
    except requests.RequestException as e:
        print(f"Connection error: {e}")
        return False
    except Exception as e:
        print(f"Error: {e}")
        return False

def main():
    """Main function"""
    parser = argparse.ArgumentParser(description="Toggle the Pepper Vision server's local microphone")
    parser.add_argument("--server", default="http://localhost:5002", help="Server URL (default: http://localhost:5002)")
    parser.add_argument("--enable", action="store_true", help="Enable the microphone")
    parser.add_argument("--disable", action="store_true", help="Disable the microphone")
    
    args = parser.parse_args()
    
    # Determine if we're enabling, disabling, or toggling
    enable = None
    if args.enable and args.disable:
        print("Error: Cannot use both --enable and --disable")
        return 1
    elif args.enable:
        enable = True
    elif args.disable:
        enable = False
    
    # Toggle the microphone
    success = toggle_microphone(args.server, enable)
    
    return 0 if success else 1

if __name__ == "__main__":
    sys.exit(main()) 