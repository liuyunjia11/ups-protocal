package org.example.communication;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldAmazon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AmazonClient {
    private  Socket socket;
    private CodedInputStream codedInputStream;
    private CodedOutputStream codedOutputStream;

    public  AmazonClient(int myPortNum) throws IOException {
        socket = new ServerSocket(myPortNum).accept();
    }

    public void writeToAmz(Message msg) throws IOException {
        codedOutputStream.writeUInt32NoTag(msg.toByteArray().length);
        msg.writeTo(codedOutputStream);
        codedOutputStream.flush();
    }

    public UpsAmazon.AUCommands readFromAmz() throws IOException {
        UpsAmazon.AUCommands auCommands = UpsAmazon.AUCommands.parseFrom(codedInputStream.readByteArray());
        return auCommands;
    }



    public void disconnectAmz() throws IOException {
        socket.close();
    }
}
