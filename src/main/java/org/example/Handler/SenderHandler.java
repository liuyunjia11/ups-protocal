package org.example.Handler;

import org.example.communication.AmazonClient;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldUps;

import java.util.Collections;

import static org.example.communication.UpsServer.*;

public class SenderHandler implements Runnable{
    @Override
    public  void run(){
        //一直发送
        WorldUps.UCommands.Builder uCommandsBuilder = WorldUps.UCommands.newBuilder();
        UpsAmazon.UACommands.Builder uACommandsBuilder = UpsAmazon.UACommands.newBuilder();
        while (true){
            for (long seq : uGoPickupMap.keySet())
                uCommandsBuilder.addPickups(uGoPickupMap.get(seq));
            for (long seq : uGoDeliverMap.keySet())
                uCommandsBuilder.addDeliveries(uGoDeliverMap.get(seq));
            for (long seq : uaTruckArrivedMap.keySet())
                uACommandsBuilder.addTruckArrived(uaTruckArrivedMap.get(seq));
        }
    }
}
