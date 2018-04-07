package com.medicus.medicus;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by kushbhardwaj on 14/02/18.
 */

public class ExpandingViewPagerTransformer implements ViewPager.PageTransformer {

    public static final float MAX_SCALE = 1.0f;
    public static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(position);

            float scaleFactor = MIN_SCALE
                    + (1-MIN_SCALE) * (position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else if (position <= 0 && position >= -1) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            float scaleFactor = MIN_SCALE
                    + (1-MIN_SCALE) * (position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else if (position <= 1 && position > 0) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
//            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    - (1 - MIN_SCALE) * (position);
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else if(position>1){ // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0.1f);
        }
        view.setBackgroundColor(Color.parseColor("#ffffff"));
//        view.setMinimumWidth(1000);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            view.getParent().requestLayout();
        }

    }
}