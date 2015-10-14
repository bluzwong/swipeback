Swipe Back
============
A library to add swipe back feature for activity.

  ![1](./swipe.gif)

 * Do not need inherit BaseActivity
 * Do not need theme
 * Support Parallax, Shadow background, Edge to back

[Download Demo][1]
[中文用户请点此处][0]

Usage
--------
__0. Use helper method to start the activity you want swipe back.
This method have some overloads to make parallax, shadow background or edge to back__
```java
SwipeBackActivityHelper.startSwipeActivity(this, intent, true, true);
// or use a builder
SwipeBackActivityHelper.activityBuilder(MainActivity.this)
                        .intent(intent).needParallax(true).needBackgroundShadow(true).startActivity();
```
__1. Setup the activity you want swipe back.__
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
__2. More better, override onBackPressed() to show swipe back animation__
```java
@Override
public void onBackPressed() {
    helper.finish();
}
```
__3. That's all.But if this activity contains another swipe view, it may cause conflict.
Example. Handle ViewPager with swipe back :__
```java
viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position != 0) {
            /// if the current view page is not the first, make 'viewPager' receive touch event.
            helper.disableSwipeBack();
        } else {
            /// the current page return to the first one, make 'swipe back' receive touch event.
            helper.enableSwipeBack();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
});
```
Dependence
--------
Gradle:
```groovy
compile 'com.github.bluzwong:swipeback:0.1.3@aar'
```

[0]: README_ZH.md
[1]: https://github.com/bluzwong/swipeback/releases/download/0.1.1/demo.apk