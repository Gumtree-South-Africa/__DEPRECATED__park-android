package com.ebay.park.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by paula.baudo on 1/8/2016.
 */
public class CustomSwipeRefresh extends SwipeRefreshLayout {

    private int mActivePointerId;

    public CustomSwipeRefresh(Context context) {
        super(context);
    }

    public CustomSwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            int pointerCount = MotionEventCompat.getPointerCount(ev);
            int index = MotionEventCompat.getActionIndex(ev);
            mActivePointerId = MotionEventCompat.getPointerId(ev, index);
            index = MotionEventCompat.findPointerIndex(ev,mActivePointerId);
            if (index > -1 && index < pointerCount) {
                super.onInterceptTouchEvent(ev);
            } else {
                return true;
            }
        } else if(ev.getAction() == MotionEventCompat.ACTION_POINTER_DOWN && super.onInterceptTouchEvent(ev)) {
            final int index = MotionEventCompat.getActionIndex(ev);
            mActivePointerId = MotionEventCompat.getPointerId(ev, index);
            return false;
        } else if(ev.getAction() == MotionEventCompat.ACTION_POINTER_UP && super.onInterceptTouchEvent(ev)) {
            onSecondaryPointerUp(ev);
            return false;
        } else if(ev.getAction() == MotionEvent.ACTION_DOWN && super.onInterceptTouchEvent(ev)) {
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }
}
