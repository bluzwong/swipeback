Swipe Back
============
高仿知乎、微信的Activity滑动返回库。

  ![1](./swipe.gif)

 * Activity不需要继承基Activity，最大程度减少侵入
 * Activity不需要使用theme
 * 支持视觉差效果，背景阴影效果，触摸边界、全屏返回

[下载 Demo][1]
用法
--------
__0. 使用helper的方法来启动需要支持滑动返回的activity，该方法包含多个重载可供设置效果__
```java
SwipeBackActivityHelper.startSwipeActivity(this, intent, true, true);
```
__1. 在需要支持滑动返回的activity中，设置helper，可根据需要设置效果__
```java
SwipeBackActivityHelper helper = new SwipeBackActivityHelper();
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ...
    helper.setEdgeMode(true)
            .setParallaxMode(true)
            .setParallaxRatio(3)
            .setNeedBackgroundShadow(true)
            .init(this);
    // ...
}
```
__2. 为了更好的效果，重写返回键按下，显示一个滑动返回的动画__
```java
@Override
public void onBackPressed() {
    helper.finish();
}
```
__3. 基本搞定。但是在一些情况下，如果activity还包含了其他可能造成滑动冲突的组件，比如ViewPager，还需要对触摸事件另行处理。
触摸的处理很复杂，但是不用害怕，已经提供了方法来解决冲突，以ViewPager为例:__
```java
viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position != 0) {
            /// viewPager当前显示不是首页，那么由viewPager来处理触摸，屏蔽掉滑动返回
            helper.disableSwipeBack();
        } else {
            /// viewPager当前显示的是首页，那么由swipe back来提供滑动返回的效果
            helper.enableSwipeBack();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
});
```
依赖
--------
Gradle:
```groovy
compile 'com.github.bluzwong:swipeback:0.1.1'
```

[1]: https://github.com/bluzwong/swipeback/releases/download/0.1.1/demo.apk