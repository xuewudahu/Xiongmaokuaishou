package com.rq.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.rq.camera.Frame;
import com.rq.camera.FrameSaver;
import com.rq.camera.MImage;
import com.rq.camera.RqCameraManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * 杂项工具类
 */
public class MiscUtil {
    private static final String TAG = MiscUtil.getTag(MiscUtil.class);

    public static final int NO_ERROR = 0;
    public static final int ERROR_NULL_POINT = -101;
    public static final int ERROR_NULL_DATA = -102;
    public static final int ERROR_UNACTIVATED = -103;

    public static final int YUV420P = 0;
    public static final int YUV420SP = 1;
    public static final int NV21 = 2;

    public static final boolean DEBUG = true;
    private static final String PREFIX_TAG = "Boogoob_";
    public  static Bitmap bitmap;
    /**
     * 格式化num
     * 00001
     * 00002
     */
    private final static String FORMAT = "000000";

    private final static int FILE_NAME_MAX_LENGTH = 32;

    public final static String getTag(Class object){
        return PREFIX_TAG+object.getSimpleName();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final static MImage copyImageToMImage(Image image){
        if (image == null) {
            if(DEBUG)
                Log.d(TAG, "copyImageToMImage error: image is null, return.");
            return null;
        }
        try {
            MImage mImage = new MImage();
            //获取源数据，如果是YUV格式的数据planes.length = 3
            //plane[i]里面的实际数据可能存在byte[].length <= capacity (缓冲区总大小)
            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();


            int[] pixelsStride = new int[planes.length];
            int[] rowStride= new int[planes.length];
            byte[][] buffers = new byte[planes.length][];
            for (int i = 0; i < planes.length; i++) {
                pixelsStride[i] = planes[i].getPixelStride();
                rowStride[i] = planes[i].getRowStride();
                buffers[i] = new byte[planes[i].getBuffer().capacity()];
                planes[i].getBuffer().get(buffers[i]);
            }
            mImage.setByteBuffers(buffers);
            mImage.setHeight(image.getHeight());
            mImage.setWidth(image.getWidth());
            mImage.setPixelsStride(pixelsStride);
            mImage.setRowStride(rowStride);
            return mImage;
        } catch (final Exception e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public final static byte[] getBytesFromMImageAsType(MImage mImage, int type) {
        try {
            int width = mImage.getWidth();
            int height = mImage.getHeight();
            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < mImage.getByteBuffers().length; i++) {
                pixelsStride = mImage.getPixelsStride()[i];
                rowStride = mImage.getRowStride()[i];

                byte[] bytes = mImage.getByteBuffers()[i];

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }

            //   image.close();

            //根据要求的结果类型进行填充
            switch (type) {
                case YUV420P:
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
                    break;
                case YUV420SP:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = uBytes[i];
                        yuvBytes[dstIndex++] = vBytes[i];
                    }
                    break;
                case NV21:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = vBytes[i];
                        yuvBytes[dstIndex++] = uBytes[i];
                    }
                    break;
            }
            return yuvBytes;
        } catch (final Exception e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 注册联网判断
     * @return 网络是否连通
     */
    public final static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final static Frame packImageDataToFrame(Image image) {
        try {
            return new Frame(MiscUtil.copyImageToMImage(image), System.currentTimeMillis(), null);
        } catch (Exception e) {
            e.printStackTrace();
            if (MiscUtil.DEBUG)
                Log.e(TAG,"packImageDataToFrame exception :"+e.getMessage());
        }
        return null;
    }

    /**
     * 从指定yuv文件读取转换为Frame，用于解码
     * @return yuv文件转换的Frame对象
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Frame packDataFromYuvFile(String path) {
        File sampleFile = new File(path);
        if (!sampleFile.exists()) {
            if(DEBUG)
                Log.d(TAG, "file not exsits, return null.");
            return null;
        }

        int IMAGESIZE = RqCameraManager.DecodePreviewWidth * RqCameraManager.DecodePreviewHeight;
        byte[] buffer = new byte[IMAGESIZE];
        FileInputStream fis = null;
        int len = -1;

        try {
            fis = new FileInputStream(sampleFile);
            len = fis.read(buffer);
        } catch (IOException e) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {

                }
            }
        }
        if(DEBUG)
            Log.d(TAG, "onPreviewFrame SAMPLE.");
        return new Frame(System.currentTimeMillis(), buffer); //eric
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String doSave(Context context, byte[] data, String name, int frameSaveFormat){
        if (frameSaveFormat == FrameSaver.FORMAT_YUV) {
            return yuvSave(data, name, frameSaveFormat);
        } else if (frameSaveFormat == FrameSaver.FORMAT_PNG) {
            return rgbSave(context,data, name,frameSaveFormat);
        } else if (frameSaveFormat == FrameSaver.FORMAT_JPEG) {
            //yuvSave(data, name, FrameSaver.FORMAT_YUV);//  added by wxw
            return rgbSave(context,data, name,frameSaveFormat);
        } else {
            if(DEBUG)
                Log.d(TAG, "doSave error: wrong frameSaveFormat.");
        }
        return null;
    }

    /**
     * 把桢中数据存为yuv数据格式到sdcard
     * @param data
     * @param name
     * @return 文件路径
     */
    public static String yuvSave(byte[] data,String name, int rgbFormat) {

        if(DEBUG)
            Log.d(TAG, "imageSave  name:=" + name+" data.length="+data.length);
        File imageFile = new File(Environment.getExternalStorageDirectory() + name
                + getSuffix(rgbFormat));
        FileOutputStream fos = null;
        try {
            if(imageFile.exists()) {
                if(DEBUG)
                    Log.d(TAG, "file exsits, overwrite.");
            }
            fos = new FileOutputStream(imageFile);
            fos.write(data, 0, data.length * 2 / 3);
        } catch (IOException e) {
            e.printStackTrace();
            if(DEBUG)
                Log.d(TAG, "yuvSave  write exception:"+e.getMessage());
            return null;
        } finally {
            imageFile = null;
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Environment.getExternalStorageDirectory() + name + getSuffix(rgbFormat);
    }

    /***
     * YUV420 转化成 RGB
     */
    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height)
    {
        
        final int frameSize = width * height;
        int rgb[] = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    /***
     * YUV420 RenderScript 转换为 RGB
     */
    public static Bitmap nv21ToBitmap(Context context,byte[] yuv420sp, int width, int height) {
        final NV21ToBitmap nv21ToBitmap = new NV21ToBitmap(context);
        try {
            return nv21ToBitmap.nv21ToBitmap(yuv420sp,width,height);
        }finally {
            nv21ToBitmap.destroy();
        }
    }

    private static Bitmap getNewBitmap() {
        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/DCIM/899401528678.png");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap, 0, 0, width , height);
    }
    /**
     * 把桢中数据存为yuv数据格式到sdcard
     * @param data
     * @param
     * @return 文件路径
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String rgbSave(Context context,byte[] data, String path,int rgbFormat) {
        if(DEBUG)
            Log.d(TAG, "rgbSave  name:=" + path);
        File imageFile = new File(Environment.getExternalStorageDirectory() + path + getSuffix(rgbFormat));
        bitmap = nv21ToBitmap(context,data,RqCameraManager.DecodePreviewWidth, RqCameraManager.DecodePreviewHeight);
        mLinstener.savePicture(bitmap,path);
        Log.d("picture------00000","--1111");
//        Log.d("picture  rgbSave","----"+Environment.getExternalStorageDirectory().getPath()+"/123.jpg");
//        BufferedOutputStream bos = null;
//        try {
//            bos = new BufferedOutputStream(new FileOutputStream(imageFile));
//            bitmap.compress(getBitmapCompressFormat(rgbFormat), FrameSaver.QUALITY, bos);
//            bos.flush();
//            bos.close();
//            bitmap.recycle();
//        } catch (Exception e) {
//            e.printStackTrace();
//            if(DEBUG)
//                Log.d(TAG, "rgbSave  write exception:"+e.getMessage());
//            return null;
//        } finally {
//            if (bos != null) {
//                try {
//                    bos.close();
//                    bos = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if(bitmap != null) {
//                bitmap.recycle();
//            }
//            imageFile = null;
//        }
        return Environment.getExternalStorageDirectory() + path + getSuffix(rgbFormat);
    }
    public  static void  setListener(Listener listener) {
        //把传入的 listener 赋值给 mLinstener
        mLinstener = listener;
    }
    public static Listener mLinstener;
    public interface Listener {
        //回调方法
        void savePicture(Bitmap bitmap,String s);
    }
    public final static byte[] getFrameData(Frame frame) {
        if(frame.getData() != null) {
            return frame.getData();
        }else if (frame.getFrameImage() != null) {
            byte[] data = MiscUtil.getBytesFromMImageAsType(
                    frame.getFrameImage(),
                    MiscUtil.NV21);
            if(MiscUtil.DEBUG)
                Log.d(TAG, "getFrameData data size="+data.length);
            return data;
        } else {
            if(MiscUtil.DEBUG)
                Log.e(TAG, "getFrameData error: data not avaible.");
            return null;
        }
    }

    /**
     * 通过文件关键字，按顺序获取一个新的文件名称，防止写入覆盖
     * @param folderInSdcard
     * @param prefix
     * @param format
     * @return 计划要生成的文件名
     */
    public static String getNewFilePath(int saveNumMax,String folderInSdcard,String prefix,String format) {
        String folderPath = Environment.getExternalStorageDirectory()+folderInSdcard;
        String prefixPath = folderPath+prefix;
        File folderDir = new File(folderPath);
        if(!folderDir.exists() || !folderDir.isDirectory()) {
            folderDir.mkdir();
        }
        List<String> listFilePath = new ArrayList<String>();
        File[] allFileInDir = folderDir.listFiles();

        for(int i = 0;i < allFileInDir.length;i ++) {
            File file  = allFileInDir[i];
            if(DEBUG)
                Log.i(TAG,"file.getPath()："+file.getPath()+" prefix:"+prefix);
            if (file.getPath().contains(prefix)) {
                listFilePath.add(file.getPath());
            }
        }
        Collections.sort(listFilePath, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        /**
         * 如果数量超过saveNumMax，从头删除一张，然后重新排序命名
         */
        if(listFilePath.size() >= saveNumMax) {
            int needRemoveCount = listFilePath.size() - saveNumMax;
            for(int i = needRemoveCount; i >= 0;i --){
                String deletePath = listFilePath.get(0);
                if(DEBUG)
                    Log.i(TAG,"deletePath："+deletePath);
                /**
                 * 删除文件
                 */
                File deleteFile = new File(deletePath);
                deleteFile.delete();
                /**
                 * 清除路径缓存
                 */
                listFilePath.remove(deletePath);
            }
        }
        /**
         * 如果文件不涉及重复覆盖，则不去进行文件排序
         */
        File newFile = new File(prefixPath+format(listFilePath.size())+format);
        if(newFile.exists()) {

            for (int i = 0; i < listFilePath.size(); i++) {
                if(DEBUG)
                    Log.i(TAG,"listFilePath.get(i)="+listFilePath.get(i));
                File renameFile = new File(listFilePath.get(i));
                File newNameFile = new File(prefixPath + format(i) + format);
                renameFile.renameTo(newNameFile);
            }
        }
        /**
         * reutnrn custom path ,like /DCIM/DEBUG_2   /DCIM/DEBUG_3 。。。
         */
        return folderInSdcard+prefix+format(listFilePath.size());
    }

    /**
     * 通过条码名称获取文件名称路径，防止写入覆盖
     * @param folderInSdcard
     * @param barcode
     * @param format
     * @return 计划要生成的文件名
     */
    public static String getNewFilePathByBarcode(String folderInSdcard,String barcode,String format) {
        String folderPath = Environment.getExternalStorageDirectory()+folderInSdcard;
        final int barcodeLength = barcode.trim().length();
        /**
         * barcode如果长度太长，限定其文件长度
         */
        String fileName =
                (barcodeLength > FILE_NAME_MAX_LENGTH ?
                        barcode.trim().substring(barcodeLength - FILE_NAME_MAX_LENGTH):
                        barcode.trim());
        /**
         * 文件夹不存在重建
         */
        File folderDir = new File(folderPath);
        if(!folderDir.exists() || !folderDir.isDirectory()) {
            folderDir.mkdir();
        }
        List<String> listFilePath = new ArrayList<String>();

        /**
         * 如果文件存在，就不再创建
         */
        File newFile = new File(folderPath + fileName + format);
        if(newFile.exists()) {
            if(DEBUG)
                Log.e(TAG,"getNewFilePathByBarcode error:file exits ，barcode："+barcode);
            return  null;
        }
        /**
         * reutnrn custom path ,like /DCIM/barcode1   /DCIM/barcode2 。。。
         */
        return folderInSdcard+fileName;
    }
    /**
     * 获取文件尾缀
     * @param frameSaveFormat
     * @return 文件尾缀
     */
    public static String getSuffix(int frameSaveFormat) {
        switch (frameSaveFormat) {
            case FrameSaver.FORMAT_YUV:
                return ".yuv";
            case FrameSaver.FORMAT_PNG:
                return ".png";
            case FrameSaver.FORMAT_JPEG:
                return ".jpg";
        }
        return null;
    }
    /**
     * 格式化编号数据
     * @param num
     * @return 格式化后的编号
     */
    private final static String format(int num){
        String formatString = Integer.toString(num);
        if (formatString.length() > FORMAT.length()) {
            if(DEBUG)
                Log.e(TAG,"error:num is too large,can't format.");
            return FORMAT;
        }
        return FORMAT.substring(0,FORMAT.length() - formatString.length())+formatString;
    }

    /**
     * 文件格式
     * @param rgbFormat
     * @return
     */
    private static Bitmap.CompressFormat getBitmapCompressFormat(int rgbFormat) {
        switch (rgbFormat) {
            case FrameSaver.FORMAT_PNG:
                return Bitmap.CompressFormat.PNG;
            case FrameSaver.FORMAT_JPEG:
                return Bitmap.CompressFormat.JPEG;
        }
        return null;
    }


}
