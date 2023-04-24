package org.example.Handler;

import org.example.Database.Truck;
import org.example.communication.AmazonClient;
import org.example.controller.TruckController;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldAmazon;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.List;

import static org.example.communication.UpsServer.truckController;
import static org.example.communication.UpsServer.truckMapper;

public class ACommandHandler implements  Runnable{
    private AmazonClient amazonClient;


    public  ACommandHandler(AmazonClient amazonClient){
        this.amazonClient = amazonClient;
    }
    @Override
    public void run(){
        //todo: send wordID to amz??? ack??
        long worldId = truckController.getUConnectedWorldId();
        UpsAmazon.UAConnect.Builder builder = UpsAmazon.UAConnect.newBuilder();
        builder.setWorldid(worldId);
        try {
            amazonClient.writeToAmz(builder.build());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("worldID sending error :" + e);
        }
        while (true){
            try {
                UpsAmazon.AUCommands auCommands = amazonClient.readFromAmz();
                //AUCallTruck
                List<UpsAmazon.AUCallTruck> auCallTruckList = auCommands.getCallTruckList();
                for (UpsAmazon.AUCallTruck auCallTruck : auCallTruckList){
                    Truck truck = truckMapper.getMinPackageTruck();
                    WorldUps.UGoPickup.Builder pickUpBuilder = WorldUps.UGoPickup.newBuilder();
                    pickUpBuilder.setTruckid(truck.getTruckId());
                    //seq??累加？
                    pickUpBuilder.setSeqnum()
                    //truckController.truckSender.sendUCommandUGoPickUp(truck.getTruckId(), whId, seqNum);
                }
                //load
                //ack
                //todo: more receiver
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("amazon error: " + e);
            }
        }
    }

}
