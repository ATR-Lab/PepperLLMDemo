import uvicorn
from app.utils.metrics import log_metrics_periodically

if __name__ == "__main__":
    # Start metrics logging in background
    metrics_thread = log_metrics_periodically(interval_seconds=60)
    
    # Start the FastAPI server
    uvicorn.run("app.main:app", host="0.0.0.0", port=3000, log_level="info") 