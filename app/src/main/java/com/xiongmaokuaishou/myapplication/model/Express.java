package com.xiongmaokuaishou.myapplication.model;

public class Express {
    private String takeCode;
    private String outState;//出库状态  false->待出库  true->已出库
    private String phoneNum;
    private String name;
    private String orderID;
    private String arriveTime;
    private String parcelId;

    public Express(String takeCode, String outState, String phoneNum, String name, String orderID, boolean uploadState, String time,String arriveTime,String parcelId) {
        this.takeCode = takeCode;
        this.outState = outState;
        this.phoneNum = phoneNum;
        this.name = name;
        this.orderID = orderID;
        this.uploadState = uploadState;
        this.time = time;
        this.arriveTime=arriveTime;
        this.parcelId=parcelId;
    }

    private boolean uploadState;//上传状态  false->上传失败  true->上传成功
    private String time;

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public String getTakeCode() {
        return takeCode;
    }

    public void setTakeCode(String takeCode) {
        this.takeCode = takeCode;
    }

    public String getOutState() {
        return outState;
    }

    public void setOutState(String outState) {
        this.outState = outState;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public boolean isUploadState() {
        return uploadState;
    }

    public void setUploadState(boolean uploadState) {
        this.uploadState = uploadState;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
