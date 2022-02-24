package com.rq.camera;

import android.media.MicrophoneInfo;
import android.media.midi.MidiManager;

import java.util.Arrays;

/**
 * @deprecated
 */
public class MImage{
    private int[] pixelsStride;
    private int[] rowStride;
    private byte[][] byteBuffers;
    private int width;
    private int height;

    public MImage() {
    }
    public MImage(int[] pixelsStride, int[] rowStride, byte[][] byteBuffers, int width, int height) {
        this.pixelsStride = pixelsStride;
        this.rowStride = rowStride;
        this.byteBuffers = byteBuffers;
        this.width = width;
        this.height = height;
    }

    public int[] getPixelsStride() {
        return pixelsStride;
    }

    public void setPixelsStride(int[] pixelsStride) {
        this.pixelsStride = pixelsStride;
    }

    public int[] getRowStride() {
        return rowStride;
    }

    public void setRowStride(int[] rowStride) {
        this.rowStride = rowStride;
    }

    public byte[][] getByteBuffers() {
        return byteBuffers;
    }

    public void setByteBuffers(byte[][] byteBuffers) {
        this.byteBuffers = byteBuffers;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void destory(){
        pixelsStride = null;
        rowStride = null;
        byteBuffers = null;
    }

    public MImage clone(){
        MImage mImage = new MImage();
        mImage.pixelsStride = Arrays.copyOf(pixelsStride,pixelsStride.length);
        mImage.byteBuffers = Arrays.copyOf(byteBuffers,byteBuffers.length);
        mImage.rowStride = Arrays.copyOf(rowStride,rowStride.length);
        mImage.height = height;
        mImage.width = width;
        return mImage;
    }
}
