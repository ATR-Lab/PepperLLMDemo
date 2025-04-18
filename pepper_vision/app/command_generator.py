import logging

logger = logging.getLogger(__name__)

def generate_command(detection_result: dict) -> dict:
    """
    Generate a command for the Pepper robot based on detection results.
    
    Args:
        detection_result: Dictionary with detection results from image_processor
        
    Returns:
        Command dictionary to send to the robot
    """
    # Default command (no action)
    command = {"action": "none"}
    
    # If faces are detected, prioritize looking at the largest face
    if detection_result["faces"]:
        # Sort faces by area (largest first)
        faces = sorted(
            detection_result["faces"], 
            key=lambda face: face["width"] * face["height"],
            reverse=True
        )
        largest_face = faces[0]
        
        # Calculate relative position (normalized to -1 to 1)
        image_width = detection_result["image_width"]
        image_height = detection_result["image_height"]
        
        # Calculate normalized coordinates (-1 to 1)
        center_x = largest_face["center_x"]
        center_y = largest_face["center_y"]
        norm_x = (center_x / image_width) * 2 - 1
        norm_y = (center_y / image_height) * 2 - 1
        
        # If face is centered enough, say hello
        if abs(norm_x) < 0.2 and abs(norm_y) < 0.2:
            command = {
                "action": "say",
                "text": "Hello there! I see you."
            }
        else:
            # Otherwise, look at the face
            command = {
                "action": "goto",
                "x": 0.5,  # Move forward 0.5 meters
                "y": 0.0,  # No sideways movement
                "theta": norm_x * 0.5  # Turn proportionally to face position
            }
    
    # Check for gestures
    for gesture in detection_result["gestures"]:
        if gesture["type"] == "wave" and gesture["confidence"] > 0.7:
            command = {
                "action": "animate",
                "animation": "raiseHands"
            }
    
    logger.debug(f"Generated command: {command}")
    return command 