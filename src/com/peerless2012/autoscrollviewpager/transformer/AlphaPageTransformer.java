package com.peerless2012.autoscrollviewpager.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

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
