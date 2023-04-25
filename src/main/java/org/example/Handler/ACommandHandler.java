package org.example.Handler;

import org.example.Database.Truck;
import org.example.Utils.SeqUtils;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.communication.UpsServer.*;

public class ACommandHandler implements  Runnable{

    SeqUtils seqUtils = SeqUtils.getSeqUtils();
    void handleAUCallTruck(UpsAmazon.AUCallTruck auCallTruck){
        Truck truck = truckMapper.getMinPackageTruck();
        System.out.println("get truck : " + truck.getTruckId());
        WorldUps.UGoPickup.Builder pickUpBuilder = WorldUps.UGoPickup.newBuilder().setTruckid(truck.getTruckId()).setWhid(auCallTruck.getWhid());
        seqUtils.accumulate();
        pickUpBuilder.setSeqnum(seqUtils.getValue());

        uGoPickupMap.put(seqUtils.getValue(), pickUpBuilder.build());
        uaACKSet.add(auCallTruck.getSeqnum());
        System.out.println("store " + seqUtils.getValue() + " into map");

        //database
        List<UpsAmazon.AProduct> aProductList = auCallTruck.getThingsList();
        for (UpsAmazon.AProduct aProduct : aProductList){
            packagesMapper.updatePackageTruckId(Integer.valueOf((int) aProduct.getId()), truck.getTruckId());
            truckMapper.updateTruckStatus("", truck.getTruckId());
        }

    }

    void handleAUAcks(long ack){
        System.out.println("sucessfully get ack : " + ack);
        uaTruckArrivedMap.remove(ack);
        uaUpdatePackageStatusMap.remove(ack);
        //todo
    }

    void handleAUTruckGoDeliver(UpsAmazon.AUTruckGoDeliver auTruckGoDeliver){
        seqUtils.accumulate();
        List<UpsAmazon.AUDeliveryLocation> auDeliveryLocationList = auTruckGoDeliver.getPackagesList();
        List<WorldUps.UDeliveryLocation> uDeliveryLocationList = new ArrayList<>();
        for (UpsAmazon.AUDeliveryLocation auDeliveryLocation : auDeliveryLocationList){
            uDeliveryLocationList.add(WorldUps.UDeliveryLocation.newBuilder().setPackageid(auDeliveryLocation.getShipid()).
                    setX(auDeliveryLocation.getX()).
                    setY(auDeliveryLocation.getY()).
                    build());
        }
        WorldUps.UGoDeliver.Builder uGoDeliverBuilder = WorldUps.UGoDeliver.newBuilder();
        uGoDeliverBuilder.addAllPackages(uDeliveryLocationList).
                setTruckid(auTruckGoDeliver.getTruckid()).
                setSeqnum(seqUtils.getValue());

        uGoDeliverMap.put(seqUtils.getValue(), uGoDeliverBuilder.build());
        uaACKSet.add(auTruckGoDeliver.getSeqnum());

        //datbse todo
        truckMapper.updateTruckStatus("", auTruckGoDeliver.getTruckid());
    }
    @Override
    public void run(){
        //send worldid to amz
        UpsAmazon.UAConnect.Builder builder = UpsAmazon.UAConnect.newBuilder();
        builder.setWorldid(WORD_ID);
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
                List<Long> ackList = auCommands.getAcksList();
                List<UpsAmazon.AUTruckGoDeliver> auTruckGoDeliverList = auCommands.getTruckGoDeliverList();
                List<UpsAmazon.AURequestPackageStatus> auRequestPackageStatusList =  auCommands.getRequestPackageStatusList();



                //autruck
                for (UpsAmazon.AUCallTruck auCallTruck : auCallTruckList){
                    handleAUCallTruck(auCallTruck);
                }
                //ack
                for (long ack : ackList){
                    handleAUAcks(ack);
                }
                //augodelivery
                for (UpsAmazon.AUTruckGoDeliver auTruckGoDeliver : auTruckGoDeliverList){
                    handleAUTruckGoDeliver(auTruckGoDeliver);
                }
                //todo
                for (UpsAmazon.AURequestPackageStatus auRequestPackageStatus: auRequestPackageStatusList){

                }
                //todo: more receiver
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("amazon error: " + e);
            }
        }
    }

}
