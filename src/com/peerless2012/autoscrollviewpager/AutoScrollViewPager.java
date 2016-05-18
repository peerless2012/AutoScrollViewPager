package com.peerless2012.autoscrollviewpager;

import java.util.ArrayList;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.IntDef;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
* @Author peerless2012
* @Email peerless2012@126.com
* @DateTime 2016年5月18日 上午12:25:17
* @Version V1.0
* @Description: 自动滚动的无限轮播的ViewPager,注意：本自定义控件不适于用到带有指示器等。
*/
public class AutoScrollViewPager extends ViewPager {
	
	/**
	 * 向左滚动
	 */
	public final static int SCROLL_ORIENTATION_LEFT = 0x00;
	
	/**
	 * 向右滚动
	 */
	public final static int SCROLL_ORIENTATION_RIGHT = 0x01;
	
	@IntDef({SCROLL_ORIENTATION_LEFT,SCROLL_ORIENTATION_RIGHT})
	public @interface SCROLL_ORIENTATION{};
	
	@SCROLL_ORIENTATION
	private int mScrollOrientation = SCROLL_ORIENTATION_LEFT;
	
	private final static int LOOP_DEFAULT_TIME = 3000;
	
	private int mLoopTime = LOOP_DEFAULT_TIME;
	
	private boolean mIsRunning = false;
	
	private LoopRunnable mLoopRunnable;
	
	private PagerAdapter mOutterPagerAdapter;
	
	private InnerPagerAdapter mInnerPagerAdapter;
	
	private InnerDataSetObserver mInnerDataSetObserver;
	
	private OnPageChangeListener mInnerOnPageChangeListener;
	
	private ArrayList<OnPageChangeListener> mOnPageChangeListeners;
	
	public AutoScrollViewPager(Context context) {
		this(context,null);
	}

	public AutoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mLoopRunnable = new LoopRunnable();
		mInnerPagerAdapter = new InnerPagerAdapter();
		mInnerDataSetObserver = new InnerDataSetObserver();
		mOnPageChangeListeners = new ArrayList<OnPageChangeListener>();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		super.addOnPageChangeListener(mInnerOnPageChangeListener);
		removeCallbacks(mLoopRunnable);
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
	 * <p>{@link #SCROLL_ORIENTATION_LEFT} 从右向左滚动</p>
	 * <p>{@link #SCROLL_ORIENTATION_RIGHT} 从左向右滚动</p>
	 */
	public void setScrollOrientation(@SCROLL_ORIENTATION int orientation) {
		
	}
	
	public void start() {
		mIsRunning = true;
		removeCallbacks(mLoopRunnable);
		postDelayed(mLoopRunnable, mLoopTime);
	}
	
	public void stop() {
		mIsRunning = false;
		removeCallbacks(mLoopRunnable);
	}
	
	/**
	 * 设置自动滚动的时间间隔
	 * @param time 循环时间
	 */
	public void setLoopTime(int time) {
		mLoopTime = time;
	}
	
	private void moveToNext() {
		postDelayed(mLoopRunnable, mLoopTime);
		int count = mInnerPagerAdapter.getCount();
		if (!mIsRunning || count == 0) return;
		int currentItem = getCurrentItem();
//		setCurrentItem(mScrollOrientation == SCROLL_ORIENTATION_LEFT ? ++currentItem : --currentItem, false);
	}
	
	class LoopRunnable implements Runnable{

		@Override
		public void run() {
			Log.i("AutoScrollViewPager", "执行切换");
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
					0 : mOutterPagerAdapter.getCount() + 2;
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
		 * @param position
		 * @return
		 */
		public int getRealPosition(int position) {
			if (position == 0) {
				return getCount() -3; 
			}else if (position ==  getCount() -1) {
				return 0;
			}else {
				return position -1;
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
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			mInnerPagerAdapter.notifyDataSetChanged();
		}
		
	}
	
	/**
	* @Author peerless2012
	* @Email peerless2012@126.com
	* @DateTime 2016年5月18日 上午11:23:09
	* @Version V1.0
	* @Description: ViewPager滚动监听
	*/
	class InnerPagerScroll implements OnPageChangeListener{

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			Log.i("AutoScrollViewPager", "onPageScrolled positon = " + position +"   positionOffset = " + positionOffset+"   positionOffsetPixels = "+positionOffsetPixels);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageScrolled(mInnerPagerAdapter.getRealPosition(position), positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageSelected(int position) {
			Log.i("AutoScrollViewPager", "onPageSelected positon = " + position);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageSelected(mInnerPagerAdapter.getRealPosition(position));
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			Log.i("AutoScrollViewPager", "onPageSelected onPageScrollStateChanged = " + state);
			for (OnPageChangeListener l : mOnPageChangeListeners) {
				l.onPageScrollStateChanged(state);
			}
			
			if (state == SCROLL_STATE_IDLE) {
				Log.i("AutoScrollViewPager", "onPageSelected onPageScrollStateChanged = 空闲");
			}
		}
		
	}
}