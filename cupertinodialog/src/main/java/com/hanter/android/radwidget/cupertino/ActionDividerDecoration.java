package com.hanter.android.radwidget.cupertino;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 类名：DividerItemDecoration <br/>
 * 描述：通用的RecyclerView LinearLayoutManager的Divider <br/>
 * 创建时间：2017/4/10 10:13
 *
 * @author wangmingshuo
 * @version 1.0
 */
public class ActionDividerDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "ActionSheetDividerDecor";

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL = LinearLayoutManager.VERTICAL;

    private Rect mBounds = new Rect();

    private Drawable mDivider;

    private Drawable mPressedDivider;

    private int mWidth;

    private int mHeight;

    private int mOrientation;

    private boolean mHeaderDividersEnabled;

    private boolean mFooterDividersEnabled;

    public ActionDividerDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        mPressedDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * 自定义dividerDrawable
     */
    public ActionDividerDecoration(Context context, int orientation, Drawable divider, Drawable pressedDivider) {
        mDivider = divider;
        mPressedDivider = pressedDivider;
        setOrientation(orientation);
    }

    public ActionDividerDecoration(Context context, int orientation, @DrawableRes int dividerRes,
                                   @DrawableRes int pressedDividerRes) {
        mDivider = context.getResources().getDrawable(dividerRes);
        mPressedDivider = context.getResources().getDrawable(pressedDividerRes);
        setOrientation(orientation);
    }

    public void setDividerWidth(int width) {
        this.mWidth = width;
    }

    public void setDividerHeight(int height) {
        this.mHeight = height;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        if (mOrientation == VERTICAL) {
            drawVertical(c, parent);
        } else if (mOrientation == HORIZONTAL) {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        Log.d(TAG, "drawVertical");

        c.save();

        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            c.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final CupertinoDialogActionButton child = (CupertinoDialogActionButton) parent.getChildAt(i);
            final View nextChild = parent.getChildAt(i + 1);

            Log.d(TAG, "child: " + i + ", isPressed: " + child.isActionDown());

            parent.getDecoratedBoundsWithMargins(child, mBounds);

            // 底部
            int bottom;
            int top;
            if (!isLast(parent, child) || mFooterDividersEnabled) {
                bottom = mBounds.bottom + Math.round(child.getTranslationY());
                top = bottom - getDividerHeight();

                if (child.isActionDown() || (nextChild != null && ((CupertinoDialogActionButton) nextChild).isActionDown())) {
                    mPressedDivider.setBounds(left, top, right, bottom);
                    mPressedDivider.draw(c);
                } else {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }

            // 上部
            if (isFirst(parent, child) && mHeaderDividersEnabled) {
                top = mBounds.top + Math.round(child.getTranslationY());
                bottom = top + getDividerHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        c.restore();
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        Log.d(TAG, "drawVertical");

        c.save();

        final int top;
        final int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            c.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final CupertinoDialogActionButton child = (CupertinoDialogActionButton) parent.getChildAt(i);
            final View nextChild = parent.getChildAt(i + 1);

            Log.d(TAG, "child: " + i + ", isPressed: " + child.isActionDown());

            parent.getDecoratedBoundsWithMargins(child, mBounds);

            int right;
            int left;

            // 绘制 Footer Divider 部分
            if (!isLast(parent, child) || mFooterDividersEnabled) {
                right = mBounds.right + Math.round(child.getTranslationX());
                left = right - getDividerWidth();

                if (child.isActionDown() || (nextChild != null && ((CupertinoDialogActionButton) nextChild).isActionDown())) {
                    mPressedDivider.setBounds(left, top, right, bottom);
                    mPressedDivider.draw(c);
                } else {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }

            // 绘制 Header Divider 部分
            if (isFirst(parent, child) && mHeaderDividersEnabled) {
                left = mBounds.left + Math.round(child.getTranslationX());
                right = left + getDividerWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        c.restore();
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            int top, bottom;

            if (isFirst(parent, view) && isLast(parent, view)) {
                top = mHeaderDividersEnabled ? getDividerHeight() : 0;
                bottom = mFooterDividersEnabled ? getDividerHeight() : 0;
            } else if (isFirst(parent, view)) {
                top = mHeaderDividersEnabled ? getDividerHeight() : 0;
                bottom = getDividerHeight();
            } else if (isLast(parent, view)) {
                top = 0;
                bottom = mFooterDividersEnabled ? getDividerHeight() : 0;
            } else {
                top = 0;
                bottom = getDividerHeight();
            }

            outRect.set(0, top, 0, bottom);

        } else if (mOrientation == HORIZONTAL) {
            int left, right;

            if (isFirst(parent, view) && isLast(parent, view)) {
                left = mHeaderDividersEnabled ? getDividerWidth() : 0;
                right = mFooterDividersEnabled ? getDividerWidth() : 0;
            } else if (isFirst(parent, view)) {
                left = mHeaderDividersEnabled ? getDividerWidth() : 0;
                right = getDividerWidth();
            } else if (isLast(parent, view)) {
                left = 0;
                right = mFooterDividersEnabled ? getDividerWidth() : 0;
            } else {
                left = 0;
                right = getDividerWidth();
            }

            outRect.set(left, 0, right, 0);
        }
    }

    private boolean isLast(RecyclerView parent, View view) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null)
            return false;

        int count = layoutManager.getItemCount();
        int position = layoutManager.getPosition(view);
        return position == (count - 1);
    }

    private boolean isFirst(RecyclerView parent, View view) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null) {
            return false;
        }

        return layoutManager.getPosition(view) == 0;
    }

    public int getDividerWidth() {
        return mWidth > 0 ? mWidth : mDivider.getIntrinsicWidth();
    }

    public int getDividerHeight() {
        return mHeight > 0 ? mHeight : mDivider.getIntrinsicHeight();
    }

    public boolean isHeaderDividersEnabled() {
        return mHeaderDividersEnabled;
    }

    public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
        mHeaderDividersEnabled = headerDividersEnabled;
    }

    public boolean isFooterDividersEnabled() {
        return mFooterDividersEnabled;
    }

    public void setFooterDividersEnabled(boolean footerDividersEnabled) {
        mFooterDividersEnabled = footerDividersEnabled;
    }
}