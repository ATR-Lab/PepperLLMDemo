import time
from collections import deque
import threading
import logging

logger = logging.getLogger(__name__)

# Metrics storage
_latency_buffer = deque(maxlen=100)  # Store last 100 latency measurements
_frame_count = 0
_start_time = time.time()
_metrics_lock = threading.Lock()

def track_latency(latency_seconds: float):
    """
    Track processing latency
    
    Args:
        latency_seconds: Processing time in seconds
    """
    with _metrics_lock:
        _latency_buffer.append(latency_seconds)

def increment_frame_counter():
    """Increment the processed frame counter"""
    global _frame_count
    with _metrics_lock:
        _frame_count += 1

def get_metrics():
    """
    Get current performance metrics
    
    Returns:
        Dictionary with metrics
    """
    with _metrics_lock:
        elapsed_time = time.time() - _start_time
        fps = _frame_count / elapsed_time if elapsed_time > 0 else 0
        
        avg_latency = sum(_latency_buffer) / len(_latency_buffer) if _latency_buffer else 0
        max_latency = max(_latency_buffer) if _latency_buffer else 0
        
        return {
            "frames_processed": _frame_count,
            "uptime_seconds": elapsed_time,
            "fps": fps,
            "avg_latency": avg_latency,
            "max_latency": max_latency
        }

# Periodically log metrics
def log_metrics_periodically(interval_seconds=60):
    """
    Log metrics at regular intervals
    
    Args:
        interval_seconds: Interval between logs in seconds
    """
    def _log_metrics():
        while True:
            metrics = get_metrics()
            logger.info(f"Performance metrics: "
                       f"FPS={metrics['fps']:.2f}, "
                       f"Avg latency={metrics['avg_latency']*1000:.1f}ms, "
                       f"Frames processed={metrics['frames_processed']}")
            time.sleep(interval_seconds)
    
    thread = threading.Thread(target=_log_metrics, daemon=True)
    thread.start()
    return thread 