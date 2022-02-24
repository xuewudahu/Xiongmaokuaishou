package com.xiongmaokuaishou.myapplication.model;

public class Take {
    private String takeCode;

    private String orderID;
    public Take(String takeCode, String orderID) {
        this.takeCode = takeCode;
        this.orderID = orderID;
    }

    public String getTakeCode() {
        return takeCode;
    }
    public void setTakeCode(String takeCode) {
        this.takeCode = takeCode;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

}
