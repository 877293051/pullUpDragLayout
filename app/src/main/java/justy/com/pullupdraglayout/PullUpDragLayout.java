package justy.com.pullupdraglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public class PullUpDragLayout extends ViewGroup {

    private ViewDragHelper mViewDragHelper;//拖拽帮助类
    private View mBottomView;//底部内容View
    private View mContentView;//内容View
    private LayoutInflater mLayoutInflater;
    private int mBottomBorderHeigth = 0;//底部边界凸出的高度
    private Point mAutoBackBottomPos = new Point();
    private Point mAutoBackTopPos = new Point();
    private int mBoundTopY;
    private boolean isOpen;
    private OnStateListener mOnStateListener;
    public static final int LOCATION_BOTTOM = 0;
    public static final int LOCATION_CENTER = 1;
    public static final int LOCATION_TOP = 2;
    private int mCurrentState = LOCATION_BOTTOM;
    private int mViewPositionTop;//记录View当前位置
    private int mLayoutBottom;
    private boolean isInitPosition = true; //是否初始化设置位置
    private float mRate;
    private boolean isNotCenter; //上拉不在中间停顿。
    private int mCenterHeight; //中间位置高度值

    public void setCenterHeight(int centerHeight) {
        this.mCenterHeight = centerHeight;
    }

    public void setNotCenter(boolean notCenter) {
        isNotCenter = notCenter;
    }

    public void setInitPosition(boolean initPosition) {
        isInitPosition = initPosition;
    }

    public View getContentView() {
        return mContentView;
    }

    /**
     * 设置显示部分高度
     *
     * @param mBottomBorderHeigth
     */
    public void setmBottomBorderHeigth(int mBottomBorderHeigth) {
        this.mBottomBorderHeigth = mBottomBorderHeigth;
        mBoundTopY = mContentView.getHeight() - mBottomBorderHeigth - DisplayUtils.getRelativeViewHeightInpx(getContext(), 80);
        isInitPosition = true;
        invalidate();
    }

    public void setOnStateListener(OnStateListener onStateListener) {
        mOnStateListener = onStateListener;
    }

    public interface OnStateListener {
        void initState();

        void onScrollChange();

        void pullState(int state);
    }

    /**
     * 是否滚动至顶部
     *
     * @return
     */
    public boolean isTop() {
        return mRate == 1f;
    }

    /**
     * 是否滚动接近顶部
     *
     * @return
     */
    private boolean isNearTop() {
        return mRate > 0.8f;
    }

    /**
     * 是否滚动至底部
     *
     * @return
     */
    public boolean isBottom() {
        return mRate == 0f;
    }

    /**
     * 是否滚动至中间
     *
     * @return
     */
    private boolean isCenter() {
        return mRate == mCenterHeight;
    }

    public PullUpDragLayout(Context context) {
        this(context, null, 0);
    }

    public PullUpDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullUpDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initCustomAttrs(context, attrs);
    }

    private void init(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mBottomView == child;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - mBottomView.getWidth() - leftBound;
            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int topBound = mContentView.getHeight() - mBottomView.getHeight();
            int bottomBound = mContentView.getHeight() - mBottomBorderHeigth;
            return Math.min(bottomBound, Math.max(top, topBound));
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mBottomView) {
                isInitPosition = false;
                mViewPositionTop = top;
                float startPosition = mContentView.getHeight() - mBottomView.getHeight();
                float endPosition = mContentView.getHeight() - mBottomBorderHeigth;
                float totalLength = endPosition - startPosition;
                float rate = 1 - ((top - startPosition) / totalLength);
                mRate = rate;
                if (mOnStateListener != null) {
                    mOnStateListener.onScrollChange();
                }
            }
        }


        //手指释放的时候回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (releasedChild != mBottomView) {
                return;
            }
            if (isNotCenter){   //中间不停留
                if (mCurrentState==LOCATION_TOP&&(yvel>10||mRate < 0.92f)) {
                    mCurrentState = LOCATION_BOTTOM;
                }
                else if (mCurrentState==LOCATION_BOTTOM&&(yvel<-10||mRate > 0.08f)) {
                    mCurrentState = LOCATION_TOP;
                }
            }else {
                if (yvel > 10) {
                    if (mCurrentState > LOCATION_BOTTOM) {
                        mCurrentState--;
                    }
                } else if (yvel < -10) {
                    if (mCurrentState < LOCATION_TOP) {
                        mCurrentState++;
                    }
                } else {
                    if (mRate <= 0.08f) {
                        mCurrentState = LOCATION_BOTTOM;
                    } else if (mRate <= 0.6f) {
                        mCurrentState = LOCATION_CENTER;
                    } else {
                        mCurrentState = LOCATION_TOP;
                    }
                }
            }
            switch (mCurrentState) {
                case LOCATION_BOTTOM:
                    isOpen = false;
                    mViewDragHelper.settleCapturedViewAt(mAutoBackBottomPos.x, mAutoBackBottomPos.y);
                    break;
                case LOCATION_CENTER:
                    isOpen = false;
                    mViewDragHelper.settleCapturedViewAt(mAutoBackTopPos.x, mCenterHeight);
                    break;
                case LOCATION_TOP:
                    isOpen = true;
                    mViewDragHelper.settleCapturedViewAt(mAutoBackTopPos.x, mAutoBackTopPos.y);
                    break;
                default:
                    break;
            }

            if (mOnStateListener != null) {
                mOnStateListener.pullState(mCurrentState);
            }
            invalidate();
        }

    };

    public boolean isOpen() {
        return isOpen;
    }

    public void toggleCenterView() {
        if (isOpen) {
            mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackBottomPos.x, mAutoBackBottomPos.y);
            if (mOnStateListener != null) mOnStateListener.pullState(LOCATION_BOTTOM);
        } else {
            mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackTopPos.x, mCenterHeight);
            if (mOnStateListener != null) mOnStateListener.pullState(LOCATION_CENTER);
        }
        invalidate();
        isOpen = !isOpen;
    }

    public void close() {
        isOpen = false;
        isInitPosition = true;
        mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackBottomPos.x, mAutoBackBottomPos.y);
        if (mOnStateListener != null) mOnStateListener.pullState(LOCATION_BOTTOM);
        invalidate();
        mRate = 0f;
    }

    /**
     * 切换底部View
     */
    public void toggleBottomView() {
        if (isOpen) {
            mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackBottomPos.x, mAutoBackBottomPos.y);
            if (mOnStateListener != null) mOnStateListener.pullState(LOCATION_BOTTOM);
        } else {
            mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackTopPos.x, mAutoBackTopPos.y);
            if (mOnStateListener != null) mOnStateListener.pullState(LOCATION_TOP);
        }
        invalidate();
        isOpen = !isOpen;
    }

    public void setPullTop() {
        mViewDragHelper.smoothSlideViewTo(mBottomView, mAutoBackBottomPos.x, 0);
        invalidate();
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PullUpDragLayout);
        if (typedArray != null) {
            if (typedArray.hasValue(R.styleable.PullUpDragLayout_PullUpDrag_ContentView)) {
                inflateContentView(typedArray.getResourceId(R.styleable.PullUpDragLayout_PullUpDrag_ContentView, 0));
            }
            if (typedArray.hasValue(R.styleable.PullUpDragLayout_PullUpDrag_BottomView)) {
                inflateBottomView(typedArray.getResourceId(R.styleable.PullUpDragLayout_PullUpDrag_BottomView, 0));
            }
            if (typedArray.hasValue(R.styleable.PullUpDragLayout_PullUpDrag_BottomBorderHeigth)) {
                mBottomBorderHeigth = (int) typedArray.getDimension(R.styleable.PullUpDragLayout_PullUpDrag_BottomBorderHeigth, 250);
            }
            typedArray.recycle();
        }

    }

    private void inflateContentView(int resourceId) {
        mContentView = mLayoutInflater.inflate(resourceId, this, true);
    }

    private void inflateBottomView(int resourceId) {
        mBottomView = mLayoutInflater.inflate(resourceId, this, true);
    }

    public void setBottomViewHide(boolean isHide) {
        if (isHide)
            mBottomView.setVisibility(View.GONE);
        else
            mBottomView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mContentView = getChildAt(0);
        mBottomView = getChildAt(1);
        measureChild(mBottomView, widthMeasureSpec, heightMeasureSpec);
        int bottomViewHeight = mBottomView.getMeasuredHeight();
        measureChild(mContentView, widthMeasureSpec, heightMeasureSpec);
        int contentHeight = mContentView.getMeasuredHeight();
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), bottomViewHeight + contentHeight + getPaddingBottom() + getPaddingTop());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mContentView = getChildAt(0);
        mBottomView = getChildAt(1);
        if (isInitPosition) {
            mViewPositionTop = mContentView.getHeight() - mBottomBorderHeigth;
        }
        mLayoutBottom = mViewPositionTop + mContentView.getHeight();
        mContentView.layout(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), mContentView.getMeasuredHeight());
        mBottomView.layout(getPaddingLeft(), mViewPositionTop, getWidth() - getPaddingRight(), mLayoutBottom);
        if (isInitPosition) {
            mAutoBackBottomPos.x = mBottomView.getLeft();
            mAutoBackBottomPos.y = mBottomView.getTop();
            mAutoBackTopPos.x = mBottomView.getLeft();
            mAutoBackTopPos.y = mContentView.getHeight() - mBottomView.getHeight();
        }
    }

    /**
     * 核对是否有拖动
     */
    private void checkMove() {
        if (mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {//没有移动位置
            if (mOnStateListener != null) {
                isOpen = false;
                mOnStateListener.initState();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

}

