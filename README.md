# GoodPagerIndicator

[![Release](https://jitpack.io/v/com.github.GoodRequest/GoodPagerIndicator.svg)](https://jitpack.io/#com.github.GoodRequest/GoodPagerIndicator)

Custom implementation of view pager indicator. Current scroll value can be distributed between more "Dots", not just adjacent ones.

Currently only single visual is supported as well as only ViewPager2 as indicator's target.  Please check following sample to make sure, this is exactly what you need. New behaviors and features will be added later:  
  
<img src="./presentation/sample.gif" alt="Sample">  
 
## Example usage
Library is stored on JitPack, so you can include it in your project using this config:

Root [build.gradle](./build.gradle) 

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

And in project [build.gradle](./app/build.gradle) use

    dependencies {
        implementation 'com.github.GoodRequest:GoodPagerIndicator:0.1.1'
    }

After successful sync, create your view using XML with `androidx.viewpager2.widget.ViewPager2` and the `GoodPagerIndicator`. To see explanation of used attributes, please have a look at [Supported attributes section](#supported-attributes).

    <com.goodrequest.GoodPagerIndicator
      android:id="@+id/indicator"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:indicator_dot_min_size="4dp"
      app:indicator_dot_max_size="12dp"
      app:indicator_dot_spacing="3dp"
      app:indicator_resizing_span="4"
      app:indicator_dot_active_color="@android:color/black"
      app:indicator_dot_inactive_color="@android:color/darker_gray"
      app:indicator_interpolator="accelerate"/>

In your activity, setup the `indicator` with given `view_pager` like this:

    indicator.initWith(view_pager)
 
Any changes to `view_pager` scroll position and its adapter will be reflected to the `indicator`. The pager indicator can also handle scrolling behavior, so you can easily
 
## Supported attributes
List of currently supported [attributes](./goodpagerindicator/src/main/res/values/attrs.xml):  
1. **indicator_dot_min_size** : sets the minimal diameter of dot  
2. **indicator_dot_max_size** : sets the maximal diameter of dot  
3. **indicator_dot_spacing** : minimal spacing between 2 dots (dots however always takes max_size width)  
4. **indicator_resizing_span** : set how many dots are affected by scrolling between pages  
5. **indicator_dot_active_color** : color of dot on active position (accent color by default)  
6. **indicator_dot_inactive_color** : color of dot on inactive position (primary color by default)  
7. **indicator_interpolator** : interpolator to be used for computing dot diameter. Recommended values are `linear` and `accelerate`

## ToDo's
1. Enable turn on/off swipe gestures
2. Enable turn on/off click gestures
3. Resolve in-project ToDo-s (questioning performance)
4. Implement other visuals