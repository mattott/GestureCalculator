package com.ottmatt.gesturecalculator;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int DIGIT = 0;
    private static final int PARENTHESES = 1;
    private static final int EXPONENT = 2;
    private static final int MULT_DIV = 3;
    private static final int ADD_SUB = 4;
    private static int TOUCH_SLOP;
    private GestureDetectorCompat mDetector;
    private View.OnTouchListener mDigitListener;
    private GestureDetectorCompat mViewerDetector;
    private View.OnTouchListener mViewerListener;
    private TextView mTextViewer;
    private List<Hashtable<String, Integer>> mEquation;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewConfiguration vc = ViewConfiguration.get(this);
        TOUCH_SLOP = vc.getScaledTouchSlop();

        mDetector = new GestureDetectorCompat(this, new GestureListener());
        mDigitListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDetector.onTouchEvent(motionEvent);
            }
        };
        mViewerDetector = new GestureDetectorCompat(this, new ViewerGestureListener());
        mViewerListener = new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mViewerDetector.onTouchEvent(motionEvent);
            }
        };

        //noinspection deprecation
        mEquation = (List<Hashtable<String, Integer>>)getLastNonConfigurationInstance();
        if (mEquation == null)
            mEquation = Collections.synchronizedList(new ArrayList<Hashtable<String, Integer>>());

        final TypedArray button_digits = getResources().obtainTypedArray(R.array.button_digits);
        if (button_digits != null) {
            for (int i = 0; i < button_digits.length(); i++) {
                findViewById(button_digits.getResourceId(i,0)).setOnClickListener(this);
                findViewById(button_digits.getResourceId(i,0)).setOnTouchListener(mDigitListener);
            }
            button_digits.recycle();
        }
        mTextViewer = (TextView)findViewById(R.id.text_viewer);
        mTextViewer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                // Calculate value on click.
            }
        });
        mTextViewer.setOnTouchListener(mViewerListener);
        refreshViewer();
    }

    @SuppressWarnings("deprecation")
    @Override public List<Hashtable<String, Integer>> onRetainNonConfigurationInstance() {
        return mEquation;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    void push(String value, int order) {
        Hashtable<String, Integer> math = new Hashtable<String, Integer>();
        math.put(value, order);
        // If the last value was an operator and this value is an operator
        // then replace the old operator with the new one
//        if (!mEquation.get(mEquation.size()-1).containsValue(DIGIT) &&
            //    order != DIGIT)
           // mEquation.set(mEquation.size()-1, math);
       // else
            mEquation.add(math);
    }

    void pop() {
        if (!mEquation.isEmpty())
            mEquation.remove(mEquation.size() - 1);
    }

    String stackToString() {
        StringBuilder sb = new StringBuilder();
        for (Hashtable<String, Integer> ht: mEquation) {
            for (String key: ht.keySet()) {
                sb.append(key);
            }
        }
        return sb.toString();
    }

    void refreshViewer() {
        mTextViewer.setText(stackToString());
    }

    void makeToast(String toastText) {
        mTextViewer.setText(toastText);
    }

    void clearViewer() {
        mEquation.clear();
    }

    public void onClick(View v) {
        String input = "";
        if (v instanceof Button) {
            CharSequence s = ((Button)v).getText();
            if (s != null && s.length() > 0)
                input = s.toString();
        }

        push(input, DIGIT);
        refreshViewer();
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "Gestures";

        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                         float distanceX, float distanceY) {
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();

            try {
                Log.d(TAG, "onScroll: " + e1.toString()+e2.toString());
                if (Math.abs(deltaY) <= SWIPE_MAX_OFF_PATH) {
                    if(deltaX > TOUCH_SLOP) {
                        // scroll left
                        push("-", ADD_SUB);
                    } else if (deltaX < -TOUCH_SLOP) {
                        // scroll right
                        push("+", ADD_SUB);
                    }
                } else {
                    if(deltaY > TOUCH_SLOP) {
                        // scroll up
                        push("*", MULT_DIV);
                    } else if (deltaY < -TOUCH_SLOP) {
                        // scroll down
                        push("/", MULT_DIV);
                    }
                }
                refreshViewer();
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }

    class ViewerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "ViewerGestures";

        @Override public boolean onSingleTapUp(MotionEvent e) {
            int x = e.AXIS_X;
            int y = e.AXIS_Y;
            int viewWidth = mTextViewer.getWidth();
            if (x < viewWidth/2)
                pop();
            else
                push("=", DIGIT);
            refreshViewer();
            return false;
        }

        @Override public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                         float distanceX, float distanceY) {
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();

            try {
                Log.d(TAG, "onScroll: " + e1.toString()+e2.toString());
                if (Math.abs(deltaY) <= SWIPE_MAX_OFF_PATH) {
                    if (deltaX > TOUCH_SLOP) {
                        // scroll left
                        //pop();
                    } else if (deltaX < TOUCH_SLOP) {
                        // scroll left
                    }
                } else {
                    if(deltaY > TOUCH_SLOP) {
                        // scroll up
                    } else if (deltaY < TOUCH_SLOP) {
                        // scroll down
                        clearViewer();
                    }
                }
                refreshViewer();
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
}
