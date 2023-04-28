package org.example.communication;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorldClient{
    private Socket socket;
    private CodedInputStream codedInputStream;
    private CodedOutputStream codedOutputStream;

    public WorldClient(String toWorldHost, int toWorldPortNum) throws IOException {
        this.socket = new Socket(toWorldHost, toWorldPortNum);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        codedInputStream = CodedInputStream.newInstance(inputStream);
        codedOutputStream = CodedOutputStream.newInstance(outputStream);

        System.out.println("connect to world");
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
        //        InputStream inputStream = worldToUpsSocket.getInputStream();
//        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
//        WorldUps.UResponses uResponses = WorldUps.UResponses.parseFrom(codedInputStream.readByteArray());
//        System.out.println(uResponses.toBuilder().toString());
//        return uResponses.toBuilder();
    }

    public WorldUps.UConnected readFromWorldConnected() throws IOException {
        return WorldUps.UConnected.parseFrom(codedInputStream.readByteArray());
    }


    public void disconnectAmz() throws IOException {
        socket.close();
    }
}
