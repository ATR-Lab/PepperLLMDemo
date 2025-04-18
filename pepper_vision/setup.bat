@echo off
echo Creating Python virtual environment...
python -m venv venv

echo Activating virtual environment...
call venv\Scripts\activate

echo Installing dependencies...
pip install -r requirements.txt

echo Creating necessary directories...
mkdir logs
mkdir static\js
mkdir app\utils

echo Setup complete! You can now run the service with:
echo venv\Scripts\activate
echo python -m pepper_vision 