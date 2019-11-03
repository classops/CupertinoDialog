package com.hanter.android.radwidget.cupertino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

public class CupertinoDialogActionButton extends AppCompatButton {

    private boolean actionDown;
    private OnActionDownChangeListener listener;


    public interface OnActionDownChangeListener {
        void onActionDownChange(View view, boolean actionDown);
    }

    public CupertinoDialogActionButton(Context context) {
        this(context, null);
    }

    public CupertinoDialogActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CupertinoDialogActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                changeActionDown(true);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                changeActionDown(false);
                break;
        }

        return super.onTouchEvent(event);
    }

    public boolean isActionDown() {
        return actionDown;
    }

    public void changeActionDown(boolean actionDown) {
        if (this.actionDown != actionDown) {
            this.actionDown = actionDown;
            if (listener != null)
                listener.onActionDownChange(this, actionDown);
        }
    }

    public void setOnActionDownChangeListener(OnActionDownChangeListener listener) {
        this.listener = listener;
    }
}
