package com.rq.camera;

import java.util.Arrays;

/**
 * 条码桢对象封装
 */
public class Frame {
    private String barcode;
    private MImage frameImage;
    private long frameTime;
    private byte[] data;

    public Frame() {
    }

    public Frame(long frameTime, byte[] data) {
        this.frameTime = frameTime;
        this.data = data;
    }

    public Frame(MImage frameImage, long frameTime, byte[] data) {
        this.frameImage = frameImage;
        this.frameTime = frameTime;
        this.data = data;
    }

    public MImage getFrameImage(){
        return frameImage;
    }
    public void setFrameImage(MImage frameImage){
        this.frameImage = frameImage;
    }
    public long getFrameTime() {
        return frameTime;
    }

    public void setFrameTime(long frameTime) {
        this.frameTime = frameTime;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void destory(){
        if(frameImage != null) {
            frameImage.destory();
            frameImage = null;
        }

        data = null;
    }

    public boolean isEmpty(){
        return data == null && frameImage == null;
    }

    public Frame clone(){
        Frame frame = new Frame();
        if(frameImage != null)
            frame.frameImage = frameImage.clone();
        if(this.data != null && data.length > 0)
            frame.data = Arrays.copyOf(data,data.length);
        frame.frameTime = frameTime;
        return frame;
    }
}

