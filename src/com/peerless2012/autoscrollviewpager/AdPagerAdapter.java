package com.peerless2012.autoscrollviewpager;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
* @Author peerless2012
* @Email peerless2012@126.com
* @DateTime 2016年5月18日 上午12:28:54
* @Version V1.0
* @Description: 广告图片无限轮播
*/
public class AdPagerAdapter extends PagerAdapter {

	private LayoutInflater mLayoutInflater;
	
	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view ==  object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (mLayoutInflater == null) {
			mLayoutInflater = LayoutInflater.from(container.getContext());
		}
		View view = mLayoutInflater.inflate(R.layout.pager, container, false);
		TextView tv = (TextView) view.findViewById(R.id.tv);
		tv.setText("当前索引   "+position);
		container.addView(view);
		return view;
	}
}
