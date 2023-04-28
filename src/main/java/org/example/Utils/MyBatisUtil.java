package org.example.Utils;

import org.apache.ibatis.annotations.Mapper;
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
        SqlSession sqlSession = MyBatisUtil.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        PackagesMapper packagesMapper = sqlSession.getMapper(PackagesMapper.class);
        TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);
        packagesMapper.dropPackagesTable();
        userMapper.dropUserTable();
        truckMapper.dropTruckTable();
        sqlSession.commit();
    }

    public static  void createAllTables(){
        SqlSession sqlSession = MyBatisUtil.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        PackagesMapper packagesMapper = sqlSession.getMapper(PackagesMapper.class);
        TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);
        userMapper.createUserTable();
        truckMapper.createTruckTable();
        packagesMapper.createPackagesTable();
        sqlSession.commit();
    }

}
