package org.example.controller;


import org.example.Utils.ConstUtils;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TruckController {
//    public final TruckSender truckSender;
//    public final TruckReceiver truckReceiver;
//
//    public TruckController(Socket socket) {
//        truckSender = new TruckSender(socket);
//        truckReceiver = new TruckReceiver(socket);
//    }



//    public CompletableFuture<WorldUps.UResponses.Builder> receiveUResponsesAsync(Consumer<WorldUps.UResponses.Builder> callback) {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                return getUResponse();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }).whenComplete((uResponses, throwable) -> {
//            if (throwable != null) {
//                throwable.printStackTrace();
//            } else if (callback != null) {
//                callback.accept(uResponses);
//            }
//        });
//    }
//



//    public void sendUConnect(Long worldId, int numOfTruck) throws IOException {
//        truckSender.sendUConnect(worldId, numOfTruck);
//    }
//
//    public void sendUCommandDisconnect() throws IOException {
//        truckSender.sendUCommandDisconnect();
//    }
//
//    public void getUConnected() {
//        truckReceiver.getUConnected();
//    }
//
//    public long getUConnectedWorldId() {
//        return truckReceiver.getUConnectedWorldId();
//    }
//
//    public String getUConnectedResult() {
//        return truckReceiver.getUConnectedResult();
//    }
    //todo : init?
    public List<WorldUps.UInitTruck> initTrucks(){
        List<WorldUps.UInitTruck> uInitTruckList = new ArrayList<>();
        for (int i = 0; i < ConstUtils.TRUCK_NUM; i++){
            Random random = new Random();
            int x= random.nextInt(101), y = random.nextInt(101);
            uInitTruckList.add(WorldUps.UInitTruck.newBuilder()
                            .setId(i).setX(x).setY(y).build());
        }
        return  uInitTruckList;
    }
}

