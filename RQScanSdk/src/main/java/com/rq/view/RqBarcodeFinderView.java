package com.rq.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

/**
 * @deprecated
 */
class RqBarcodeFinderView extends View {

    float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;
    float mWidth;
    int screenDiff;
    int mHeight;
    Context mContext;
    String  strResult;

    protected RqBarcodeFinderView(Context context, int[] points, int screenWidth, int screenHeight, int screenHeightDiff, float heightRatio, float widthRatio, String result) {
        super(context);
        mInitializePoints(points, heightRatio);
        mWidth = screenWidth;
        mHeight = screenHeight;
        screenDiff = screenHeightDiff;
        mContext = context;
	 strResult = result;  //zzz	
    }

    protected RqBarcodeFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void mInitializePoints(int[] p, float hr) {
        p1x = p[0] * hr;
        p1y = p[1] * hr;
        p2x = p[2] * hr;
        p2y = p[3] * hr;
        p3x = p[4] * hr;
        p3y = p[5] * hr;
        p4x = p[6] * hr;
        p4y = p[7] * hr;
    }

    @Override
    public void onDraw(Canvas canvas) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
	    if (wm == null)
	        return;
        int rotation = wm.getDefaultDisplay().getRotation();
	    Log.d("BarcodeFinderView","onDraw rotation:="+rotation);
	 
	    rotation = Surface.ROTATION_270;   //eric-zhao TODO 270
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                drawPortrait(canvas, rotation);
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                drawLandscape(canvas, rotation);
                break;
            default:
                break;
        }
    }

    private void drawPortrait(Canvas c, int rotate) {
        Paint mPaint = new Paint();
        mPaint.setColor(1748159794);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        Path mPath = new Path();

        int yOffset = 0;
        if (rotate == 2) {
            inversePortrait(mPath, c, mPaint);
        } else {
            mPath.moveTo(mWidth + (-1 * p1y) , p1x - screenDiff);
            mPath.lineTo(mWidth + (-1 * p2y) , p2x- screenDiff);
            mPath.lineTo(mWidth + (-1 * p3y), p3x- screenDiff);
            mPath.lineTo(mWidth + (-1 * p4y), p4x- screenDiff);
            mPath.lineTo(mWidth + (-1 * p1y), p1x - 7- screenDiff);
            c.drawPath(mPath, mPaint);
        }

    }

    private void drawLandscape(Canvas c, int rotation) {
        int xOffset = 0;
        int yOffset = 0;
        int xMulti = 1;
        int yMulti = 1;
        if (rotation == 3) {
            xOffset = (int) mWidth + screenDiff;
            yOffset = (mHeight * -1) ;
            xMulti *= -1;
            yMulti *= -1;
        } else {
            yOffset = screenDiff;
        }

        Paint mPaint = new Paint();
       // mPaint.setColor(1748159794); //1748159794  2070243378
        mPaint.setColor(Color.parseColor("#7CFC00"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
	 

        Path mPath = new Path();

        mPath.moveTo((p1x * xMulti) + xOffset, (p1y * yMulti) - yOffset);
        mPath.lineTo((p2x * xMulti) + xOffset, (p2y * yMulti) - yOffset);
        mPath.lineTo((p3x * xMulti) + xOffset, (p3y * yMulti) - yOffset);
        mPath.lineTo((p4x * xMulti) + xOffset, (p4y * yMulti) - yOffset);
        mPath.lineTo((p1x * xMulti) + xOffset, (p1y * yMulti) - yOffset);

        c.drawPath(mPath, mPaint);
        
        Log.d("BarcodeFinderView","onDraw result:="+strResult);
	  mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(10);	
	  mPaint.setTextSize(25);	

	 if(((p2x * xMulti) + xOffset) >400)  //for right border
         c.drawText(strResult, (p2x * xMulti) + xOffset-110, (p2y * yMulti) - yOffset-20, mPaint);	//zzz
	 else
         c.drawText(strResult, (p2x * xMulti) + xOffset, (p2y * yMulti) - yOffset-20, mPaint);	//zzz
    }


    private void inversePortrait(Path p, Canvas c, Paint paint) {
        float tp1x = p1y;
        float tp1y = (-1 * p1x);
        float tp2x = p2y;
        float tp2y = (-1 * p2x);
        float tp3x = p3y;
        float tp3y = (-1 * p3x);
        float tp4x = p4y;
        float tp4y = (-1 * p4x);

        p.moveTo(tp1x, tp1y + mHeight - screenDiff);
        p.lineTo(tp2x, tp2y + mHeight - screenDiff);
        p.lineTo(tp3x, tp3y + mHeight - screenDiff);
        p.lineTo(tp4x, tp4y + mHeight - screenDiff);
        p.lineTo(tp1x, tp1y + mHeight - screenDiff);
        c.drawPath(p, paint);

    }
}
