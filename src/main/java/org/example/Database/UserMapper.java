package org.example.Database;

import org.example.Database.User;
import org.apache.ibatis.annotations.*;

public interface UserMapper {

    //@Select("SELECT * FROM user WHERE userId = #{userId}")
    User getUserById(Integer userId);

    //@Insert("INSERT INTO user (userId, password) VALUES (#{userId}, #{password})")
    void insertUser(User user);

    //@Update("UPDATE user SET password = #{password} WHERE userId = #{userId}")
    void updateUser(User user);

    //@Delete("DELETE FROM user WHERE userId = #{userId}")
    void deleteUser(Integer userId);
}

