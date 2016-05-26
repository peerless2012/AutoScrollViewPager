# AutoScrollViewPager
自动滚动的无限轮播ViewPager

## 效果
![截图](https://raw.githubusercontent.com/peerless2012/AutoScrollViewPager/master/ScreenShots/ScreenGif.gif)

## 已经实现的功能
1. 无限轮播。
2. 手触摸时暂停计时。
3. 增加过渡动画的时长。

## 原理

### 数据结构处理

#### 1.普通ViewPager的数据结构
![普通效果](file:///D:/develop/git/AutoScrollViewPager/ScreenShots/img_normal.png)

我们一般使用`ViewPager`的时候，需要继承`ViewPager`的数据适配器`PagerAdapter`，然后重写以下四个方法：

* __getCount()__ 返回数据集合个数，这样ViewPager就知道总共有多少个Pager页面。
* __isViewFromObject(View view, Object object)__ 返回传入的View和Object是否是同一个关联对象（因为我们一般在`instantiateItem(ViewGroup container, int position)`中add是View，返回的也是View，所以一般就判断 `view ==  object`）。
* __destroyItem(ViewGroup container, int position, Object object)__ 当View要被销毁的时候需要我们手动把View冲ViewPager上移除出去。
* __instantiateItem(ViewGroup container, int position)__ 根据传入的Position，实例化View，然后 __添加到ViewGroup上__（这个千万不能忘记，否则会出现View无法显示的问题）并返回。

#### 2.自定义ViewPager内部适配器数据结构
![普通效果](file:///D:/develop/git/AutoScrollViewPager/ScreenShots/pager_inner.png)
 
AutoScrollViewPager 内部会使用自己的数据适配器，在用户设置的适配器的数据基础上进行处理。

* 在原有数据集合首部增加原有数据集合的尾元素。
* 在原有数据集合尾部增加原有数据集合的头元素。

所以一般情况下，内部数据适配器的数目= 用户适配器数目 + 2。

### 逻辑处理

#### ViewPager回调中Position的处理
从上述数据元素处理可以看出，内部数据适配器比用户适配器个数大2，这就导致ViewPager在回调的时候会出现Position对应不上的问题，因此需要在内部数据适配器中对Position进行转换：
	
	/**
	 * 通过内部的位置获取真实位置
	 * @param position ViewPager回调的Position
	 * @return 真实的Position
	 */
	public int getRealPosition(int position) {
		if (position == 0) {
			return getCount() -1 -2; 
		}else if (position ==  getCount() -1) {
			return 0;
		}else {
			return position -1;
		}
	}



## 感谢
感谢 [王徐杰](http://wangxujie.github.io/) 给出的思路，如果有更原始的作者，可联系我。

## 关于
Author peerless2012

Email  [peerless2012@126.con](mailto:peerless2012@126.con)

Blog   [https://peerless2012.github.io](https://peerless2012.github.io)