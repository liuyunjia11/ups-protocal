import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.apache.ibatis.session.SqlSession;
import org.example.Database.Truck;
import org.example.Database.TruckMapper;
import org.example.MyBatisUtil;
import org.example.Receiver.TruckReceiver;
import org.example.controller.TruckController;
import org.example.protocol.WorldAmazon;
import org.example.protocol.WorldUps;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.example.Database.*;

public class TruckControllerTest {
    private static Socket socket_ups_world;
    private static Socket socket_amazon_world;
    private static long worldId;

    @BeforeAll
    public static void setUpUConnect() {
        String worldServerIP = "vcm-32197.vm.duke.edu";
        int worldServerPort = 12345;

        try {
            // UConnect
            socket_ups_world = new Socket(worldServerIP, worldServerPort);
            TruckController truckController = new TruckController(socket_ups_world);

            // Send UConnect message
            truckController.sendUConnect(null, 5);

            // Receive UConnected message
            truckController.getUConnected();
            String result = truckController.getUConnectedResult();

            // Check if UConnected message contains expected values
            assertEquals("connected!", result);
            System.out.println("UConnected received successfully");

            // Get worldId from UConnected message
            worldId = truckController.getUConnectedWorldId();

        } catch (IOException e) {
            e.printStackTrace();
            fail("An exception occurred during the test.");
        }
    }

    @AfterAll
    public static void tearDown() throws IOException {
        socket_ups_world.close();
    }

    @Test
    public void testUpsInit() {
        String worldServerIP = "vcm-32197.vm.duke.edu";
        int amazonServerPort = 23456;

        try {
            // AConnect
            WorldAmazon.AConnect.Builder builder = WorldAmazon.AConnect.newBuilder();
            builder.setWorldid(worldId);
            builder.setIsAmazon(true);
            WorldAmazon.AInitWarehouse.Builder aInitWarehouse = WorldAmazon.AInitWarehouse.newBuilder();
            aInitWarehouse.setX(1).setY(1).setId(1);
            aInitWarehouse.build();
            builder.addInitwh(aInitWarehouse);

            socket_amazon_world = new Socket(worldServerIP, amazonServerPort);
            OutputStream outputStream = socket_amazon_world.getOutputStream();
            CodedOutputStream codedOutputStream1 = CodedOutputStream.newInstance(outputStream);
            Message msg = builder.build();
            codedOutputStream1.writeUInt32NoTag(msg.toByteArray().length);
            msg.writeTo(codedOutputStream1);
            codedOutputStream1.flush();

            InputStream inputStream = socket_amazon_world.getInputStream();
            CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
            WorldAmazon.AConnected aConnected = WorldAmazon.AConnected.parseFrom(codedInputStream.readByteArray());

            assertEquals("connected!", aConnected.toBuilder().getResult());
            System.out.println("AConnected successfully");


            // Send pickup command
            int truckId = 0;
            int whId = 1;
            long seqNum = 1;

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                // 获取 TruckMapper 接口的实例
                TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);

                // 执行数据库操作，例如插入一条新的 truck 记录
                Truck newTruck = truckMapper.getMinPackageTruck();

                truckId = newTruck.getTruckId();  // truckId = 5

            } catch (Exception e) {
                e.printStackTrace();
            }

            TruckController truckController = new TruckController(socket_ups_world);
            TruckReceiver truckReceiver = truckController.truckReceiver;
            truckController.truckSender.sendUCommandUGoPickUp(truckId, whId, seqNum);

            System.out.println("Pickup UCommands sent");

            WorldUps.UResponses.Builder uResponsesBuilder = truckReceiver.getUResponse();
            System.out.println("UResponses received");
            assertEquals(1, uResponsesBuilder.getCompletionsCount());
            WorldUps.UFinished completion = uResponsesBuilder.getCompletions(0);
            System.out.println("Pickup completion received:" + completion);

            //===========================g
            System.out.println("Sending APutOnTruck message");


            // 创建一个 APutOnTruck 消息
            WorldAmazon.APutOnTruck.Builder aPutOnTruckBuilder = WorldAmazon.APutOnTruck.newBuilder();
            aPutOnTruckBuilder.setWhnum(whId);
            aPutOnTruckBuilder.setTruckid(truckId); // 5
            aPutOnTruckBuilder.setShipid(9);
            aPutOnTruckBuilder.setSeqnum(seqNum + 1); // 增加一个新的 seqNum 用于此消息
            aPutOnTruckBuilder.build();

            // 创建一个 ACommands 消息
            WorldAmazon.ACommands.Builder aCommandsBuilder = WorldAmazon.ACommands.newBuilder();
            aCommandsBuilder.addLoad(aPutOnTruckBuilder);
//            aCommandsBuilder.addAcks(seqNum); //
            Message aCommandsMsg = aCommandsBuilder.build();
            CodedOutputStream codedOutputStream2 = CodedOutputStream.newInstance(outputStream);
            codedOutputStream2.writeUInt32NoTag(aCommandsMsg.toByteArray().length);
            aCommandsMsg.writeTo(codedOutputStream2);
            codedOutputStream2.flush();

            System.out.println("APutOnTruck message sent. Waiting for AResponses...");

            // 等待 AResponses 确认
            WorldAmazon.AResponses aResponses = WorldAmazon.AResponses.parseFrom(codedInputStream.readByteArray());
            System.out.println(aResponses.toBuilder().toString());
            System.out.println("APutOnTruck AResponses received");
            //===========================g

            // Send deliver command
            WorldUps.UDeliveryLocation deliveryLocation = WorldUps.UDeliveryLocation.newBuilder()
                    .setPackageid(9)
                    .setX(10)
                    .setY(10)
                    .build();

            truckController.truckSender.sendUCommandUGoDeliver(truckId, deliveryLocation, seqNum + 1);

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


    @BeforeEach
    public void deleteThenInsertTestData() {

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            PackagesMapper packagesMapper = sqlSession.getMapper(PackagesMapper.class);
            TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);

            // 删除表中的测试数据
            userMapper.deleteUser("test_user");
            truckMapper.deleteTruck(5);
            packagesMapper.deletePackage(9);
            sqlSession.commit();


            // 插入测试数据到表
            User testUser = new User();
            testUser.setUserId("test_user");
            testUser.setPassword("test_password");
            userMapper.insertUser(testUser);

            Truck testTruck = new Truck();
            testTruck.setTruckId(5);
            testTruck.setStatus("IDLE");
            testTruck.setPackageNum(1);
            truckMapper.insertTruck(testTruck);

            Packages testPackage = new Packages();
            testPackage.setPackageId(9);
            testPackage.setTruckId(5);
            testPackage.setUserId("test_user");
            testPackage.setItemNum(1);
            packagesMapper.insertPackage(testPackage);

            sqlSession.commit();

        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred while inserting test data.");
        }
    }
}

