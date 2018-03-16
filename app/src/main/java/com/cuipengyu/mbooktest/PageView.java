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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

/**
 * Created by mingren on 2018/3/14.
 */

public class PageView extends View {
    private Paint mPaint;//画笔
    float mTouchToCornerDis;//要计算出手指触到的坐标到页脚坐标的直线距离，需要用到一个变量
    float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};
    private float mMaxLength;
    Matrix mMatrix;
    ColorMatrixColorFilter mColorMatrixFilter;
    int[] mBackShadowColors;// 背面颜色组
    int[] mFrontShadowColors;// 前面颜色组
    /**
     * GradientDrawable mBackShadowDrawableLR;//后面的阴影效果，从左边到右边
     * GradientDrawable mBackShadowDrawableRL;//后面的阴影效果，从右边到左边
     * GradientDrawable mFolderShadowDrawableLR;//夹在中间的阴影效果，从左边到右边
     * GradientDrawable mFolderShadowDrawableRL;//夹在中间的阴影效果，从右边到左边
     * GradientDrawable mFrontShadowDrawableHBT;//前面的阴影效果，右下角到左上角
     * GradientDrawable mFrontShadowDrawableHTB;//前面的阴影效果，从左上到右下
     * GradientDrawable mFrontShadowDrawableVLR;//前面的阴影效果，从左到右
     * GradientDrawable mFrontShadowDrawableVRL;//前面的阴影效果，从右到左
     */
    GradientDrawable mBackShadowDrawableLR;
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

    private Scroller mScroller;

    public PageView(Context context) {
        super(context);
        init();
    }

    public PageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        createDrawable();
        mViewWidth = AppScreenUtil.getmAppScreenUtil().getAppWidth();
        mViewHeight = AppScreenUtil.getmAppScreenUtil().getAppHeight();
        mMaxLength = (float) Math.hypot(mViewWidth, mViewHeight);
        mPaint = new Paint();
        mPath0 = new Path();
        mPath1 = new Path();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        ColorMatrix cm = new ColorMatrix();//设置颜色数组
        float array[] = {1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);
        mMatrix = new Matrix();
        mScroller = new Scroller(getContext(), new LinearInterpolator());
        mTouchX = 0.01f; // 不让x,y为0,否则在点计算时会有问题
        mTouchY = 0.01f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFAAAAAA);
        calcPoints();
        canvas.drawText("a", mTouchX, mTouchY, mPaint);
        canvas.drawText("g", mMiddleX, mMiddleY, mPaint);
        canvas.drawText("e", mBezierControl1.x, mBezierControl1.y, mPaint);
        canvas.drawText("h", mBezierControl2.x, mBezierControl2.y, mPaint);
        canvas.drawText("c", mBezierStart1.x, mBezierStart1.y, mPaint);
        canvas.drawText("j", mBezierStart2.x, mBezierStart2.y, mPaint);
        canvas.drawText("d", mBeziervertex1.x, mBeziervertex1.y, mPaint);
        canvas.drawText("i", mBeziervertex2.x, mBeziervertex2.y, mPaint);
        mCurPageBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        mNextPageBitmap = Bitmap
                .createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        Canvas mCurPageCanvas = new Canvas(mCurPageBitmap);
        Canvas mNextPageCanvas = new Canvas(mNextPageBitmap);
        drawCurrentPageArea(canvas, mCurPageBitmap, mPath0);
        drawNextPageAreaAndShadow(canvas, mNextPageBitmap);
        drawCurrentPageShadow(canvas);
        drawCurrentBackArea(canvas, mCurPageBitmap);
    }

    public void setBitmaps(Bitmap bm1, Bitmap bm2) {
        mCurPageBitmap = bm1;
        mNextPageBitmap = bm2;
    }

    private void startAnimation(int delayMillis) {
        int dx, dy;
        // dx 水平方向滑动的距离，负值会使滚动向左滚动
        // dy 垂直方向滑动的距离，负值会使滚动向上滚动
        if (mCornerX > 0) {
            dx = -(int) (mViewWidth + mTouchX);
        } else {
            dx = (int) (mViewWidth - mTouchX + mViewWidth);
        }
        if (mCornerY > 0) {
            dy = (int) (mViewWidth - mTouchY);
        } else {
            dy = (int) (1 - mTouchY); // // 防止mTouch.y最终变为0
        }
//        if ((mStartY > mScreenHeight / 3 && mStartY < mScreenHeight * 2 / 3) ||  mDirection.equals(Direction.PRE)){
//            mTouchY = mScreenHeight;
//        }
//
//        if (mStartY > mScreenHeight / 3 && mStartY < mScreenHeight / 2 && mDirection.equals(Direction.NEXT)){
//            mTouchY = 1;
//        }
        //Start scrolling by providing a starting point and the distance to travel.
        mScroller.startScroll((int) mTouchX, (int) mTouchY, dx, dy,
                delayMillis);
    }

    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            //停止动画，与forceFinished(boolean)相反，Scroller滚动到最终x与y位置时中止动画。
            mScroller.abortAnimation();
        }
    }

    public boolean canDragOver() {
        //设置开始翻页的条件
//      if (mTouchToCornerDis > mWidth / 10)
        if (mTouchToCornerDis > 1)
            return true;
        return false;
    }

    /**
     * 是否从左边翻向右边
     */
    public boolean DragToRight() {
        if (mCornerX > 0)
            return false;
        return true;
    }

    public void setmTouchY(float y) {
        this.mTouchY = y;
    }

    public boolean doTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            mTouchX = event.getX();
            mTouchY = event.getY();
            /* Android提供了Invalidate和postInvalidate方法实现界面刷新，但是Invalidate不能直接在线程中调用，因为他是违背了单线程模型：
             * Android UI操作并不是线程安全的，并且这些操作必须在UI线程中调用。
             * invalidate()的调用是把之前的旧的view从主UI线程队列中pop掉
             * 而postInvalidate()在工作者线程中被调用
            */
            this.postInvalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouchX = event.getX();
            mTouchY = event.getY();
            // calcCornerXY(mTouch.x, mTouch.y);
            // this.postInvalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //是否触发翻页
            if (canDragOver()) {
                startAnimation(400);
            } else {
                mTouchX = mCornerX - 0.09f;//如果不能翻页就让mTouch返回没有静止时的状态
                mTouchY = mCornerY - 0.09f;//- 0.09f是防止mTouch = 800 或mTouch= 0 ,在这些值时会出现BUG
            }

            this.postInvalidate();
        }
        // return super.onTouchEvent(event);
        return true;
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
        mPath0.reset();//开始画之前需要把路径清空
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);//移动到起始点
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
         *  mPath1.moveTo i
         *  mPath1.lineTo d
         *  mPath1.lineTo b
         *  mPath1.lineTo a
         *  mPath1.lineTo k
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

    private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
        mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
                - mCornerX, mBezierControl2.y - mCornerY));
        int leftx;
        int rightx;
        GradientDrawable mBackShadowDrawable;
        if (mIsRTandLB) {  //左下及右上
            leftx = (int) (mBezierStart1.x);
            rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightx = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        try {
            canvas.clipPath(mPath0);
            canvas.clipPath(mPath1, Region.Op.INTERSECT);
        } catch (Exception e) {
        }


        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
                (int) (mMaxLength + mBezierStart1.y));//左上及右下角的xy坐标值,构成一个矩形
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制翻起页的阴影
     *
     * @param canvas
     */
    public void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRTandLB) {
            degree = Math.PI
                    / 4
                    - Math.atan2(mBezierControl1.y - mTouchY, mTouchX
                    - mBezierControl1.x);
        } else {
            degree = Math.PI
                    / 4
                    - Math.atan2(mTouchY - mBezierControl1.y, mTouchX
                    - mBezierControl1.x);
        }
        // 翻起页阴影顶点与touch点的距离
        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouchX + d1);
        float y;
        if (mIsRTandLB) {
            y = (float) (mTouchY + d2);
        } else {
            y = (float) (mTouchY - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouchX, mTouchY);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();
        try {
            canvas.clipPath(mPath0, Region.Op.XOR);
            canvas.clipPath(mPath1, Region.Op.INTERSECT);
        } catch (Exception e) {
            // TODO: handle exception
        }

        int leftx;
        int rightx;
        GradientDrawable mCurrentPageShadow;
        if (mIsRTandLB) {
            leftx = (int) (mBezierControl1.x);
            rightx = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftx = (int) (mBezierControl1.x - 25);
            rightx = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouchX
                - mBezierControl1.x, mBezierControl1.y - mTouchY));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(leftx,
                (int) (mBezierControl1.y - mMaxLength), rightx,
                (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouchX, mTouchY);
        mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.close();
        canvas.save();
        try {
            canvas.clipPath(mPath0, Region.Op.XOR);
            canvas.clipPath(mPath1, Region.Op.INTERSECT);
        } catch (Exception e) {
        }

        if (mIsRTandLB) {
            leftx = (int) (mBezierControl2.y);
            rightx = (int) (mBezierControl2.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftx = (int) (mBezierControl2.y - 25);
            rightx = (int) (mBezierControl2.y + 1);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y
                - mTouchY, mBezierControl2.x - mTouchX));
        canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
        float temp;
        if (mBezierControl2.y < 0)
            temp = mBezierControl2.y - mViewHeight;
        else
            temp = mBezierControl2.y;

        int hmg = (int) Math.hypot(mBezierControl2.x, temp);
        if (hmg > mMaxLength)
            mCurrentPageShadow
                    .setBounds((int) (mBezierControl2.x - 25) - hmg, leftx,
                            (int) (mBezierControl2.x + mMaxLength) - hmg,
                            rightx);
        else
            mCurrentPageShadow.setBounds(
                    (int) (mBezierControl2.x - mMaxLength), leftx,
                    (int) (mBezierControl2.x), rightx);

        mCurrentPageShadow.draw(canvas);
        canvas.restore();
    }

    //渐变式位图的初始化
    private void createDrawable() {
        /**
         * GradientDrawable.Orientation BL_TR                从绘制渐变左下到右上
         * GradientDrawable.Orientation BOTTOM_TOP           绘制渐变，从底部到顶部
         * GradientDrawable.Orientation BR_TL                从右下角到左上角的绘制渐变
         * GradientDrawable.Orientation LEFT_RIGHT           绘制渐变从左侧到右侧
         * GradientDrawable.Orientation RIGHT_LEFT           从向左右侧绘制渐变
         * GradientDrawable.Orientation TL_BR                绘制渐变，从左上角向右下角
         * GradientDrawable.Orientation TOP_BOTTOM           从顶部至底部绘制渐变
         * GradientDrawable.Orientation TR_BL                从右上角到左下角的绘制渐变
         */
        int[] color = {0x333333, 0xb0333333};
        mFolderShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[]{0xff111111, 0x111111};
        mBackShadowDrawableRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[]{0x80111111, 0x111111};
        mFrontShadowDrawableVLR = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mFrontShadowDrawableVRL = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT
                .setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mTouchX = event.getX();
//                mTouchY = event.getY();
//                calcCornerXY(mTouchX, mTouchY);
//                invalidate();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mTouchX = event.getX();
//                mTouchY = event.getY();
//                calcCornerXY(mTouchX, mTouchY);
//                invalidate();
//                break;
//        }
//        return true;
//    }

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
