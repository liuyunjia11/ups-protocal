package org.example.Handler;

import org.example.Database.Truck;
import org.example.Utils.ConstUtils;
import org.example.controller.TruckController;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.List;

import static org.example.communication.UpsServer.truckMapper;
import static org.example.communication.UpsServer.worldClient;

public class UCommandHandler implements  Runnable{
    @Override
    public  void  run(){
        //init truck and stor into database
        TruckController truckController = new TruckController();
        List<WorldUps.UInitTruck> initTrucks = truckController.initTrucks();
        for (WorldUps.UInitTruck truck : initTrucks){
            truckMapper.insertTruck(new Truck(truck.getId(), ConstUtils.TRUCK_IDLE_STATUS));
        }

        //uconnct
        WorldUps.UConnect.Builder builder = WorldUps.UConnect.newBuilder();
        builder.setIsAmazon(false);
        builder.addAllTrucks(initTrucks);
        try {
            worldClient.writeToWorld(builder.build());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Uconnect error : " + e);
        }

        //


        while (true){
            try {
                WorldUps.UResponses uResponses = worldClient.readFromWorld();
                List<Long> uwACKList = uResponses.getAcksList();
                List<WorldUps.UDeliveryMade> uDeliveryMadeList = uResponses.getDeliveredList();
                List<WorldUps.UFinished> uFinishedList = uResponses.getCompletionsList();
                List<WorldUps.UTruck> uTruckList = uResponses.getTruckstatusList();

                for (long ack : uwACKList){

                }
                for (WorldUps.UDeliveryMade uDeliveryMade: uDeliveryMadeList){

                }
                for (WorldUps.UFinished uFinished :uFinishedList){

                }
                for (WorldUps.UTruck uTruck : uTruckList){

                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("world error : " + e);
            }
        }
    }
}
