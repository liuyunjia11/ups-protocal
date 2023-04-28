package org.example.Handler;

import org.apache.ibatis.session.SqlSession;
import org.example.Database.*;
import org.example.Utils.ConstUtils;
import org.example.Utils.MyBatisUtil;
import org.example.Utils.SeqUtils;
import org.example.controller.TruckController;
import org.example.protocol.*;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;


import static org.example.communication.UpsServer.*;
import static org.example.communication.UpsServer.worldClient;

public class WCommandHandler implements  Runnable{
    SeqUtils seqUtils = SeqUtils.getSeqUtils();
    void handleAcks(long ack){
        if (uGoPickupMap.containsKey(ack)){
            truckMapper.updateTruckStatus(ConstUtils.TRUCK_TRAVEL_STATUS, uGoPickupMap.get(ack).getTruckid());
        }
        if (uGoDeliverMap.containsKey(ack)){
            truckMapper.updateTruckStatus(ConstUtils.TRUCK_DELIVER_STATUS, uGoDeliverMap.get(ack).getTruckid());
            packagesMapper.updatePackageStatusByTruckId(ConstUtils.PACKAGE_DELIVERING, uGoPickupMap.get(ack).getTruckid());
        }
        uGoPickupMap.remove(ack);
        uGoDeliverMap.remove(ack);
        uQueryMap.remove(ack);
        sqlSession.commit();
    }
    void handleUDeliveryMade(WorldUps.UDeliveryMade uDeliveryMade){
        seqUtils.accumulate();
        UpsAmazon.UATruckDeliverMade.Builder builder = UpsAmazon.UATruckDeliverMade.newBuilder();
        builder.setPackageid(uDeliveryMade.getPackageid()).setTruckid(uDeliveryMade.getTruckid()).setSeqnum(seqUtils.getValue());
        //ack
        uwACKSet.add(uDeliveryMade.getSeqnum());
        uaTruckDeliverMadeMap.put(seqUtils.getValue(), builder.build());
        //databse
        truckMapper.updateTruckStatus(ConstUtils.TRUCK_IDLE_STATUS, uDeliveryMade.getTruckid());
        Packages packages = packagesMapper.getPackageById((int) uDeliveryMade.getPackageid());
        packages.setStatus(ConstUtils.PACKAGE_DELIVERED);
        packagesMapper.updatePackage(packages);
        sqlSession.commit();
    }
    void handleUFinished(WorldUps.UFinished uFinished){
        seqUtils.accumulate();
        UpsAmazon.UATruckArrived.Builder builder = UpsAmazon.UATruckArrived.newBuilder();
        builder.setSeqnum(seqUtils.getValue()).setTruckid(uFinished.getTruckid()).setX(uFinished.getX()).setY(uFinished.getY());
        uaTruckArrivedMap.put(seqUtils.getValue(), builder.build());
        //ack
        uwACKSet.add(uFinished.getSeqnum());
        //database
        truckMapper.updateTruckStatus(ConstUtils.TRUCK_ARRIVE_WAREHOUSE_STATUS,uFinished.getTruckid());
        sqlSession.commit();
    }
    public  void handleUTruck(WorldUps.UTruck uTruck){
        uwACKSet.add(uTruck.getSeqnum());
        int currX = uTruck.getX(), currY = uTruck.getY();
        for (long packageID : packageStatusMap.getOrDefault(uTruck.getTruckid(), new HashSet<>())){
            seqUtils.accumulate();
            UpsAmazon.UAUpdatePackageStatus.Builder builder = UpsAmazon.UAUpdatePackageStatus.newBuilder();
            builder.setSeqnum(seqUtils.getValue()).setPackageid(packageID).setStatus(uTruck.getStatus()).setX(currX).setY(currY);
            uaUpdatePackageStatusMap.put(seqUtils.getValue(), builder.build());
        }
    }
    @Override
    public  void  run(){
        //init truck and stor into database
        TruckController truckController = new TruckController();
        List<WorldUps.UInitTruck> initTrucks = truckController.initTrucks();

        //uconnct
        WorldUps.UConnect.Builder builder = WorldUps.UConnect.newBuilder();
        builder.setIsAmazon(false);
       //builder.addAllTrucks(initTrucks);
        try {
            System.out.println("world " + worldClient.getSocket());
            worldClient.writeToWorld(builder.build());
            //connected
            WorldUps.UConnected uConnected = worldClient.readFromWorldConnected();
            String result = uConnected.getResult();
            if (! result.equals("connected!"))
                throw new IllegalArgumentException(result);
            System.out.println(result);
            WORD_ID = uConnected.getWorldid();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Uconnect error : " + e);
        }

        //update database
        for (WorldUps.UInitTruck truck : initTrucks){
            truckMapper.insertTruck(new Truck(truck.getId(), ConstUtils.TRUCK_IDLE_STATUS, 0));
        }
        sqlSession.commit();

        ////////////////////
//        WorldUps.UGoDeliver.Builder builder1 = WorldUps.UGoDeliver.newBuilder();
//        builder1.setSeqnum(2).setTruckid(1);
//        WorldUps.UCommands.Builder builder2 = WorldUps.UCommands.newBuilder();
//        builder2.addDeliveries(builder1);
//        try {
//            worldClient.writeToWorld(builder2.build());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        ///////////////////


        while (true){
            try {
                WorldUps.UResponses uResponses = worldClient.readFromWorld();
                List<Long> uwACKList = uResponses.getAcksList();
                List<WorldUps.UDeliveryMade> uDeliveryMadeList = uResponses.getDeliveredList();
                List<WorldUps.UFinished> uFinishedList = uResponses.getCompletionsList();
                List<WorldUps.UTruck> uTruckList = uResponses.getTruckstatusList();
                List<WorldUps.UErr> uErrList = uResponses.getErrorList();
                if (uErrList.size() > 0){
                    for (WorldUps.UErr err: uErrList){
                        System.out.println("ups-world error is : " + err.getErr());
                    }
                    throw new IOException("err feedback");
                }

                for (long ack : uwACKList){
                    handleAcks(ack);
                }
                for (WorldUps.UDeliveryMade uDeliveryMade: uDeliveryMadeList){
                    handleUDeliveryMade(uDeliveryMade);
                }
                for (WorldUps.UFinished uFinished :uFinishedList){
                    handleUFinished(uFinished);
                }
                for (WorldUps.UTruck uTruck : uTruckList){
                    handleUTruck(uTruck);
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("world error : " + e);
            }
        }
    }
}
