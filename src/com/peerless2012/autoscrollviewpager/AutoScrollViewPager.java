package com.peerless2012.autoscrollviewpager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.IntDef;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
* @Author peerless2012
* @Email peerless2012@126.com
* @DateTime 2016年5月18日 上午12:25:17
* @Version V1.0
* @Description: 自动滚动的无限轮播的ViewPager,注意：本自定义控件不适于用到带有指示器等。
*/
public class AutoScrollViewPager extends ViewPager {
	
	/**
	 * 自动追加到集合前后的条目个数
	 */
	public final static int DEFAULT_AUTO_INCREASE = 2;
	
	
	/**
	 * ViewPager中，Pager之间的间距（dp）
	 */
	public final static int DEFAULT_PAGE_MARGIN = 10;
	
	/**
	 * 页面切换动画的时间间隔
	 */
	public final static int DEFAULT_SCROLL_TIME = 1500;
	
	/**
	 * 从右向左向左滚动
	 */
	public final static int SCROLL_ORIENTATION_RIGHT_TO_LEFT = 0x00;
	
	/**
	 * 从左向右向右滚动
	 */
	public final static int SCROLL_ORIENTATION_LEFT_TO_RIGHT = 0x01;
	
	@IntDef({SCROLL_ORIENTATION_RIGHT_TO_LEFT,SCROLL_ORIENTATION_LEFT_TO_RIGHT})
	public @interface SCROLL_ORIENTATION{};
	
	@SCROLL_ORIENTATION
	private int mScrollOrientation = SCROLL_ORIENTATION_RIGHT_TO_LEFT;
	
	private final static int LOOP_DEFAULT_TIME = 3000;
	
	private int mLoopTime = LOOP_DEFAULT_TIME;
	
	private LoopRunnable mLoopRunnable;
	
	private PagerAdapter mOutterPagerAdapter;
	
	private InnerPagerAdapter mInnerPagerAdapter;
	
	private InnerDataSetObserver mInnerDataSetObserver;
	
	private OnPageChangeListener mInnerOnPageChangeListener;
	
	private ArrayList<OnPageChangeListener> mOnPageChangeListeners;
	
	private int mCurrentItem;
	
	private long mDelaySendTime;
	
	private long mMovementDownTime;
	
	private boolean isLooping = true;
	
	private int mAutoIncrease = DEFAULT_AUTO_INCREASE;
	
