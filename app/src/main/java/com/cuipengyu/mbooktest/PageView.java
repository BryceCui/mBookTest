package com.cuipengyu.mbooktest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mingren on 2018/3/14.
 */

public class PageView extends View {
    private Paint mPaint;//画笔
    float mTouchToCornerDis;
    float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};
    private float mMaxLength;
    Matrix mMatrix;
    ColorMatrixColorFilter mColorMatrixFilter;
    int[] mBackShadowColors;// 背面颜色组
    int[] mFrontShadowColors;// 前面颜色组
    GradientDrawable mBackShadowDrawableLR; // 有阴影的GradientDrawable
    GradientDrawable mBackShadowDrawableRL;
    GradientDrawable mFolderShadowDrawableLR;
    GradientDrawable mFolderShadowDrawableRL;

    GradientDrawable mFrontShadowDrawableHBT;
    GradientDrawable mFrontShadowDrawableHTB;
    GradientDrawable mFrontShadowDrawableVLR;
    GradientDrawable mFrontShadowDrawableVRL;
    Bitmap mCurPageBitmap = null; // 当前页
    Bitmap mNextPageBitmap = null;
    /**
     * 两条Path路径
     */
    private android.graphics.Path mPath0;//jhk和
    private android.graphics.Path mPath1;
    /**
     * 第一条贝塞尔曲线
     * mBezierStart1
     * mBezierControl1  e点
     * mBeziervertex1
     * mBezierEnd1
     */
    private PointF mBezierStart1 = new PointF();//起始点
    private PointF mBezierControl1 = new PointF();//控制点
    private PointF mBeziervertex1 = new PointF();//顶点
    private PointF mBezierEnd1 = new PointF();//结束点

    /**
     * 第二条贝塞尔曲线
     * mBezierStart1
     * mBezierControl1  h点
     * mBeziervertex1
     * mBezierEnd1
     */
    private PointF mBezierStart2 = new PointF();//起始点
    private PointF mBezierControl2 = new PointF();//控制点
    private PointF mBeziervertex2 = new PointF();//顶点
    private PointF mBezierEnd2 = new PointF();//结束点
    /**
     * af中点g的坐标
     */
    private float mMiddleX;
    private float mMiddleY;
    /**
     * f点的坐标
     * 拖拽点的页脚，左上0，0；右上mWidth,0；左下0，mHeight；右下mWidth,mHeight；
     */
    float mCornerX;
    float mCornerY;

    /**
     * 屏幕的尺寸
     */
    int mViewWidth;
    int mViewHeight;

    /**
     * 按下的坐标
     */
    float mTouchX;
    float mTouchY;

    /**
     * 是否属于右上左下
     */
    boolean mIsRTandLB;

    private float mDegrees; //角度

    public PageView(Context context) {
        super(context);
        init();
    }

    public PageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calcPoints();
        canvas.drawText("a", mTouchX, mTouchY, mPaint);
        canvas.drawText("g", mMiddleX, mMiddleY, mPaint);
        canvas.drawText("e", mBezierControl1.x, mBezierControl1.y, mPaint);
        canvas.drawText("h", mBezierControl2.x, mBezierControl2.y, mPaint);
        canvas.drawText("c", mBezierStart1.x, mBezierStart1.y, mPaint);
        canvas.drawText("j", mBezierStart2.x, mBezierStart2.y, mPaint);
        canvas.drawText("d", mBeziervertex1.x, mBeziervertex1.y, mPaint);
        canvas.drawText("i", mBeziervertex2.x, mBeziervertex2.y, mPaint);
        //绘制当前页
        //TODO        drawCurrentPageArea
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
                mBezierEnd1.y);
        mPath0.lineTo(mTouchX, mTouchY);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
                mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();
        canvas.save();
        canvas.drawPath(mPath0, mPaint);
        canvas.clipPath(mPath0, Region.Op.XOR);

        canvas.restore();
        //绘制翻起背面
        //TODO        drawCurrentBackArea
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouchX, mTouchY);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        canvas.save();
        canvas.drawPath(mPath1, mPaint);
        canvas.clipPath(mPath1, Region.Op.XOR);
        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
                - mCornerX, mBezierControl2.y - mCornerY));
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);

    }

    /**
     * 绘制当前页面区域
     *
     * @param canvas
     * @param bitmap
     * @param path
     */
    private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
        /**
         * mPath0.moveTo(cx, cy);
         * mPath0.quadTo(ex, ey, bx, by);
         * mPath0.lineTo(ax, ay);
         * mPath0.lineTo(kx, ky);
         * mPath0.quadTo(hx, hy, jx,jy);
         * mPath0.lineTo(fx, fy);
         * mPath0.close();
         * canvas.restore(); 此调用将先前调用save（），并用于移除自上次保存调用以来对矩阵/剪辑状态的所有修改。
         */
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
                mBezierEnd1.y);
        mPath0.lineTo(mTouchX, mTouchY);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
                mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();
        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);
        canvas.drawBitmap(bitmap, 0, 0, null);
        try {
            canvas.restore();
        } catch (Exception e) {

        }
    }

    /**
     * 绘制翻起页背面
     *
     * @param canvas
     * @param bitmap
     */
    private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
        //  (cx+ex)/2
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        // Math.abs返回 double 值的绝对值
        float f1 = Math.abs(i - mBezierControl1.x);
        //   (jy+hy)/2
        int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
        float f2 = Math.abs(i1 - mBezierControl2.y);
        //比较结果中返回较小的那个数值
        float f3 = Math.min(f1, f2);
        /**
         *  mPath1.moveTo
         *  mPath1.lineTo
         *  mPath1.lineTo
         *  mPath1.lineTo
         *  mPath1.lineTo
         */
        mPath1.reset();
        mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouchX, mTouchY);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRTandLB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left = (int) (mBezierStart1.x - f3 - 1);
            right = (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        try {
            canvas.clipPath(mPath0);
            canvas.clipPath(mPath1, Region.Op.INTERSECT);
        } catch (Exception e) {
        }

        mPaint.setColorFilter(mColorMatrixFilter);
        //对Bitmap进行取色
        int color = bitmap.getPixel(1, 1);
        //获取对应的三色
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        //转换成含有透明度的颜色
        int tempColor = Color.argb(200, red, green, blue);

        //返回它的所有参数的平方和的平方根
        float dis = (float) Math.hypot(mCornerX - mBezierControl1.x,
                mBezierControl2.y - mCornerY);
        float f8 = (mCornerX - mBezierControl1.x) / dis;
        float f9 = (mBezierControl2.y - mCornerY) / dis;
        mMatrixArray[0] = 1 - 2 * f9 * f9;
        mMatrixArray[1] = 2 * f8 * f9;
        mMatrixArray[3] = mMatrixArray[1];
        mMatrixArray[4] = 1 - 2 * f8 * f8;
        //TODO  矩阵的学习
        mMatrix.reset();
        mMatrix.setValues(mMatrixArray);
        mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
        mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
        canvas.drawBitmap(bitmap, mMatrix, mPaint);
        //背景叠加
        canvas.drawColor(tempColor);

        mPaint.setColorFilter(null);

        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right,
                (int) (mBezierStart1.y + mMaxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }

    private void init() {

        mViewWidth = AppScreenUtil.getmAppScreenUtil().getAppWidth();
        mViewHeight = AppScreenUtil.getmAppScreenUtil().getAppHeight();
        mTouchX = 0.01f; // 不让x,y为0,否则在点计算时会有问题
        mTouchY = 0.01f;
        mPaint = new Paint();
        mPath0 = new Path();
        mPath1 = new Path();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(20);
        ColorMatrix cm = new ColorMatrix();//设置颜色数组
        float array[] = {1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        mMatrix = new Matrix();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                calcCornerXY(mTouchX, mTouchY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchX = event.getX();
                mTouchY = event.getY();
                calcCornerXY(mTouchX, mTouchY);
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 根据按下的位置计算拖拽点的坐标属于哪一象限内
     */
    public void calcCornerXY(float x, float y) {
        if (x < mViewWidth / 2) {
            mCornerX = 0;
        } else {
            mCornerX = mViewWidth;
        }
        if (y < mViewHeight / 2) {
            mCornerY = 0;
        } else {
            mCornerY = mViewHeight;
        }
        if ((mCornerX == 0 && mCornerY == mViewHeight) || (mCornerX == mViewHeight && mCornerY == 0)) {
            mIsRTandLB = true;
        } else {
            mIsRTandLB = false;
        }
    }

    /**
     * 计算个点坐标
     */
    public void calcPoints() {
        //g=(a+f)/2         g.x = (a.x + f.x) / 2;
        mMiddleX = (mTouchX + mCornerX) / 2;
//        g.y = (a.y + f.y) / 2;
        mMiddleY = (mTouchY + mCornerY) / 2;
        //e点的坐标(gx-em , mHeight)   em=gm*gm/mf;         e.x = g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x);
        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;
        //h点
        mBezierControl2.x = mCornerX;
        //gm
        float f4 = mCornerY - mMiddleY;
        if (f4 == 0) {
            mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                    * (mCornerX - mMiddleX) / 0.1f;

        } else {
            mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                    * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
        }
//        c.x = e.x - (f.x - e.x) / 2;
//        c.y = f.y;
        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
                / 2;
        mBezierStart1.y = mCornerY;

        // 当mBezierStart1.x < 0或者mBezierStart1.x > 480时
        // 如果继续翻页，会出现BUG故在此限制
        if (mTouchX > 0 && mTouchX < mViewWidth) {
            if (mBezierStart1.x < 0 || mBezierStart1.x > mViewWidth) {
                if (mBezierStart1.x < 0)
                    mBezierStart1.x = mViewWidth - mBezierStart1.x;

                float f1 = Math.abs(mCornerX - mTouchX);
                float f2 = mViewWidth * f1 / mBezierStart1.x;
                mTouchX = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouchX)
                        * Math.abs(mCornerY - mTouchY) / f1;
                mTouchY = Math.abs(mCornerY - f3);

                mMiddleX = (mTouchX + mCornerX) / 2;
                mMiddleY = (mTouchY + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
                        * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;

                float f5 = mCornerY - mMiddleY;
                if (f5 == 0) {
                    mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                            * (mCornerX - mMiddleX) / 0.1f;
                } else {
                    mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
                            * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
                }

                mBezierStart1.x = mBezierControl1.x
                        - (mCornerX - mBezierControl1.x) / 2;
            }
        }
//        j.x = f.x;
//        j.y = h.y - (f.y - h.y) / 2;
        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
                / 2;
        mTouchToCornerDis = (float) Math.hypot((mTouchX - mCornerX),
                (mTouchY - mCornerY));
        //b,k
        mBezierEnd1 = getCross(new PointF(mTouchX, mTouchY), mBezierControl1, mBezierStart1,
                mBezierStart2);
        mBezierEnd2 = getCross(new PointF(mTouchX, mTouchY), mBezierControl2, mBezierStart1,
                mBezierStart2);
//        d.x = (c.x + 2 * e.x + b.x) / 4;
//        d.y = (2 * e.y + c.y + b.y) / 4;
//        i.x = (j.x + 2 * h.x + k.x) / 4;
//        i.y = (2 * h.y + j.y + k.y) / 4;
        mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    /**
     * 求解直线P1P2和直线P3P4的交点坐标
     */
    public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        //将之前求得的 a,e,c,j四个点带入上式则可以求出 b. 同理可求k点
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }
}
