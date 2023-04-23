package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.Database.PackagesMapper;
import org.example.Database.TruckMapper;
import org.example.Database.UserMapper;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {
    private static final SqlSessionFactory sqlSessionFactory;

    static {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    public static void dropAllTables(){
        getSqlSession().getMapper(PackagesMapper.class).dropPackagesTable();
        getSqlSession().getMapper(UserMapper.class).dropUserTable();
        getSqlSession().getMapper(TruckMapper.class).dropTruckTable();
        getSqlSession().commit();
    }

    public static  void createAllTables(){
        getSqlSession().getMapper(UserMapper.class).createUserTable();
        getSqlSession().getMapper(TruckMapper.class).createTruckTable();
        getSqlSession().getMapper(PackagesMapper.class).createPackagesTable();
        getSqlSession().commit();
    }

}