	private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };


	private ViewPagerScroller mViewPagerScroller;
	public AutoScrollViewPager(Context context) {
		this(context,null);
	}

	public AutoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		boolean clipToPadding = attrs.getAttributeBooleanValue(android.R.attr.clipToPadding, true);
		if (clipToPadding) {
			// 需要更改左右了
		}
		mLoopRunnable = new LoopRunnable();
		mInnerPagerAdapter = new InnerPagerAdapter();
		mInnerDataSetObserver = new InnerDataSetObserver();
		mInnerOnPageChangeListener = new InnerOnPageChangeListener();
		mOnPageChangeListeners = new ArrayList<OnPageChangeListener>();
		
		try {  
            Field field = ViewPager.class.getDeclaredField("mScroller");  
            field.setAccessible(true);  
            mViewPagerScroller = new ViewPagerScroller(this.getContext(), sInterpolator);  
            field.set(this, mViewPagerScroller);  
            mViewPagerScroller.setDuration(DEFAULT_SCROLL_TIME);  
        } catch (NoSuchFieldException e) {  
            e.printStackTrace();  
        } catch (IllegalAccessException e) {  
            e.printStackTrace();  
        }  
		int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PAGE_MARGIN, getResources().getDisplayMetrics());
		setPageMargin(marginPixels);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		super.addOnPageChangeListener(mInnerOnPageChangeListener);
		removeCallbacks(mLoopRunnable);
		mDelaySendTime = System.currentTimeMillis();
		postDelayed(mLoopRunnable, mLoopTime);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		super.removeOnPageChangeListener(mInnerOnPageChangeListener);
		removeCallbacks(mLoopRunnable);
	}

	@Override
	public void setAdapter(PagerAdapter pagerAdapter) {
		mOutterPagerAdapter = pagerAdapter;
		if (mOutterPagerAdapter == null) return;
		mOutterPagerAdapter.registerDataSetObserver(mInnerDataSetObserver);
		super.setAdapter(mInnerPagerAdapter);
		setCurrentItem(mAutoIncrease, false);
	}
	
	@Override
	public void addOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListeners.add(listener);
	}
	
	@Override
	public void removeOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListeners.remove(listener);
	}
	
	/**
	 * 设置滚动的方向
	 * @param orientation 滚动方向
	 * <p>{@link #SCROLL_ORIENTATION_RIGHT_TO_LEFT} 从右向左滚动</p>
	 * <p>{@link #SCROLL_ORIENTATION_LEFT_TO_RIGHT} 从左向右滚动</p>
	 */
	public void setScrollOrientation(@SCROLL_ORIENTATION int orientation) {
		mScrollOrientation = orientation;
	}
	
	public void start() {
		isLooping = true;
		removeCallbacks(mLoopRunnable);
		mDelaySendTime = System.currentTimeMillis();
		postDelayed(mLoopRunnable, mLoopTime);
	}
	
	public void stop() {
		isLooping = false;
		removeCallbacks(mLoopRunnable);
	}
	
	/**
	 * 设置自动滚动的时间间隔
	 * @param time 循环时间
	 */
	public void setLoopTime(int time) {
		mLoopTime = time;
	}
	
	/**
	 * 在原始数据左右两端追加的数据个数，用来实现平滑滚动和不出现视觉空白
	 * @param count 个数
	 */
	public void setAutoIncreaseCount(int count) {
		mAutoIncrease = count;
		mInnerPagerAdapter.notifyDataSetChanged();
	}
	
	private void moveToNext() {
		mDelaySendTime = System.currentTimeMillis();
		postDelayed(mLoopRunnable, mLoopTime);
		int count = mInnerPagerAdapter.getCount();
		if (!isLooping || count == 0) return;
		int currentItem = super.getCurrentItem();
		setCurrentItem(mScrollOrientation == SCROLL_ORIENTATION_RIGHT_TO_LEFT ? ++currentItem : --currentItem,true);
	}
	
	private void checkItemPosition() {
		int currentItem = super.getCurrentItem();
		int itemCount = mInnerPagerAdapter.getCount();
		int targetItem = -1;
		if (currentItem == mAutoIncrease -1) {
			targetItem = itemCount - 1 -mAutoIncrease;
		}else if (currentItem == itemCount - mAutoIncrease) {
			targetItem = mAutoIncrease;
		}
		if (targetItem >= 0) {
			mViewPagerScroller.setDuration(0);
			setCurrentItem(targetItem, true);
			mCurrentItem = targetItem;
			mViewPagerScroller.setDuration(DEFAULT_SCROLL_TIME);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isLooping) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				mMovementDownTime = System.currentTimeMillis();
				removeCallbacks(mLoopRunnable);
			}else if (ev.getAction() == MotionEvent.ACTION_UP 
					|| ev.getAction() == MotionEvent.ACTION_CANCEL) {
				long newDelay = (mMovementDownTime - mDelaySendTime) % mLoopTime;
				postDelayed(mLoopRunnable, newDelay);
			}
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	* @Author peerless2012
	* @Email  peerless2012@126.com
	* @HomePage http://peerless2012.github.io
	* @DateTime 2016年5月26日 下午3:03:27
	* @Version V1.0
	* @Description: 循环的Runnable 
	*/
	class LoopRunnable implements Runnable{

		@Override
		public void run() {
			moveToNext();
		}
	}
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 上午11:23:26
	* @Version V1.0
	* @Description: 内部的ViewPager适配器，用来实现数据的转换。
	*/
	class InnerPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return (mOutterPagerAdapter == null || mOutterPagerAdapter.getCount() < 1) ? 
					0 : mOutterPagerAdapter.getCount() + (mAutoIncrease << 1);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return mOutterPagerAdapter.isViewFromObject(view, object);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			mOutterPagerAdapter.destroyItem(container, getRealPosition(position), object);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return mOutterPagerAdapter.instantiateItem(container, getRealPosition(position));
		}
		
		/**
		 * 通过内部的位置获取真实位置
		 * @param position ViewPager回调的Position
		 * @return 真实的Position
		 */
		public int getRealPosition(int position) {
			if (position < mAutoIncrease) {
				return getCount() - (mAutoIncrease << 1) - mAutoIncrease + position; 
			}else if (position >  getCount() - mAutoIncrease - 1) {
				return position - (getCount() - mAutoIncrease);
			}else {
				return position - mAutoIncrease;
			}
		}
	}
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 上午10:39:51
	* @Version V1.0
	* @Description: 数据变更观察者回调
	*/
	class InnerDataSetObserver extends DataSetObserver{

		@Override
		public void onChanged() {
			super.onChanged();
			mInnerPagerAdapter.notifyDataSetChanged();
			setCurrentItem(mAutoIncrease, false);
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			mInnerPagerAdapter.notifyDataSetChanged();
			setCurrentItem(mAutoIncrease, false);
		}
		
	}
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 上午11:23:09
	* @Version V1.0
	* @Description: ViewPager滚动监听
	*/
	class InnerOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			//如果设置条目的时候，不选择平滑滚动则只会调用此放法一次，其他方法都不回调用。
			int realPosition =mInnerPagerAdapter.getRealPosition(position);
			Log.i("AutoScrollViewPager", "onPageScrolled positon = " + position +"   realPosition = "+realPosition +"   positionOffset = " + positionOffset+"   positionOffsetPixels = "+positionOffsetPixels);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageSelected(int position) {
			int realPosition =mInnerPagerAdapter.getRealPosition(position);
			mCurrentItem = realPosition;
			Log.i("AutoScrollViewPager", "onPageSelected positon = " + realPosition);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageSelected(realPosition);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			Log.i("AutoScrollViewPager", "onPageScrollStateChanged onPageScrollStateChanged = " + state);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageScrollStateChanged(state);
			}
			
			if (state == SCROLL_STATE_IDLE) {
				Log.i("AutoScrollViewPager", "onPageScrollStateChanged onPageScrollStateChanged = 空闲");
				checkItemPosition();
			}
		}
	}
	
	@Override
	public int getCurrentItem() {
		return mCurrentItem;
	}
	
	public class ViewPagerScroller extends Scroller {  
	    private int mDuration;  
	  
	    public ViewPagerScroller(Context context) {  
	        super(context);  
	    }  
	  
	    public ViewPagerScroller(Context context, Interpolator interpolator) {  
	        super(context, interpolator);  
	    }  
	  
	    public void setDuration(int mDuration) {  
	        this.mDuration = mDuration;  
	    }  
	  
	    @Override  
	    public void startScroll(int startX, int startY, int dx, int dy) {  
	        super.startScroll(startX, startY, dx, dy, this.mDuration);  
	    }  
	  
	    @Override  
	    public void startScroll(int startX, int startY, int dx, int dy, int duration) {  
	        super.startScroll(startX, startY, dx, dy, this.mDuration);  
	    }  
	  
	}
}
