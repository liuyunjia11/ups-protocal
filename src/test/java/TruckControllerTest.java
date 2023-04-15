import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.apache.ibatis.session.SqlSession;
import org.example.Database.Packages;
import org.example.Database.Truck;
import org.example.Database.TruckMapper;
import org.example.MyBatisUtil;
import org.example.Receiver.TruckReceiver;
import org.example.controller.TruckController;
import org.example.protocol.WorldAmazon;
import org.example.protocol.WorldUps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

public class TruckControllerTest {


//    private SqlSession sqlSession;
//    private TruckMapper truckMapper;
//
//    @BeforeEach
//    public void setUp() {
//        sqlSession = MyBatisUtil.getSqlSession();
//        truckMapper = sqlSession.getMapper(TruckMapper.class);
//
//        // 插入测试数据
//        Truck testTruck = new Truck();
//        testTruck.setTruckId(1);
//        testTruck.setStatus("available");
//        testTruck.setPackageNum(0);
//        truckMapper.insertTruck(testTruck);
//
//        Packages testPackage = new Packages();
//        testPackage.setPackageId(1);
//        testPackage.setTruckId(1);
//        testPackage.setUserId(String.valueOf(1));
//        testPackage.setItemNum(2);
//        truckMapper.insertPackage(testPackage);
//
//        sqlSession.commit();
//    }
//
//    @AfterEach
//    public void tearDown() {
//        // 清除测试数据
//        truckMapper.deleteTruckById(1);
//        truckMapper.deletePackageById(1);
//
//        sqlSession.commit();
//        sqlSession.close();
//    }
//


    @Test
    public void testAmazonInit() {

    }

    @Test
    public void testSendAndReceiveMessages() {
        String worldServerIP = "vcm-32197.vm.duke.edu";
        int worldServerPort = 12345;
        int amazonServerPort = 23456;

        try {
            Socket socket = new Socket(worldServerIP, worldServerPort);
            TruckController truckController = new TruckController(socket);


            TruckReceiver truckReceiver = truckController.truckReceiver;

            // Send UConnect message
            truckController.sendUConnect(null, 5);

            // Receive UConnected message
            truckController.getUConnected();
            String result = truckController.getUConnectedResult();

            // Check if UConnected message contains expected values
            assertEquals("connected!", result);
            System.out.println("UConnected received successfully");



            // Amazon init whId ================
            WorldAmazon.AConnect.Builder builder = WorldAmazon.AConnect.newBuilder();
            builder.setWorldid(1);
            builder.setIsAmazon(true);
            WorldAmazon.AInitWarehouse.Builder aInitWarehouse = WorldAmazon.AInitWarehouse.newBuilder();
            aInitWarehouse.setX(1).setY(1).setId(1);
            aInitWarehouse.build();
            builder.addInitwh(aInitWarehouse);

            Socket socket_amazon_world = new Socket(worldServerIP, amazonServerPort);
            OutputStream outputStream = socket_amazon_world.getOutputStream();
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            Message msg = builder.build();
            codedOutputStream.writeUInt32NoTag(msg.toByteArray().length);
            msg.writeTo(codedOutputStream);
            codedOutputStream.flush();


            InputStream inputStream = socket_amazon_world.getInputStream();
            CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
            WorldAmazon.AConnected aConnected = WorldAmazon.AConnected.parseFrom(codedInputStream.readByteArray());

            assertEquals("connected!", aConnected.toBuilder().getResult());
            System.out.println("AConnected successfully");
            //===================================








            // Send pickup command
            int truckId = 0;
            int whId = 1;
            long seqNum = 1;

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                // 获取 TruckMapper 接口的实例
                TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);

                // 执行数据库操作，例如插入一条新的 truck 记录
                Truck newTruck = truckMapper.getMinPackageTruck();

                truckId = newTruck.getTruckId();

            } catch (Exception e) {
                e.printStackTrace();
            }


            truckController.truckSender.sendUCommandUGoPickUp(truckId, whId, seqNum);

            System.out.println("Pickup UCommands sent");

            WorldUps.UResponses.Builder uResponsesBuilder = truckReceiver.getUResponse();
            System.out.println("UResponses received");
            assertEquals(1, uResponsesBuilder.getCompletionsCount());
            WorldUps.UFinished completion = uResponsesBuilder.getCompletions(0);
            System.out.println("Pickup completion received:" + completion);


            // Send deliver command
            int truckId_deliver = 1;
            long packageId = 1;
            int destinationX = 10;
            int destinationY = 20;
            long seqNum_deliver = 2;

            WorldUps.UDeliveryLocation deliveryLocation = WorldUps.UDeliveryLocation.newBuilder()
                    .setPackageid(packageId)
                    .setX(destinationX)
                    .setY(destinationY)
                    .build();

            truckController.truckSender.sendUCommandUGoDeliver(truckId_deliver, deliveryLocation, seqNum_deliver);

            System.out.println("Deliver UCommands sent");

            WorldUps.UResponses.Builder uResponsesBuilder_deliver = truckReceiver.getUResponse();
            System.out.println("UResponses received");
            assertEquals(1, uResponsesBuilder_deliver.getCompletionsCount());
            WorldUps.UFinished deliveryMade = uResponsesBuilder_deliver.getCompletions(0);
            System.out.println("Delivery completion received:" + deliveryMade);


        } catch (IOException e) {
            e.printStackTrace();
            fail("An exception occurred during the test.");
        }
    }
}
