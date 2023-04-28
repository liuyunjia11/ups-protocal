package org.example.Database;

import org.example.Database.Truck;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TruckMapper {

    void dropTruckTable();
    void createTruckTable();

    void updateTruckStatus(@Param("status") String status, @Param("truckId") Integer truckId);

    //@Select("SELECT * FROM truck WHERE truckId = #{truckId}")
    Truck getTruckById(Integer truckId);

    //@Select("SELECT * FROM truck")
    List<Truck> getAllTrucks();

    //@Insert("INSERT INTO truck (truckId, status, packageNum) VALUES (#{truckId}, #{status}, #{packageNum})")
    void insertTruck(Truck truck);

    //@Update("UPDATE truck SET status = #{status}, packageNum = #{packageNum} WHERE truckId = #{truckId}")
    void updateTruck(Truck truck);

    //@Delete("DELETE FROM truck WHERE truckId = #{truckId}")
    void deleteTruck(Integer truckId);

    //@Select("SELECT * FROM truck WHERE status IN ('idle', 'delivering') ORDER BY packageNum LIMIT 1;")
    Truck getMinPackageTruck();



    void insertPackage(Packages pkg);

    void deleteTruckById(int truckId);

    void deletePackageById(int packageId);
}

