package com.peerless2012.autoscrollviewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
* @Author peerless2012
* @Email peerless2012@126.com
* @DateTime 2016年5月18日 上午12:25:17
* @Version V1.0
* @Description: 自动滚动的无限轮播的ViewPager
*/
public class AutoScrollViewPager extends ViewPager {
	
	public AutoScrollViewPager(Context context) {
		super(context);
	}

	public AutoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	
}
