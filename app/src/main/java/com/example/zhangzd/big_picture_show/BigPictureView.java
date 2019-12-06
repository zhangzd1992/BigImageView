package com.example.zhangzd.big_picture_show;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.io.InputStream;

/**
 * @Description: 大图长图加载控件
 * @Author: zhangzd
 * @CreateDate: 2019-12-06 10:28
 */
public class BigPictureView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener,GestureDetector.OnDoubleTapListener {
    private BitmapFactory.Options mOptions;
    private GestureDetector mGestureDetector;
    private BitmapRegionDecoder mBitmapRegionDecoder;
    private ScaleGestureDetector mScaleGestureDetector;
    private int mImageWidth;
    private int mImageHeight;
    private int mViewWidth;
    private int mViewHeight;
    private float mScale;  // 缩放比例
    private Rect mRect;
    private Bitmap mBitmap;
    private Scroller mScroller;
    //初始缩放比例
    private float mOriginScale;

    public BigPictureView(Context context) {
        this(context,null);
    }


    public BigPictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }


    public BigPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mOptions = new BitmapFactory.Options();
        mGestureDetector = new GestureDetector(context,this);
        //图片加载区域
        mRect = new Rect();
        // 滚动设置
        mScroller = new Scroller(context);
        setOnTouchListener(this);
        // 设置双击事件
        mGestureDetector.setOnDoubleTapListener(this);
        mScaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);

    }



    public void setImage(InputStream is) {

        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is,null,mOptions);
        mImageWidth = mOptions.outWidth;
        mImageHeight = mOptions.outHeight;
        // 设置开启复用
        mOptions.inMutable = true;

        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        mOptions.inJustDecodeBounds = false;

        try {
            mBitmapRegionDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        // 确定加载图片的区域
        mRect.left = 0;
        mRect.top = 0;
        mRect.right = mImageWidth;
//         得到图片的宽度，就能根据view的宽度计算缩放因子

        mOriginScale = mViewWidth/(float)mImageWidth;
        mScale = mOriginScale;
        mRect.bottom = (int)(mViewHeight/mScale);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmapRegionDecoder == null) {
            return;
        }
        mOptions.inBitmap = mBitmap;
        mBitmap = mBitmapRegionDecoder.decodeRegion(mRect, mOptions);

        Matrix matrix = new Matrix();
        matrix.setScale(mScale,mScale);
        canvas.drawBitmap(mBitmap,matrix,null);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
            mScaleGestureDetector.onTouchEvent(event);
        //将事件交给手势识别器
        return mGestureDetector.onTouchEvent(event);
    }


    /**
     * 停止事件
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        return true;
    }


    /**
     * 处理滑动事件
     * @param e1 就是开始事件，手指按下去，获取坐标
     * @param e2 当前事件
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mRect.offset((int)distanceX, (int) distanceY);

        checkBorder();

        invalidate();
        return false;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.isFinished()) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            mRect.top = mScroller.getCurrY();
            mRect.bottom = mRect.top + (int) (mViewHeight / mScale);
            mRect.left = mScroller.getCurrX();
            mRect.right = mRect.left + (int)(mViewWidth / mScale);
            invalidate();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mScroller.fling(mRect.left,mRect.top,
                -(int)velocityX,-(int)velocityY,
                0,mImageWidth - (int)(mViewWidth / mScale),
                0,mImageHeight - (int)(mViewHeight / mScale));
        return false;
    }



    // 双击手势的重写方法

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mScale < 2) {
            mScale = mOriginScale * 3;
        } else {
            mScale = mOriginScale;
        }
        mRect.right = mRect.left + (int) (mViewWidth / mScale);
        mRect.bottom =mRect.top +  (int)(mViewHeight/mScale);
        checkBorder();

        invalidate();
        return false;
    }

    //检测边界值
    private void checkBorder() {
        // 缩放时，处理到达顶部和底部的情况
        if(mRect.bottom > mImageHeight){
            mRect.bottom = mImageHeight;
            mRect.top = mImageHeight-(int)(mViewHeight/mScale);
        }
        if(mRect.top < 0){
            mRect.top = 0;
            mRect.bottom = (int)(mViewHeight/mScale);
        }
        if(mRect.right > mImageWidth){
            mRect.right = mImageWidth;
            mRect.left = mImageWidth-(int)(mViewWidth/mScale);
        }
        if(mRect.left < 0 ){
            mRect.left = 0;
            mRect.right = (int)(mViewWidth/mScale);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }




   private ScaleGestureDetector.OnScaleGestureListener onScaleGestureListener  =   new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScale += detector.getScaleFactor() - 1;
            if (mScale < mOriginScale) {
                mScale = mOriginScale;
            }else if(mScale > 5* mOriginScale) {
                mScale = 5* mOriginScale;  //最大缩放至五倍大小
            }
            mRect.right = mRect.left +(int)(mViewWidth / mScale) ;
            mRect.bottom = mRect.top + (int)(mViewHeight / mScale);
            checkBorder();
            invalidate();
            return true;
        }
    };







    @Override
    public void onShowPress(MotionEvent e) {

    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }




}
