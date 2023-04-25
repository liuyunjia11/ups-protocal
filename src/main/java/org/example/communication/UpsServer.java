package org.example.communication;

import org.apache.ibatis.annotations.Mapper;
import org.example.Database.PackagesMapper;
import org.example.Database.TruckMapper;
import org.example.Database.UserMapper;
import org.example.Handler.ACommandHandler;
import org.example.Handler.SenderHandler;
import org.example.Handler.WCommandHandler;
import org.example.Utils.MyBatisUtil;
import org.example.controller.TruckController;
import org.example.protocol.UpsAmazon;
import org.example.protocol.WorldUps;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.example.Utils.MyBatisUtil.getSqlSession;

public class UpsServer {
    public static int WORD_ID;

    public static UserClient userClient;
    public static AmazonClient amazonClient;
    public static WorldClient worldClient;

    public static TruckMapper truckMapper;
    public static UserMapper userMapper;
    public static PackagesMapper packagesMapper;

    //uw
    public static Map<Long, WorldUps.UGoPickup> uGoPickupMap =  new ConcurrentHashMap<>();
    public static  Map<Long, WorldUps.UGoDeliver> uGoDeliverMap = new ConcurrentHashMap<>();
    public static Set<Long> uwACKSet = new CopyOnWriteArraySet<>();
    //ua
    public static Map<Long, UpsAmazon.UATruckArrived> uaTruckArrivedMap = new ConcurrentHashMap<>();
    public static Set<Long> uaACKSet = new CopyOnWriteArraySet<>();
    public static  Map<Long, UpsAmazon.UAUpdatePackageStatus> uaUpdatePackageStatusMap =  new ConcurrentHashMap<>();

    public UpsServer(String toWorldHost, int toWorldPortNum, int myPortNum) throws IOException {
        //database init
        initUpsDatabase();
        System.out.println("database initialed");
        //communication init
        userClient = new UserClient();
        amazonClient = new AmazonClient(myPortNum);
        worldClient = new WorldClient(toWorldHost, toWorldPortNum);
        System.out.println("connection built");
        //get mapper
        packagesMapper = MyBatisUtil.getSqlSession().getMapper(PackagesMapper.class);
        truckMapper = MyBatisUtil.getSqlSession().getMapper(TruckMapper.class);
        userMapper = MyBatisUtil.getSqlSession().getMapper(UserMapper.class);
    }

    private static void initUpsDatabase(){
        try {
            MyBatisUtil.dropAllTables();
            MyBatisUtil.createAllTables();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        WCommandHandler wCommandHandler = new WCommandHandler();
        Thread thread0 = new Thread(wCommandHandler);
        thread0.start();
        ACommandHandler aCommandHandler = new ACommandHandler();
        Thread thread1 = new Thread(aCommandHandler);
        thread1.start();
        //todo: user
        SenderHandler senderHandler = new SenderHandler();
        Thread thread3 = new Thread(senderHandler);
        thread3.start();
    }




}
