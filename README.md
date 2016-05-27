# AutoScrollViewPager
自动滚动的无限轮播ViewPager

## 0x00 实际效果
![截图](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/ScreenGif.gif)

## 0x01 主要实现的功能
1. 无限轮播。
2. 手触摸时暂停计时。
3. 增加过渡动画的时长。
4. 一屏显示多于一个Pager。
5. Pager切换动画。

## 0x02 原理

### 1. 数据结构处理

#### 1.1 普通ViewPager的数据结构
![普通效果](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/img_normal.png)

我们一般使用`ViewPager`的时候，需要继承`ViewPager`的数据适配器`PagerAdapter`，然后重写以下四个方法：

* __getCount()__ 返回数据集合个数，这样ViewPager就知道总共有多少个Pager页面。
* __isViewFromObject(View view, Object object)__ 返回传入的View和Object是否是同一个关联对象（因为我们一般在`instantiateItem(ViewGroup container, int position)`中add是View，返回的也是View，所以一般就判断 `view ==  object`）。
* __destroyItem(ViewGroup container, int position, Object object)__ 当View要被销毁的时候需要我们手动把View冲ViewPager上移除出去。
* __instantiateItem(ViewGroup container, int position)__ 根据传入的Position，实例化View，然后 __添加到ViewGroup上__（这个千万不能忘记，否则会出现View无法显示的问题）并返回。

#### 1.2 自定义ViewPager内部适配器数据结构
左右各追加一个元素的时候：

![普通效果+1](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/pager_inner_off1.png)

左右各追加2个元素的时候：

![普通效果+2](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/pager_inner_off2.png)

左右各追加m个元素的时候：

![普通效果+m](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/pager_inner_offm.png)
 
AutoScrollViewPager 内部会使用自己的数据适配器，在用户设置的适配器的数据基础上进行处理。

* 在原有数据集合首部增加原有数据集合的尾元素：n-m ... n-1。
* 在原有数据集合尾部增加原有数据集合的头元素：0 ... m-1。

所以一般情况下，内部数据适配器的数目= 用户适配器数目 + m * 2。

### 2. 逻辑处理

#### 2.1 ViewPager回调中Position的处理
从上述数据元素处理可以看出，内部数据适配器比用户适配器个数大2，这就导致ViewPager在回调的时候会出现Position对应不上的问题，因此需要在内部数据适配器中对Position进行转换：
	

	@Override
	public int getCount() {
		return (mOutterPagerAdapter == null || mOutterPagerAdapter.getCount() < 1) ? 
				0 : mOutterPagerAdapter.getCount() + (mAutoIncrease << 1);
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

### 3. 在适当的位置重新设置ViewPager位置

判断当前位置是原始数据的前一个或者原始数据的后一个，则以非动画的方式切换到另一侧的位置上。

比如：左右追加1个元素来说，

* 如果当前位置是左侧第0个的时候，则让ViewPager以非动画的方式，快速切换到倒数第二个的位置。
* 如果当前位置是最后一个位置，则让ViewPager以非动画的方式快速切换到第1个位置。

	这样，就能产生类似无限轮播的效果了。

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

### 3. ViewPager显示多于一个Pager
这里面主要用到了`android:clipToPadding="false"`这个属性，他的意思是内容在Padding区域照样绘制，并不裁剪。然后再设置一个对于左右两侧的Padding值即可。

别忘了设置缓存的页面>=一屏可见Pager个数。

布局文件预览：

	<com.peerless2012.autoscrollviewpager.AutoScrollViewPager
		android:clipToPadding="false"
	    android:id="@+id/auto_scroll_viewpager"
	    android:layout_width="match_parent"
	    android:layout_height="200dp"
	    android:paddingLeft="60dp"
	    android:paddingRight="60dp"/>


### 4. 关于定时切换
在`onAttachedToWindow()`的时候开始循环，在`onDetachedFromWindow()`结束循环。

### 5. 触摸的时候暂停循环计时
记录开始循环的时间和按下的时间，在按下的时候取消循环，这样已经过去的时间 = 按下时间 - 开始循环时间。

当抬起的时候，延迟执行切换的时间 = 延迟时间 - 按下的时候已经过去的时间。

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

## 0x03 感谢
感谢 [王徐杰](http://wangxujie.github.io/) 给出的思路，如果有更原始的作者，可联系我。

## 0x04 关于
Author peerless2012

Email  [peerless2012@126.con](mailto:peerless2012@126.con)

Blog   [https://peerless2012.github.io](https://peerless2012.github.io)