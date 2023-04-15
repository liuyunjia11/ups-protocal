package org.example.Database;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PackagesMapper {

    @Select("SELECT * FROM package WHERE packageId = #{packageId}")
    Packages getPackageById(Integer packageId);

    @Select("SELECT * FROM package")
    List<Packages> getAllPackages();

    @Insert("INSERT INTO package (truckId, userId, itemNum) VALUES (#{truckId}, #{userId}, #{itemNum})")
    @Options(useGeneratedKeys = true, keyProperty = "packageId")
    void insertPackage(Packages pkg);

    @Update("UPDATE package SET truckId = #{truckId}, userId = #{userId}, itemNum = #{itemNum} WHERE packageId = #{packageId}")
    void updatePackage(Packages pkg);

    @Delete("DELETE FROM package WHERE packageId = #{packageId}")
    void deletePackage(Integer packageId);
}
