package com.ottmatt.gesturecalculator;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetectorCompat mDetector;
    View.OnTouchListener mListener;
    TextView mTextViewer;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetector = new GestureDetectorCompat(this, new GestureListener());
        mListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDetector.onTouchEvent(motionEvent);
            }
        };
        if (savedInstanceState == null) {
            final TypedArray button_digits = getResources().obtainTypedArray(R.array.button_digits);
            for (int i = 0; i < button_digits.length(); i++) {
                findViewById(button_digits.getResourceId(i,0)).setOnClickListener(this);
                findViewById(button_digits.getResourceId(i,0)).setOnTouchListener(mListener);
            }
            button_digits.recycle();
            mTextViewer = (TextView)findViewById(R.id.text_viewer);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void makeToast(String toastText) {
        mTextViewer.setText(toastText);
    }

    public void onClick(View v) {
        String input = ((Button) v).getText().toString();
        makeToast(input);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "Gestures";

        @Override public boolean onFling(MotionEvent e1, MotionEvent e2,
                                         float velocityX, float velocityY) {

            try {
                Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
                if (Math.abs(e1.getY() - e2.getY()) <= SWIPE_MAX_OFF_PATH) {
                    // right to left swipe
                    if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        makeToast("-");
                    }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        makeToast("+");
                    }
                } else {
                    // up to down swipe
                    if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        makeToast("X");
                    }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        makeToast("/");
                    }
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
}
