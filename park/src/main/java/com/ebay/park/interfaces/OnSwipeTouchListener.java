package com.ebay.park.interfaces;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by paula.baudo on 11/24/2016.
 */

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 50;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
                result = true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return true;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onClick(){
    }

}
