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

import static org.junit.jupiter.api.Assertions.*;
import org.example.Database.*;

public class TruckControllerTest {
    private static Socket socket_amazon_world;
    private static long worldId;

    @BeforeAll
    public static void setUpUConnect() {
        String worldServerIP = "vcm-32197.vm.duke.edu";
        int worldServerPort = 12345;

        try {
            // UConnect
            socket_amazon_world = new Socket(worldServerIP, worldServerPort);
            TruckController truckController = new TruckController(socket_amazon_world);

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
        socket_amazon_world.close();
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

            Socket socket_amazon_world_new = new Socket(worldServerIP, amazonServerPort);
            OutputStream outputStream = socket_amazon_world_new.getOutputStream();
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            Message msg = builder.build();
            codedOutputStream.writeUInt32NoTag(msg.toByteArray().length);
            msg.writeTo(codedOutputStream);
            codedOutputStream.flush();

            InputStream inputStream = socket_amazon_world_new.getInputStream();
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

                truckId = newTruck.getTruckId();

            } catch (Exception e) {
                e.printStackTrace();
            }

            TruckController truckController = new TruckController(socket_amazon_world);
            TruckReceiver truckReceiver = truckController.truckReceiver;
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

            // Close the new socket_amazon_world at the end of the test
            socket_amazon_world_new.close();

        } catch (IOException e) {
            e.printStackTrace();
            fail("An exception occurred during the test.");
        }
    }


    private TruckMapper truckMapper;
    private SqlSession sqlSession;
    private User testUser;
    private Package testPackage;
    private Truck testTruck;

    @BeforeEach
    public void insertTestData() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            PackagesMapper packagesMapper = sqlSession.getMapper(PackagesMapper.class);
            TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);

            // 插入测试数据到 user 表
            User testUser = new User();
            testUser.setUserId("test_user");
            testUser.setPassword("test_password");
            userMapper.insertUser(testUser);

            // 插入测试数据到 truck 表
            Truck testTruck = new Truck();
            testTruck.setTruckId(5);
            testTruck.setStatus("idle");
            testTruck.setPackageNum(1);

            truckMapper.insertTruck(testTruck);

            // 插入测试数据到 packages 表
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

    @AfterEach
    public void deleteTestData() {
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            PackagesMapper packagesMapper = sqlSession.getMapper(PackagesMapper.class);
            TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);

            // 删除 user 表中的测试数据
            userMapper.deleteUser("test_user");

            // 删除 truck 表中的测试数据
            truckMapper.deleteTruck(5);

            // 删除 packages 表中的测试数据
            packagesMapper.deletePackage(9);

            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception occurred while deleting test data.");
        }
    }
}

