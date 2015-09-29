package cn.geminiwen.circlepager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import cn.geminiwen.circlepager.adapter.BaseAdapter;

/**
 * Created by geminiwen on 15/9/29.
 */
public class CirclePager extends HorizontalScrollView {
    private final int VELOCITY_SLOT = 1000;
    private final int DEFAULT_AUTO_PLAY_DURATION = 5000;

    private LinearLayout mContainer;
    private LinearLayout.LayoutParams mLinearLayoutParams;
    private VelocityTracker mVelocityTracker;
    private int mCurrPage = 0;

    private BaseAdapter mAdapter;
    private PagerDataSetObserver mPagerDataSetObserver = new PagerDataSetObserver();

    private long mDuration = DEFAULT_AUTO_PLAY_DURATION;
    private boolean mIsAutoPlaying = false;
    private AutoPlayRunnable mAutoPlayRunnable = new AutoPlayRunnable();

    private int mMaximumVelocity;

    private float mCircleRadius;

    private Paint mStrokePaint;
    private Paint mFillPaint;

    public CirclePager(Context context) {
        this(context, null);
    }

    public CirclePager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        Context ctx = getContext();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.mContainer = new LinearLayout(ctx);
        this.mContainer.setOrientation(LinearLayout.HORIZONTAL);
        this.mContainer.setLayoutParams(params);
        this.addView(this.mContainer);
        this.mLinearLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        this.mLinearLayoutParams.weight = 1; // 平等分
        this.setHorizontalScrollBarEnabled(false);
        this.setSmoothScrollingEnabled(true);

        this.mCircleRadius = 8;

        /**
         * 设置松手时velocity的追踪
         */
        final ViewConfiguration configuration = ViewConfiguration.get(ctx);
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        initPaint();
    }

    private void initPaint() {
        mStrokePaint = new Paint();
        mStrokePaint.setStrokeWidth(1.0f);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(Color.WHITE);
        mStrokePaint.setAntiAlias(true);

        mFillPaint = new Paint();
        mFillPaint.setColor(Color.WHITE);
        mFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mFillPaint.setAntiAlias(true);
    }

    public void scrollToNext() {
        this.scrollToPage(mCurrPage + 1, true);
    }

    public void scrollToPrev() {
        this.scrollToPage(mCurrPage - 1, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int size = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        int pagerLength;
        if (size > 1) {
            pagerLength = size + 2;
        } else {
            pagerLength = 1;
        }
        int containerSize = widthSize * pagerLength;
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY: {
                int childWidthSpec = MeasureSpec.makeMeasureSpec(containerSize, MeasureSpec.EXACTLY);
                this.mContainer.measure(childWidthSpec, heightMeasureSpec);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                throw new RuntimeException("Can not be unspecified");
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (mIsAutoPlaying) {
                    removeCallbacks(mAutoPlayRunnable);
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        initVelocityTrackerIfNeed();
        mVelocityTracker.addMovement(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float velocityX = mVelocityTracker.getXVelocity();
                int scrollX = this.getScrollX();
                int width = this.getWidth();
                int page;
                if (Math.abs(velocityX) > VELOCITY_SLOT) {
                    page = scrollX / width;
                    if (velocityX > 0) {
                        page = page - 1;
                    }
                } else {
                    page = (int)Math.round(scrollX * 1.0 / width) - 1;
                }
                this.scrollToPage(page, true);

                recycleVelocityTracker();
                if (mIsAutoPlaying) {
                    postDelayed(mAutoPlayRunnable, mDuration);
                }
                return true;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void initVelocityTrackerIfNeed() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mAutoPlayRunnable);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.scrollToPage(0, false);
        super.onLayout(changed, l, t, r, b);
    }

    /**
     * 滚动到某个页面
     *
     * @param page 页面第n页
     * @param smooth 滑动
     */
    public void scrollToPage(int page, boolean smooth) {
        int size = this.mAdapter == null ? 1: this.mAdapter.getCount();
        if (size > 1) {
            int width = this.mContainer.getChildAt(0).getWidth();
            mCurrPage = page;

            //fix current page num
            if (mCurrPage < 0) {
                mCurrPage = size - 1;
            } else if (mCurrPage == size) {
                mCurrPage = 0;
            }

            if (!smooth) {
                this.scrollTo(width * (page + 1), 0);
            } else {
                this.smoothScrollTo(width * (page + 1), 0);
            }
        } else {
            mCurrPage = size - 1;
            this.scrollTo(0, 0);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        postInvalidate();
    }

    private class AutoPlayRunnable implements Runnable {
        @Override
        public void run() {
            int size = mAdapter.getCount();
            int targetPage = mCurrPage + 1;
            if (targetPage >= size) {
                targetPage = 0;
            }
            scrollToPage(targetPage, true);
            postDelayed(mAutoPlayRunnable, mDuration);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.setAutoPlay(autoPlay, DEFAULT_AUTO_PLAY_DURATION);
    }

    public void setAutoPlay(boolean autoPlay, long duration) {
        mIsAutoPlaying = autoPlay;
        mDuration = duration;
        removeCallbacks(mAutoPlayRunnable);
        if (autoPlay) {
            postDelayed(mAutoPlayRunnable, duration);
        }
    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        int width = this.mContainer.getChildAt(0).getWidth();
        int size = this.mAdapter == null ? 1: this.mAdapter.getCount();
        if (clampedX) {
            if (scrollX > 0) {
                mCurrPage = 0;
                scrollTo(width, 0);
            } else {
                mCurrPage = size - 1;
                scrollTo(width * size, 0);
            }
        }
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas) {
        int width = this.getWidth();
        int height = this.getHeight();

        float threeRadius = 3 * mCircleRadius;

        int size = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        int circleLayoutWidth = (int)(threeRadius * size - mCircleRadius);

        int offsetX = (int)((width - circleLayoutWidth) / 2 + mCircleRadius) + this.getScrollX();   // start pos
        int offsetY = (int)(height - 15 - mCircleRadius);                       // padding Bottom 10px

        int iLoop;
        for (iLoop = 0; iLoop < size; iLoop ++) {
            canvas.drawCircle(offsetX, offsetY, mCircleRadius, mStrokePaint);

            if (iLoop == mCurrPage) {
                canvas.drawCircle(offsetX, offsetY, mCircleRadius, mFillPaint);
            }

            offsetX += threeRadius;
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterObserver(mPagerDataSetObserver);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerObserver(mPagerDataSetObserver);
        this.mAdapter.notifyChanged();
    }

    public class PagerDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            int count = mAdapter.getCount();
            mContainer.removeAllViews();
            if (count < 1) {
                requestLayout();
                return;
            }
            if (count == 1) {
                View view = mAdapter.getView(0);
                mContainer.addView(view, mLinearLayoutParams);
                requestLayout();
                return;
            }
            int position = count - 1;
            View view = mAdapter.getView(position);
            mContainer.addView(view, mLinearLayoutParams);
            for (position = 0; position < count; position ++) {
                view = mAdapter.getView(position);
                mContainer.addView(view, mLinearLayoutParams);
            }
            view = mAdapter.getView(0);
            mContainer.addView(view, mLinearLayoutParams);
            requestLayout();
            scrollToPage(0, false);
        }
    }
}
