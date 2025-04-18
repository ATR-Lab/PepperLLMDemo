#!/usr/bin/env python3
"""
Test script for the command generator.
This script creates mock detection results and passes them to the command generator
to verify the generated commands.
"""

import json
import cv2
import numpy as np
from app.command_generator import generate_command
from app.image_processor import process_image

def test_with_mock_data():
    """Test the command generator with mock detection data"""
    print("\n=== Testing with mock detection data ===\n")
    
    # Test case 1: No faces or gestures
    detection_result = {
        "faces": [],
        "gestures": [],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 1 (No faces or gestures):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()
    
    # Test case 2: One centered face
    detection_result = {
        "faces": [
            {
                "x": 270,
                "y": 190,
                "width": 100,
                "height": 100,
                "center_x": 320,
                "center_y": 240
            }
        ],
        "gestures": [],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 2 (One centered face):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()
    
    # Test case 3: One face to the side
    detection_result = {
        "faces": [
            {
                "x": 50,
                "y": 190,
                "width": 100,
                "height": 100,
                "center_x": 100,
                "center_y": 240
            }
        ],
        "gestures": [],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 3 (One face to the side):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()
    
    # Test case 4: Multiple faces
    detection_result = {
        "faces": [
            {
                "x": 50,
                "y": 190,
                "width": 80,
                "height": 80,
                "center_x": 90,
                "center_y": 230
            },
            {
                "x": 270,
                "y": 190,
                "width": 100,
                "height": 100,
                "center_x": 320,
                "center_y": 240
            },
            {
                "x": 450,
                "y": 190,
                "width": 70,
                "height": 70,
                "center_x": 485,
                "center_y": 225
            }
        ],
        "gestures": [],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 4 (Multiple faces):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()
    
    # Test case 5: Wave gesture
    detection_result = {
        "faces": [],
        "gestures": [
            {
                "type": "wave",
                "confidence": 0.9
            }
        ],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 5 (Wave gesture):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()
    
    # Test case 6: Face and gesture
    detection_result = {
        "faces": [
            {
                "x": 270,
                "y": 190,
                "width": 100,
                "height": 100,
                "center_x": 320,
                "center_y": 240
            }
        ],
        "gestures": [
            {
                "type": "wave",
                "confidence": 0.9
            }
        ],
        "image_width": 640,
        "image_height": 480
    }
    
    command = generate_command(detection_result)
    print("Test case 6 (Face and gesture):")
    print(f"  Input: {json.dumps(detection_result)}")
    print(f"  Output: {json.dumps(command)}")
    print()

def test_with_image_file(image_path):
    """Test the command generator with a real image file"""
    print(f"\n=== Testing with image file: {image_path} ===\n")
    
    try:
        # Load the image
        image = cv2.imread(image_path)
        if image is None:
            print(f"Error: Could not load image from {image_path}")
            return
        
        # Process the image
        detection_result = process_image(image)
        
        # Generate command
        command = generate_command(detection_result)
        
        # Display results
        print(f"Detection result: {json.dumps(detection_result)}")
        print(f"Generated command: {json.dumps(command)}")
        
        # Visualize the detection (optional)
        for face in detection_result["faces"]:
            x, y, w, h = face["x"], face["y"], face["width"], face["height"]
            cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
        
        # Resize image for display if it's too large
        height, width = image.shape[:2]
        max_display_width = 800
        if width > max_display_width:
            scale = max_display_width / width
            image = cv2.resize(image, (int(width * scale), int(height * scale)))
        
        # Show the image with detections
        cv2.imshow("Detection Result", image)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        
    except Exception as e:
        print(f"Error processing image: {str(e)}")

def main():
    """Main function"""
    print("Command Generator Test Script")
    print("============================")
    
    # Test with mock data
    test_with_mock_data()
    
    # Test with image file (if provided)
    import sys
    if len(sys.argv) > 1:
        test_with_image_file(sys.argv[1])
    else:
        print("\nTo test with a real image, run:")
        print("python test_command_generator.py path/to/image.jpg")

if __name__ == "__main__":
    main() 