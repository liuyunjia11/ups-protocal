package org.example.Database;

public class Truck {
    public Truck() {
    }

    public Truck(Integer truckId, String status) {
        this.truckId = truckId;
        this.status = status;
    }

    public Truck(Integer truckId, String status, Integer packageNum) {
        this.truckId = truckId;
        this.status = status;
        this.packageNum = packageNum;
    }

    public Integer getTruckId() {
        return truckId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPackageNum() {
        return packageNum;
    }

    public void setPackageNum(Integer packageNum) {
        this.packageNum = packageNum;
    }

    private Integer truckId;
    private String status;
    private Integer packageNum;
    // Getters and setters

}

