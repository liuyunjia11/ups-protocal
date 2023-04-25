package org.example.communication;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.example.protocol.WorldUps;

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

    public void writeToWorld(Message msg) throws IOException {
        codedOutputStream.writeUInt32NoTag(msg.toByteArray().length);
        msg.writeTo(codedOutputStream);
        codedOutputStream.flush();
    }

    public WorldUps.UResponses readFromWorld() throws IOException {
        WorldUps.UResponses uResponse = WorldUps.UResponses.parseFrom(codedInputStream.readByteArray());
        return uResponse;
    }



    public void disconnectAmz() throws IOException {
        socket.close();
    }
}
