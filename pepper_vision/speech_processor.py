import logging
import tempfile
import os
import torch
import numpy as np
import soundfile as sf
import whisper
import requests
from pydub import AudioSegment
import time

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class SpeechProcessor:
    """
    Class to handle speech processing including voice activity detection,
    speech-to-text, and interaction with a language model.
    """
    def __init__(self, 
                 whisper_model_size="small", 
                 llm_url="http://localhost:11434/api/generate",
                 llm_model="gemma3:12b"):
        """
        Initialize the speech processor.

        Args:
            whisper_model_size: Size of the Whisper model to use (tiny, base, small, medium, large)
            llm_url: URL of the LLM API endpoint
            llm_model: Name of the LLM model to use
        """
        self.llm_url = llm_url
        self.llm_model = llm_model
        
        # Load Whisper model
        logger.info(f"Loading Whisper model ({whisper_model_size})...")
        try:
            self.whisper_model = whisper.load_model(whisper_model_size)
            logger.info("Whisper model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading Whisper model: {e}")
            self.whisper_model = None
        
        # Initialize VAD model
        logger.info("Loading VAD model...")
        try:
            self.vad_model, utils = torch.hub.load(repo_or_dir='snakers4/silero-vad',
                                             model='silero_vad',
                                             force_reload=False)
            self.get_speech_timestamps = utils[0]
            logger.info("VAD model loaded successfully")
        except Exception as e:
            logger.error(f"Error loading VAD model: {e}")
            self.vad_model = None
            self.get_speech_timestamps = None
    
    def check_voice_activity(self, audio_path, threshold_seconds=0.5):
        """
        Check if there's significant voice activity in the audio file.
        Returns True if voice detected for more than threshold_seconds.
        """
        if self.vad_model is None:
            logger.warning("VAD model not loaded, skipping voice check")
            return True
            
        try:
            # Load audio
            wav, sample_rate = sf.read(audio_path)
            # Convert to mono if stereo
            if len(wav.shape) > 1:
                wav = wav.mean(axis=1)
            # Convert to float32
            wav = wav.astype(np.float32)
            
            # Get speech timestamps with more aggressive settings
            speech_timestamps = self.get_speech_timestamps(
                wav, 
                self.vad_model,
                threshold=0.4,  # Lower threshold to detect more speech
                sampling_rate=sample_rate,
                min_speech_duration_ms=250,  # Detect shorter speech segments
                min_silence_duration_ms=500  # Shorter silences between words
            )
            
            # Calculate total speech duration
            total_speech_time = sum(
                (ts['end'] - ts['start']) for ts in speech_timestamps
            ) / sample_rate
            
            logger.info(f"Detected speech duration: {total_speech_time:.2f} seconds")
            return total_speech_time >= threshold_seconds
            
        except Exception as e:
            logger.error(f"Error in VAD processing: {e}")
            return True  # Default to true on error
    
    def transcribe_audio(self, audio_path):
        """
        Transcribe audio to text using Whisper.
        
        Args:
            audio_path: Path to the audio file
            
        Returns:
            Transcribed text
        """
        if self.whisper_model is None:
            logger.error("Whisper model not loaded")
            return None
            
        try:
            logger.info("Transcribing with Whisper...")
            result = self.whisper_model.transcribe(audio_path)
            text = result["text"].strip()
            logger.info(f"Transcribed text: {text}")
            return text
        except Exception as e:
            logger.error(f"Error in transcription: {e}")
            return None
    
    def get_llm_response(self, text_input, prompt_prefix="", max_tokens=100):
        """
        Get a response from the LLM for the input text.
        
        Args:
            text_input: Input text to send to the LLM
            prompt_prefix: Optional prefix to add before the text input
            max_tokens: Maximum number of tokens to generate
            
        Returns:
            LLM response text
        """
        try:
            prompt = f"{prompt_prefix}{text_input}" if prompt_prefix else text_input
            logger.info(f"Sending to LLM: {prompt}")
            
            # Prepare the request payload
            payload = {
                "model": self.llm_model,
                "prompt": prompt,
                "stream": False,
                "max_tokens": max_tokens
            }
            
            logger.info(f"LLM request: URL={self.llm_url}, Model={self.llm_model}")
            
            response = requests.post(
                self.llm_url,
                json=payload,
                timeout=30  # Add timeout to prevent hanging
            )
            
            if response.status_code != 200:
                logger.error(f"Failed to get response from LLM (Status: {response.status_code})")
                logger.error(f"Response content: {response.text}")
                logger.error(f"Request payload: {payload}")
                return None
            
            llm_response = response.json().get("response", "")
            logger.info(f"LLM response: {llm_response}")
            return llm_response
        except requests.exceptions.RequestException as e:
            logger.error(f"Error connecting to LLM service: {e}")
            logger.error(f"LLM URL: {self.llm_url}")
            logger.error(f"LLM model: {self.llm_model}")
            return None
        except Exception as e:
            logger.error(f"Error in LLM processing: {e}")
            return None
    
    def process_audio_file(self, audio_file, brief_response=True):
        """
        Process an audio file: check voice activity, transcribe, and get LLM response.
        
        Args:
            audio_file: Audio file object (from request.files)
            brief_response: Whether to request brief responses from the LLM
            
        Returns:
            Dictionary with input_text, llm_response, and processing_time
        """
        start_time = time.time()
        
        # First save the blob as is
        temp_raw_path = tempfile.mktemp(suffix='.webm')
        audio_file.save(temp_raw_path)
        
        # Convert to WAV using pydub
        try:
            logger.info(f"Converting audio from {temp_raw_path}")
            audio = AudioSegment.from_file(temp_raw_path)
            temp_wav_path = tempfile.mktemp(suffix='.wav')
            audio.export(temp_wav_path, format="wav")
            logger.info(f"Converted to WAV at {temp_wav_path}")
            
            # Clean up the raw file
            os.unlink(temp_raw_path)
            
            # Check for voice activity
            if not self.check_voice_activity(temp_wav_path):
                os.unlink(temp_wav_path)
                return {"status": "no_speech"}
                
        except Exception as e:
            if os.path.exists(temp_raw_path):
                os.unlink(temp_raw_path)
            logger.error(f"Error converting audio: {e}")
            return {"error": f"Error processing audio: {str(e)}"}
        
        # Transcribe with Whisper
        text_input = self.transcribe_audio(temp_wav_path)
        
        # Clean up the WAV file
        os.unlink(temp_wav_path)
        
        if not text_input:
            return {"error": "Could not transcribe speech - no text detected"}
        
        # Get LLM response
        prompt_prefix = "IMPORTANT: Be extremely brief. Respond with only 1-2 very short sentences. No greetings or explanations. Question: " if brief_response else ""
        llm_response = self.get_llm_response(text_input, prompt_prefix)
        
        if not llm_response:
            return {"error": "Failed to get response from LLM"}
        
        # Calculate processing time
        end_time = time.time()
        processing_time = end_time - start_time
        
        return {
            "input_text": text_input,
            "llm_response": llm_response,
            "processing_time": f"{processing_time:.2f}"
        } 