package com.peerless2012.autoscrollviewpager;

import com.peerless2012.autoscrollviewpager.transformer.AlphaPageTransformer;
import com.peerless2012.autoscrollviewpager.transformer.ScaleInTransformer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class MainActivity extends Activity {

	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mViewPager = (ViewPager) findViewById(R.id.auto_scroll_viewpager);
		mViewPager.setAdapter(new AdPagerAdapter());
//		mViewPager.setPageTransformer(true, new AlphaPageTransformer());
		mViewPager.setPageTransformer(true, new ScaleInTransformer());
	}
}
