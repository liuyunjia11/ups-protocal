package org.example.Handler;

import org.example.communication.AmazonClient;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.Collections;

import static org.example.communication.UpsServer.*;

public class SenderHandler implements Runnable{
    @Override
    public  void run() {
        //一直发送
        WorldUps.UCommands.Builder uCommandsBuilder = WorldUps.UCommands.newBuilder();
        UpsAmazon.UACommands.Builder uACommandsBuilder = UpsAmazon.UACommands.newBuilder();
        while (true) {
            //amazon
            uACommandsBuilder.addAllTruckArrived(uaTruckArrivedMap.values());
            //uCommandsBuilder.addAllAcks(uaACKMap.values());
            uACommandsBuilder.addAllUpdatePackageStatus(uaUpdatePackageStatusMap.values());
            //world
            uCommandsBuilder.addAllPickups(uGoPickupMap.values());
            uCommandsBuilder.addAllDeliveries(uGoDeliverMap.values());
            //uCommandsBuilder.addAllAcks(uwACKMap.values());
            try {
                amazonClient.writeToAmz(uACommandsBuilder.build());
                //world write todo
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("sending error: " + e);
            }
        }
    }
}
