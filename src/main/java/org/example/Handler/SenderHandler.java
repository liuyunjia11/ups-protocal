package org.example.Handler;

import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldUps;
import org.example.protocol.UpsAmazon;

import java.io.IOException;

import static org.example.communication.UpsServer.*;

public class SenderHandler implements Runnable{
    @Override
    public  void run(){
        //一直发送
        WorldUps.UCommands.Builder uCommandsBuilder = WorldUps.UCommands.newBuilder();
        UpsAmazon.UACommands.Builder uACommandsBuilder = UpsAmazon.UACommands.newBuilder();
        while (true){
            //uw
            uCommandsBuilder.addAllAcks(uwACKSet);
            uwACKSet.clear();
            uCommandsBuilder.addAllDeliveries(uGoDeliverMap.values());
            uCommandsBuilder.addAllPickups(uGoPickupMap.values());
            uCommandsBuilder.addAllQueries(uQueryMap.values());
            //ua
            uACommandsBuilder.addAllAcks(uaACKSet);
            uaACKSet.clear();
            uACommandsBuilder.addAllUpdatePackageStatus(uaUpdatePackageStatusMap.values());
            uACommandsBuilder.addAllTruckArrived(uaTruckArrivedMap.values());
            uACommandsBuilder.addAllDelivered(uaTruckDeliverMadeMap.values());

            try {
                worldClient.writeToWorld(uCommandsBuilder.build());
                amazonClient.writeToAmz(uACommandsBuilder.build());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("send message error :" + e);
            }
        }
    }
}
