package com.peerless2012.autoscrollviewpager.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
* @Author peerless2012
* @Email  peerless2012@126.com
* @HomePage http://peerless2012.github.io
* @DateTime 2016年5月26日 下午3:04:20
* @Version V1.0
* @Description: 透明度渐变
*/
public class AlphaPageTransformer implements ViewPager.PageTransformer {
	private static final float DEFAULT_MIN_ALPHA = 0.6f;
	private float mMinAlpha = DEFAULT_MIN_ALPHA;
	@Override
	public void transformPage(View page, float position) {
		if (position < -1) { // [-Infinity,-1)
			page.setAlpha(mMinAlpha);
		} else if (position <= 1) { // [-1,1]

			if (position < 0) // [0，-1]
			{ // [1,min]
				float factor = mMinAlpha + (1 - mMinAlpha) * (1 + position);
				page.setAlpha(factor);
			} else// [1，0]
			{
				// [min,1]
				float factor = mMinAlpha + (1 - mMinAlpha) * (1 - position);
				page.setAlpha(factor);
			}
		} else { // (1,+Infinity]
			page.setAlpha(mMinAlpha);
		}
	}
}
