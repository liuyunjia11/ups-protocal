package org.example.controller;

import org.example.Sender.TruckSender;
import org.example.Receiver.TruckReceiver;

import java.io.IOException;
import java.net.Socket;

public class TruckController {
    public final TruckSender truckSender;
    public final TruckReceiver truckReceiver;

    public TruckController(Socket socket) {
        truckSender = new TruckSender(socket);
        truckReceiver = new TruckReceiver(socket);
    }



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



    public void sendUConnect(Long worldId, int numOfTruck) throws IOException {
        truckSender.sendUConnect(worldId, numOfTruck);
    }

    public void sendUCommandDisconnect() throws IOException {
        truckSender.sendUCommandDisconnect();
    }

    public void getUConnected() {
        truckReceiver.getUConnected();
    }

    public long getUConnectedWorldId() {
        return truckReceiver.getUConnectedWorldId();
    }

    public String getUConnectedResult() {
        return truckReceiver.getUConnectedResult();
    }
}

