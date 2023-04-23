package org.example.controller;

import org.apache.ibatis.session.SqlSession;
import org.example.Database.User;
import org.example.Database.UserMapper;
import org.example.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "UserController", urlPatterns = {"/register"})
public class UserController extends HttpServlet {
    private UserMapper userMapper;

    public UserController() {
        try {
            SqlSession sqlSession = MyBatisUtil.getSqlSession();
            userMapper = sqlSession.getMapper(UserMapper.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        userMapper.insertUser(new User(username, password));
        // Redirect to the login page after successful registration
        response.sendRedirect("/login");
    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userMapper.getUserById(username);

        if (user.getPassword().equals(password)) {
            // User authenticated, set session attribute and redirect to the protected page
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
        } else {
            // User not authenticated, redirect to the login page with an error message
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
      doRegister(request, response);
    }



}
