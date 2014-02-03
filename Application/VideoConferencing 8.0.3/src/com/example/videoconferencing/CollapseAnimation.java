package com.example.videoconferencing;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
 
public class CollapseAnimation extends Animation implements Animation.AnimationListener {
 
    private View view;
    private static double ANIMATION_DURATION;
    private int LastMargin;
    private int FromMargin;
    private int ToMargin;
    private static int STEP_SIZE=30;
    public CollapseAnimation(View v,int ToMargin, double Duration) {
         
    	this.view = v;
    	FrameLayout.LayoutParams par =(android.widget.FrameLayout.LayoutParams) v.getLayoutParams();
		
        ANIMATION_DURATION = Duration;
    	this.FromMargin = par.leftMargin;
        this.ToMargin = ToMargin;
        setDuration((long) ANIMATION_DURATION);
        setRepeatCount(20);
        setFillAfter(false);
        setInterpolator(new AccelerateInterpolator());
        setAnimationListener(this);
    }
 
    @Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		FrameLayout.LayoutParams par =(android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
        par.leftMargin = par.leftMargin - ToMargin/20;
        view.setLayoutParams(par);
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
			FrameLayout.LayoutParams par =(android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
			LastMargin = par.leftMargin;
	}
 
}