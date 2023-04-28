package org.example.Database;

public class Packages {
    private Integer packageId;
    private Integer truckId;
    private String userId;
    private Integer itemNum;
    private  String status;
    private  int destx;
    private int desty;
    String description;

//    public Packages(Integer packageId, Integer truckId, String userId, Integer itemNum) {
//        this.packageId = packageId;
//        this.truckId = truckId;
//        this.userId = userId;
//        this.itemNum = itemNum;
//    }


    public Packages(Integer packageId, Integer truckId, String userId, Integer itemNum, String status, int destx, int desty, String description) {
        this.packageId = packageId;
        this.truckId = truckId;
        this.userId = userId;
        this.itemNum = itemNum;
        this.status = status;
        this.destx = destx;
        this.desty = desty;
        this.description = description;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItemNum(Integer itemNum) {
        this.itemNum = itemNum;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public Integer getTruckId() {
        return truckId;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getPackageNum() {
        return itemNum;
    }

    public String getStatus() {
        return status;
    }

    public int getDestx() {
        return destx;
    }

    public int getDesty() {
        return desty;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDestx(int destx) {
        this.destx = destx;
    }

    public void setDesty(int desty) {
        this.desty = desty;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getters and setters
}
