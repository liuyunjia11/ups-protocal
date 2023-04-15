package org.example.Receiver;

import com.google.protobuf.CodedInputStream;
import org.example.protocol.*;



import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TruckReceiver {

    private final Socket worldToUpsSocket;
    private WorldUps.UConnected.Builder uConnected;

    public TruckReceiver(Socket socket) {
        worldToUpsSocket = socket;
    }


    // =================   response from world     =================
    // =================   =====================   =================

    private WorldUps.UConnected.Builder recvFromWorldUConnected() throws IOException {
        InputStream inputStream = worldToUpsSocket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        WorldUps.UConnected uConnected = WorldUps.UConnected.parseFrom(codedInputStream.readByteArray());
        return uConnected.toBuilder();
    }

    private WorldUps.UResponses.Builder recvFromWorldUResponses() throws IOException {
        InputStream inputStream = worldToUpsSocket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        WorldUps.UResponses uResponses = WorldUps.UResponses.parseFrom(codedInputStream.readByteArray());
        System.out.println(uResponses.toBuilder().toString());
        return uResponses.toBuilder();
    }

    public void getUConnected(){
        try {
            uConnected = recvFromWorldUConnected();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WorldUps.UResponses.Builder getUResponse() throws IOException {
        return recvFromWorldUResponses();
    }


    public long getUConnectedWorldId(){
        return uConnected.getWorldid();
    }

    public String getUConnectedResult(){
        return uConnected.getResult();
    }


}
