// WebSocket connection
let socket = null;
let frameCount = 0;
let isConnected = false;

// DOM elements
const videoCanvas = document.getElementById('videoFeed');
const ctx = videoCanvas.getContext('2d');
const connectBtn = document.getElementById('connectBtn');
const disconnectBtn = document.getElementById('disconnectBtn');
const connectionStatus = document.getElementById('connectionStatus');
const frameCountElement = document.getElementById('frameCount');
const lastCommandElement = document.getElementById('lastCommand');
const logContainer = document.getElementById('logContainer');

// Initialize the canvas
ctx.fillStyle = 'black';
ctx.fillRect(0, 0, videoCanvas.width, videoCanvas.height);
ctx.font = '20px Arial';
ctx.fillStyle = 'white';
ctx.textAlign = 'center';
ctx.fillText('No video feed', videoCanvas.width / 2, videoCanvas.height / 2);

// Add log entry
function addLog(message) {
    const logEntry = document.createElement('div');
    logEntry.textContent = `${new Date().toLocaleTimeString()}: ${message}`;
    logContainer.appendChild(logEntry);
    logContainer.scrollTop = logContainer.scrollHeight;
}

// Connect to WebSocket
function connect() {
    if (isConnected) return;
    
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/viewer`;
    
    addLog(`Connecting to ${wsUrl}...`);
    
    socket = new WebSocket(wsUrl);
    
    socket.onopen = () => {
        isConnected = true;
        connectionStatus.textContent = 'Connected';
        connectBtn.disabled = true;
        disconnectBtn.disabled = false;
        addLog('Connection established');
    };
    
    socket.onclose = () => {
        isConnected = false;
        connectionStatus.textContent = 'Disconnected';
        connectBtn.disabled = false;
        disconnectBtn.disabled = true;
        addLog('Connection closed');
    };
    
    socket.onerror = (error) => {
        addLog(`WebSocket error: ${error}`);
    };
    
    socket.onmessage = (event) => {
        if (event.data instanceof Blob) {
            // Handle binary data (image)
            const reader = new FileReader();
            reader.onload = () => {
                const img = new Image();
                img.onload = () => {
                    // Clear canvas
                    ctx.fillStyle = 'black';
                    ctx.fillRect(0, 0, videoCanvas.width, videoCanvas.height);
                    
                    // Draw image maintaining aspect ratio
                    const aspectRatio = img.width / img.height;
                    let drawWidth = videoCanvas.width;
                    let drawHeight = videoCanvas.width / aspectRatio;
                    
                    if (drawHeight > videoCanvas.height) {
                        drawHeight = videoCanvas.height;
                        drawWidth = videoCanvas.height * aspectRatio;
                    }
                    
                    const x = (videoCanvas.width - drawWidth) / 2;
                    const y = (videoCanvas.height - drawHeight) / 2;
                    
                    ctx.drawImage(img, x, y, drawWidth, drawHeight);
                    
                    // Update frame count
                    frameCount++;
                    frameCountElement.textContent = frameCount;
                };
                img.src = reader.result;
            };
            reader.readAsDataURL(event.data);
        } else {
            // Handle text data (JSON)
            try {
                const data = JSON.parse(event.data);
                
                if (data.type === 'command') {
                    lastCommandElement.textContent = JSON.stringify(data);
                    addLog(`Command: ${JSON.stringify(data)}`);
                }
            } catch (e) {
                addLog(`Error parsing message: ${e}`);
            }
        }
    };
}

// Disconnect from WebSocket
function disconnect() {
    if (!isConnected) return;
    
    socket.close();
    socket = null;
}

// Event listeners
connectBtn.addEventListener('click', connect);
disconnectBtn.addEventListener('click', disconnect);

// Auto-connect on page load
window.addEventListener('load', () => {
    setTimeout(connect, 1000);
}); 