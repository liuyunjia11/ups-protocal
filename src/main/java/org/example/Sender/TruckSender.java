package org.example.Sender;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.apache.ibatis.session.SqlSession;
import org.example.Database.Truck;
import org.example.Database.TruckMapper;
import org.example.MyBatisUtil;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TruckSender {
    private final Socket upsToWorldSocket;

    public TruckSender(Socket socket) {
        upsToWorldSocket = socket;
    }

    // =================   send request to world   =================
    // =================   =====================   =================
    private synchronized void sendToWorld(Message msg) throws IOException {
        OutputStream outputStream = upsToWorldSocket.getOutputStream();
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeUInt32NoTag(msg.toByteArray().length);
        msg.writeTo(codedOutputStream);
        codedOutputStream.flush();
    }

    public void sendUConnect(Long worldId, Integer numOfTruck) throws IOException {
        WorldUps.UConnect.Builder builder = WorldUps.UConnect.newBuilder();
        builder.setIsAmazon(false);

        if (worldId != null) {
            builder.setWorldid(worldId);
        } else {
            for (int i = 0; i < numOfTruck; i++) {
                WorldUps.UInitTruck.Builder uInitTruck = WorldUps.UInitTruck.newBuilder();
                uInitTruck.setX(0).setY(0).setId(i + 1);
                uInitTruck.build();
                builder.addTrucks(uInitTruck);
                //TODO: Update Truck status only if ack is received, the database part
            }
        }
        sendToWorld(builder.build());
    }


    public void sendUCommand(WorldUps.UCommands uCommands){
        try {
            sendToWorld(uCommands);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUCommandDisconnect() throws IOException {
        WorldUps.UCommands.Builder builder = WorldUps.UCommands.newBuilder();
        builder.setDisconnect(true);
        builder.build();
        sendToWorld(builder.build());
    }


    public void sendUCommandUGoPickUp(int truckId, int whId, long seqNum){

        WorldUps.UGoPickup.Builder uGoPickUpBuilder = WorldUps.UGoPickup.newBuilder();
        uGoPickUpBuilder.setSeqnum(seqNum);
        uGoPickUpBuilder.setWhid(whId);
        uGoPickUpBuilder.setTruckid(truckId);
        uGoPickUpBuilder.build();
        WorldUps.UCommands.Builder builder = WorldUps.UCommands.newBuilder();
        builder.addPickups(uGoPickUpBuilder);
        sendUCommand(builder.build());

    }

    /**
     * @param packages is from Amazon (After finishing Pack and load), which is a repeated -> addPackages
     */
    public void sendUCommandUGoDeliver(int truckId, WorldUps.UDeliveryLocation packages, long seqNum){
        WorldUps.UGoDeliver.Builder uGoDeliverBuilder = WorldUps.UGoDeliver.newBuilder();
        uGoDeliverBuilder.setTruckid(truckId);
        uGoDeliverBuilder.addPackages(packages);
        uGoDeliverBuilder.setSeqnum(seqNum);
        uGoDeliverBuilder.build();
        WorldUps.UCommands.Builder builder = WorldUps.UCommands.newBuilder();
        builder.addDeliveries(uGoDeliverBuilder);
        sendUCommand(builder.build());
    }
}
