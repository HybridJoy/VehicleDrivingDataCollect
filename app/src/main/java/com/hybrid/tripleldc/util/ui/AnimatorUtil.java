package com.hybrid.tripleldc.util.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;


import androidx.annotation.ColorRes;

import com.hybrid.tripleldc.global.App;

/**
 * Author: Joy
 * Created Time: 2021/7/9-11:41
 * <p>
 * Tips: (if you changed this file, leave your footprint
 * like this: modified by xxx in 2021/7/9 )
 * <p>
 * Describe:
 */
public class AnimatorUtil {

    private static Context context = App.getInstance().getApplicationContext();
    private static Resources resources = context.getResources();

    /**
     * 颜色渐变动画
     *
     * @param target       需要渲染的组件
     * @param propertyName 属性名
     * @param startColor   当前颜色
     * @param endColor     目标颜色
     * @param duration     时间
     */
    public static void colorTransit(Object target, String propertyName, @ColorRes int startColor,  @ColorRes int endColor, long duration) {
        ValueAnimator animator = ObjectAnimator.ofInt(target, propertyName, resources.getColor(startColor, null), resources.getColor(endColor, null));
        animator.setDuration(duration);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatCount(0);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }

    public static Animation generateScaleAnimation(float fromXScale, float toXScale, float fromYScale, float toYScale, int repeatCount, long duration) {
        Animation animation = new ScaleAnimation(fromXScale, toXScale, fromYScale, toYScale,  Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatCount(repeatCount);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setDuration(duration);
        animation.setFillAfter(false);
        return animation;
    }

    public static Animation generateTranslateAnimation(float fromXValue, float toXValue, float fromYValue, float toYValue, int repeatCount, long duration) {
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, fromXValue, Animation.RELATIVE_TO_SELF,
                toXValue, Animation.RELATIVE_TO_PARENT, fromYValue,
                Animation.RELATIVE_TO_PARENT, toYValue);
        animation.setRepeatCount(repeatCount);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        return animation;
    }

    public static Animation generateAlphaAnimation(float fromAlpha, float toAlpha, long duration) {
        AlphaAnimation animation = new AlphaAnimation(fromAlpha, toAlpha);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        return animation;
    }
}
