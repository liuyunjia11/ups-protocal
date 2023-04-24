package org.example.communication;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.net.Socket;

public class WorldClient{
    private Socket socket;
    private CodedInputStream codedInputStream;
    private CodedOutputStream codedOutputStream;

    public WorldClient(String toWorldHost, int toWorldPortNum) throws IOException {
        this.socket = new Socket(toWorldHost, toWorldPortNum);
    }

    public Socket getSocket() {
        return socket;
    }
}
