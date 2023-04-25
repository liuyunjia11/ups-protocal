package org.example.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.protobuf.InvalidProtocolBufferException;
import org.example.Utils.MyBatisUtil;
import org.example.protocol.UserUps.*;

import org.example.Database.User;
import org.example.Database.UserMapper;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;

@ServerEndpoint(value = "/user")
public class UserController {

    @OnMessage
    public void onMessage(Session session, ByteBuffer message) throws InvalidProtocolBufferException {
        JSONObject jsonMessage = new JSONObject(new String(message.array()));
        String action = jsonMessage.getString("action");
        byte[] payload = Base64.getDecoder().decode(jsonMessage.getString("payload"));

        switch (action) {
            case "register":
                handleRegister(session, payload);
                break;
            case "login":
                handleLogin(session, payload);
                break;
            default:
                System.err.println("Unsupported action: " + action);
        }
    }

    private void handleRegister(Session session, byte[] payload) throws InvalidProtocolBufferException {
        // Deserialize the received message
        Register_UserController registerRequest = Register_UserController.parseFrom(payload);

        // Extract the username and password
        String username = registerRequest.getUpsUserid();
        String password = registerRequest.getUpsPassword();

        //Validate the username and password and store them in the database
        // Create a response
        UserController_Register.Builder registerResponseBuilder = UserController_Register.newBuilder();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

            // Check if the user already exists
            User existingUser = userMapper.getUserById(username);

            if (existingUser == null) {
                // If the user does not exist, register the user and set the ack field to success
                User newUser = new User(username, password);
                userMapper.insertUser(newUser);
                sqlSession.commit();
                registerResponseBuilder.setAcks("success");
            } else {
                // If the user already exists, set the ack field to failure
                registerResponseBuilder.setAcks("failure");
            }
        }

        // Serialize the response
        payload = registerResponseBuilder.build().toByteArray();

        // Wrap the response in a new object with action and payload properties
        JSONObject responseObj = new JSONObject();
        responseObj.put("action", "registerResponse");
        responseObj.put("payload", Base64.getEncoder().encodeToString(payload));


        // Send the response to the client
        try {
            session.getBasicRemote().sendText(responseObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(Session session, byte[] payload) throws InvalidProtocolBufferException {
        // Deserialize the received message
        Login_UserController loginRequest = Login_UserController.parseFrom(payload);

        // Extract the username and password
        String username = loginRequest.getUpsUserid();
        String password = loginRequest.getUpsPassword();

        // Create a response
        UserController_Login.Builder loginResponseBuilder = UserController_Login.newBuilder();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

            // Check if the user exists and the password is correct
            User user = userMapper.getUserById(username);

            if (user != null && user.getPassword().equals(password)) {
                loginResponseBuilder.setAcks("success");
            } else {
                loginResponseBuilder.setAcks("failure");
            }
        }

        // Serialize the response
        payload = loginResponseBuilder.build().toByteArray();

        // Wrap the response in a new object with action and payload properties
        JSONObject responseObj = new JSONObject();
        responseObj.put("action", "loginResponse");
        responseObj.put("payload", Base64.getEncoder().encodeToString(payload));

        // Send the response to the client
        try {
            session.getBasicRemote().sendText(responseObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
