package org.example.communication;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.example.protocol.UpsAmazon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AmazonClient {
    private Socket socket;
    private CodedInputStream codedInputStream;
    private CodedOutputStream codedOutputStream;

    public AmazonClient(int myPortNum) throws IOException {
        socket = new ServerSocket(myPortNum).accept();
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        codedInputStream = CodedInputStream.newInstance(inputStream);
        codedOutputStream = CodedOutputStream.newInstance(outputStream);

        System.out.println("connect to amazon");
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

    public UpsAmazon.AUConnected readConnectFromAmz() throws IOException {
        return UpsAmazon.AUConnected.parseFrom(codedInputStream.readByteArray());
    }



    public void disconnectAmz() throws IOException {
        socket.close();
    }
}
