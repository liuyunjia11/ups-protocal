// src/socket.js

class Socket {
    constructor(url) {
        this.url = url;
        this.socket = null;
    }

    connect() {
        return new Promise((resolve, reject) => {
            this.socket = new WebSocket(this.url);

            this.socket.addEventListener("open", () => {
                resolve();
            });

            this.socket.addEventListener("error", (error) => {
                reject(error);
            });
        });
    }

    // send(data) {
    //   this.socket.send(JSON.stringify(data));
    // }
    send(action, protobufMessage) {
        const payload = protobufMessage.serializeBinary();
        const data = { action, payload: Array.from(payload) };
        this.socket.send(JSON.stringify(data));
    }
    onMessage(callback) {
        this.socket.addEventListener("message", (event) => {
            const data = JSON.parse(event.data);
            callback(data);
        });
    }

    close() {
        this.socket.close();
    }
}

export default Socket;
