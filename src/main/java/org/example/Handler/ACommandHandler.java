package org.example.Handler;


import org.apache.ibatis.session.SqlSession;
import org.example.Database.*;
import org.example.Utils.ConstUtils;
import org.example.Utils.MyBatisUtil;
import org.example.Utils.SeqUtils;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.example.communication.UpsServer.*;

public class ACommandHandler implements  Runnable{
    SeqUtils seqUtils = SeqUtils.getSeqUtils();
    void handleAUCallTruck(UpsAmazon.AUCallTruck auCallTruck){
        seqUtils.accumulate();

        Truck truck = truckMapper.getMinPackageTruck();
        System.out.println("get truck : " + truck.getTruckId());
        WorldUps.UGoPickup.Builder pickUpBuilder = WorldUps.UGoPickup.newBuilder().setTruckid(truck.getTruckId())
                .setWhid(auCallTruck.getWhid()).setSeqnum(seqUtils.getValue());

        uGoPickupMap.put(seqUtils.getValue(), pickUpBuilder.build());
        uaACKSet.add(auCallTruck.getSeqnum());
        System.out.println("store " + seqUtils.getValue() + " into map");

        //database
        List<UpsAmazon.AUProduct> aProductList = auCallTruck.getThingsList();
        for (UpsAmazon.AUProduct aProduct : aProductList){
            packagesMapper.insertPackage(new Packages((int) aProduct.getId(), truck.getTruckId(),
                    Integer.toString(aProduct.getUserid()), 0, ConstUtils.PACKAGR_WAITING,
                    aProduct.getDestX(), aProduct.getDestY(), aProduct.getDescription()));
        }
        truck.setPackageNum(truck.getPackageNum() + aProductList.size());
        truckMapper.updateTruck(truck);
        sqlSession.commit();

    }

    void handleAUAcks(long ack){
        System.out.println("sucessfully get ack : " + ack);

        uaTruckArrivedMap.remove(ack);
        uaTruckDeliverMadeMap.remove(ack);
        uaUpdatePackageStatusMap.remove(ack);
    }

    void handleAUTruckGoDeliver(UpsAmazon.AUTruckGoDeliver auTruckGoDeliver){
        seqUtils.accumulate();

        List<WorldUps.UDeliveryLocation> uDeliveryLocationList = new ArrayList<>();
        List<Packages> packagesList = packagesMapper.getPackagesByTruckId(auTruckGoDeliver.getTruckid());
        for (Packages packages : packagesList){
            WorldUps.UDeliveryLocation.Builder builder = WorldUps.UDeliveryLocation.newBuilder();
            builder.setPackageid(packages.getPackageId()).setX(packages.getDestx()).setY(packages.getDesty());
            uDeliveryLocationList.add(builder.build());
        }
        WorldUps.UGoDeliver.Builder uGoDeliverBuilder = WorldUps.UGoDeliver.newBuilder();
        uGoDeliverBuilder.setTruckid(auTruckGoDeliver.getTruckid()).setSeqnum(seqUtils.getValue()).
                addAllPackages(uDeliveryLocationList);

        uGoDeliverMap.put(seqUtils.getValue(), uGoDeliverBuilder.build());
        uaACKSet.add(auTruckGoDeliver.getSeqnum());
    }

    void handleUserInfo(UpsAmazon.AURequestSendUserInfo auRequestSendUserInfo){
        uaACKSet.add(auRequestSendUserInfo.getSeqnum());
        userMapper.insertUser(new User(Integer.toString(auRequestSendUserInfo.getUserid()), auRequestSendUserInfo.getPassword()));
        sqlSession.commit();
    }

    void handleRequestPackageStatus(UpsAmazon.AURequestPackageStatus auRequestPackageStatus){
        seqUtils.accumulate();

        WorldUps.UQuery.Builder builder = WorldUps.UQuery.newBuilder();
        int truckId = packagesMapper.getPackageById((int) auRequestPackageStatus.getShipid()).getTruckId();
        Set<Long> set = packageStatusMap.getOrDefault(truckId, new HashSet<>());
        set.add(auRequestPackageStatus.getShipid());
        packageStatusMap.put((long) truckId, set);
        builder.setSeqnum(seqUtils.getValue()).setTruckid(truckId);

        uQueryMap.put(seqUtils.getValue(), builder.build());
        uaACKSet.add(auRequestPackageStatus.getSeqnum());
    }

    void handleGoLoad(UpsAmazon.AUTruckGoLoad auTruckGoLoad){
        uaACKSet.add(auTruckGoLoad.getSeqnum());
        truckMapper.updateTruckStatus(ConstUtils.TRUCK_LOAD_STATUS, auTruckGoLoad.getTruckid());
        sqlSession.commit();
    }
    @Override
    public void run(){
        //send worldid to amz
        UpsAmazon.UAConnect.Builder builder = UpsAmazon.UAConnect.newBuilder();
        builder.setWorldid(WORD_ID);
        try {
            amazonClient.writeToAmz(builder.build());
            UpsAmazon.AUConnected auConnected  = amazonClient.readConnectFromAmz();
            String result = auConnected.getResult();
            if (! result.equals("connected!") || auConnected.getWorldid() != WORD_ID)
                throw new IllegalArgumentException(result);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("worldID sending error :" + e);
        }

        while (true){
            try {
                UpsAmazon.AUCommands auCommands = amazonClient.readFromAmz();
                //AUCallTruck
                List<UpsAmazon.Err> errList = auCommands.getErrList();
                if (errList.size() > 0){
                    for (UpsAmazon.Err err: errList){
                        System.out.println("ups-amz error is : " + err.getErr());
                    }
                    throw new IOException("err feedback");
                }
                List<UpsAmazon.AUTruckGoLoad> auTruckGoLoadList = auCommands.getLoadingList();
                List<UpsAmazon.AURequestSendUserInfo> auRequestSendUserInfoList = auCommands.getUserInfoList();
                List<UpsAmazon.AUCallTruck> auCallTruckList = auCommands.getCallTruckList();
                List<Long> ackList = auCommands.getAcksList();
                List<UpsAmazon.AUTruckGoDeliver> auTruckGoDeliverList = auCommands.getTruckGoDeliverList();
                List<UpsAmazon.AURequestPackageStatus> auRequestPackageStatusList =  auCommands.getRequestPackageStatusList();

                for (UpsAmazon.AUCallTruck auCallTruck : auCallTruckList){
                    handleAUCallTruck(auCallTruck);
                }
                for (long ack : ackList){
                    handleAUAcks(ack);
                }
                for (UpsAmazon.AUTruckGoDeliver auTruckGoDeliver : auTruckGoDeliverList){
                    handleAUTruckGoDeliver(auTruckGoDeliver);
                }
                for (UpsAmazon.AURequestPackageStatus auRequestPackageStatus: auRequestPackageStatusList){
                    handleRequestPackageStatus(auRequestPackageStatus);
                }
                for (UpsAmazon.AUTruckGoLoad auTruckGoLoad : auTruckGoLoadList){
                    handleGoLoad(auTruckGoLoad);
                }
                for (UpsAmazon.AURequestSendUserInfo auRequestSendUserInfo : auRequestSendUserInfoList){
                    handleUserInfo(auRequestSendUserInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("amazon error: " + e);
            }
        }
    }

}
