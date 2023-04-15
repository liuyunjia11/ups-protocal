package org.example.Database;

public class Packages {
    private Integer packageId;
    private Integer truckId;
    private String userId;
    private Integer itemNum;

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
// Getters and setters
}
