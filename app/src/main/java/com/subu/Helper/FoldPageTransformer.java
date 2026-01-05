package com.subu.Helper;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FoldPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        page.setCameraDistance(5000); //20000 original

        if (position < -1){
            page.setAlpha(0);
        } else if (position <= 0) {
            page.setAlpha(1);
            page.setPivotX(page.getWidth());
            page.setRotationY(45 * position); //90 original
        } else if (position<= 1) {
            page.setAlpha(1);
            page.setPivotX(0);
            page.setRotationY(-45 * position); //90 original
        } else {
            page.setAlpha(0);
        }
    }
}
