package com.ottmatt.gesturecalculator;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetectorCompat mDetector;
    View.OnTouchListener mDigitListener;
    private GestureDetectorCompat mViewerDetector;
    View.OnTouchListener mViewerListener;
    TextView mTextViewer;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetector = new GestureDetectorCompat(this, new GestureListener(false));
        mDigitListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDetector.onTouchEvent(motionEvent);
            }
        };
        mViewerDetector = new GestureDetectorCompat(this, new GestureListener(true));
        mViewerListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mViewerDetector.onTouchEvent(motionEvent);
            }
        };
        if (savedInstanceState == null) {
            final TypedArray button_digits = getResources().obtainTypedArray(R.array.button_digits);
            for (int i = 0; i < button_digits.length(); i++) {
                findViewById(button_digits.getResourceId(i,0)).setOnClickListener(this);
                findViewById(button_digits.getResourceId(i,0)).setOnTouchListener(mDigitListener);
            }
            button_digits.recycle();
            mTextViewer = (TextView)findViewById(R.id.text_viewer);
            mTextViewer.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    // Calculate value on click.
                }
            });
            mTextViewer.setOnTouchListener(mViewerListener);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void makeToast(String toastText) {
        mTextViewer.setText(toastText);
    }

    public void clearViewer() {
        mTextViewer.setText("");
    }

    public void onClick(View v) {
        String input = ((Button) v).getText().toString();
        makeToast(input);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "Gestures";
        private final boolean mIsViewer;
        public GestureListener(boolean isViewer) {
            mIsViewer = isViewer;
        }

        @Override public boolean onFling(MotionEvent e1, MotionEvent e2,
                                         float velocityX, float velocityY) {

            try {
                Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
                if (Math.abs(e1.getY() - e2.getY()) <= SWIPE_MAX_OFF_PATH) {
                    // right to left swipe
                    if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (mIsViewer)
                            clearViewer();
                        else
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
