#!/bin/bash

# Create virtual environment
echo "Creating Python virtual environment..."
python3 -m venv venv

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements.txt

# Create necessary directories
echo "Creating necessary directories..."
mkdir -p logs
mkdir -p static/js

# Make sure app/utils directory exists
mkdir -p app/utils

echo "Setup complete! You can now run the service with:"
echo "source venv/bin/activate"
echo "python -m pepper_vision" 