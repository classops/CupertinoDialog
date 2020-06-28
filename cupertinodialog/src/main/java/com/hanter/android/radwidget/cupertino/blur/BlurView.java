package com.hanter.android.radwidget.cupertino.blur;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hanter.android.radwidget.cupertino.R;

import java.util.Arrays;

/**
 * FrameLayout that blurs its underlying content.
 * Can have children and draw them over blurred background.
 */
public class BlurView extends FrameLayout {

    private static final String TAG = BlurView.class.getSimpleName();

    public static final int NONE = -1;
    public static final int CLEAR = 0;
    public static final int SRC = 1;
    public static final int DST = 2;
    public static final int SRC_OVER = 3;
    public static final int DST_OVER = 4;
    public static final int SRC_IN = 5;
    public static final int DST_IN = 6;
    public static final int SRC_OUT = 7;
    public static final int DST_OUT = 8;
    public static final int SRC_ATOP = 9;
    public static final int DST_ATOP = 10;
    public static final int XOR = 11;
    public static final int ADD = 12;
    public static final int MULTIPLY = 13;
    public static final int SCREEN = 14;

    public static final int OVERLAY = 15;
    public static final int LIGHTEN = 17;
    public static final int DARKEN = 16;


    private Paint imagePaint;
    private Paint roundPaint;
    private Path roundPath;
    private Path roundCornerPath;
    RectF rectF;
    BlurController blurController = new NoOpController();
    private boolean round;
    private boolean topLeftRound;
    private boolean topRightRound;
    private boolean bottomLeftRound;
    private boolean bottomRightRound;
    private float roundCornerRadius;
    private float[] radii = new float[8];

    @ColorInt
    int barrierColor;
    @ColorInt
    int overlayColor;
    int overlayBlendMode;

    public BlurView(Context context) {
        super(context);
        init(null, 0);
    }

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BlurView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BlurView, defStyleAttr, 0);
        round = a.getBoolean(R.styleable.BlurView_blv_hasRound, false);
        topLeftRound = a.getBoolean(R.styleable.BlurView_blv_hasTopLeftRound, false);
        topRightRound = a.getBoolean(R.styleable.BlurView_blv_hasTopRightRound, false);
        bottomLeftRound = a.getBoolean(R.styleable.BlurView_blv_hasBottomLeftRound, false);
        bottomRightRound = a.getBoolean(R.styleable.BlurView_blv_hasBottomRightRound, false);
        roundCornerRadius = a.getDimensionPixelSize(R.styleable.BlurView_blv_roundRadius, 0);
        barrierColor = a.getColor(R.styleable.BlurView_blv_barrierColor, Color.TRANSPARENT);
        overlayColor = a.getColor(R.styleable.BlurView_blv_overlayColor, Color.TRANSPARENT);
        overlayBlendMode = a.getColor(R.styleable.BlurView_blv_overlayBlendMode, SRC_OVER);
        a.recycle();

        roundPaint = new Paint();
        roundPaint.setAntiAlias(true);
        roundPaint.setDither(true);
        roundPaint.setFilterBitmap(true);
        roundPaint.setColor(Color.WHITE);
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        roundPath = new Path();
        rectF = new RectF(0, 0, getWidth(), getHeight());
        Arrays.fill(radii, roundCornerRadius);
        roundPath.addRoundRect(rectF, radii, Path.Direction.CCW);

        roundCornerPath = new Path();

        imagePaint = new Paint();
        imagePaint.setAntiAlias(true);
        imagePaint.setColor(Color.WHITE);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        drawRoundPath(canvas);
        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);
        boolean shouldDraw = blurController.draw(canvas);
        if (shouldDraw) {
            super.draw(canvas);
        }
        drawRoundPath(canvas);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRoundPath(w, h);
        blurController.updateBlurViewSize();
    }

    private void updateRoundPath(int w, int h) {
        rectF.set(0, 0, w, h);
        roundPath.reset();
        roundPath.addRoundRect(rectF, radii, Path.Direction.CW);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isHardwareAccelerated()) {
            Log.e(TAG, "BlurView can't be used in not hardware-accelerated window!");
        } else {
            blurController.setBlurAutoUpdate(true);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.blurController.destroy();
    }

    public void setOverlayColor(@ColorInt int overlayColor) {
        this.overlayColor = overlayColor;
        invalidate();
    }

    /**
     * @param rootView root to start blur from.
     *                 Can be Activity's root content layout (android.R.id.content)
     *                 or (preferably) some of your layouts. The lower amount of Views are in the root, the better for performance.
     * @return {@link BlurView} to setup needed params.
     */
    public BlurViewFacade setupWith(@NonNull ViewGroup rootView) {
        BlurController blurController = new BlockingBlurController(this, rootView);
        this.blurController.destroy();
        this.blurController = blurController;

        return blurController;
    }

    // Setters duplicated to be able to conveniently change these settings outside of setupWith chain

    /**
     * @see BlurViewFacade#setBlurRadius(float)
     */
    public BlurViewFacade setBlurRadius(float radius) {
        return blurController.setBlurRadius(radius);
    }

    /**
     * @see BlurViewFacade#setBlurAutoUpdate(boolean)
     */
    public BlurViewFacade setBlurAutoUpdate(boolean enabled) {
        return blurController.setBlurAutoUpdate(enabled);
    }

    /**
     * @see BlurViewFacade#setBlurEnabled(boolean)
     */
    public BlurViewFacade setBlurEnabled(boolean enabled) {
        return blurController.setBlurEnabled(enabled);
    }

    private boolean hasRound() {
        return round || topLeftRound || topRightRound || bottomLeftRound || bottomRightRound;
    }

    private void drawRoundPath(Canvas canvas) {
        if (!hasRound() || roundCornerRadius <= 0)
            return;

        roundCornerPath.reset();
        if (round) {
            addTopLeftPath();
            addTopRightPath();
            addBottomLeftPath();
            addBottomRightPath();
        } else {
            if (topLeftRound) {
                addTopLeftPath();
            }
            if (topRightRound) {
                addTopRightPath();
            }
            if (bottomLeftRound) {
                addBottomLeftPath();
            }
            if (bottomRightRound) {
                addBottomRightPath();
            }
        }
        canvas.drawPath(roundCornerPath, roundPaint);
    }

    private void addTopLeftPath() {
        if (roundCornerRadius > 0) {
            roundCornerPath.moveTo(0, roundCornerRadius);
            roundCornerPath.lineTo(0, 0);
            roundCornerPath.lineTo(roundCornerRadius, 0);
            roundCornerPath.arcTo(new RectF(0, 0, roundCornerRadius * 2, roundCornerRadius * 2),
                    -90, -90);
            roundCornerPath.close();
        }
    }

    private void addTopRightPath() {
        if (roundCornerRadius > 0) {
            int width = getWidth();
            roundCornerPath.moveTo(width - roundCornerRadius, 0);
            roundCornerPath.lineTo(width, 0);
            roundCornerPath.lineTo(width, roundCornerRadius);
            roundCornerPath.arcTo(new RectF(width - 2 * roundCornerRadius, 0, width,
                    roundCornerRadius * 2), 0, -90);
            roundCornerPath.close();
        }
    }

    private void addBottomLeftPath() {
        if (roundCornerRadius > 0) {
            int height = getHeight();
            roundCornerPath.moveTo(0, height - roundCornerRadius);
            roundCornerPath.lineTo(0, height);
            roundCornerPath.lineTo(roundCornerRadius, height);
            roundCornerPath.arcTo(new RectF(0, height - 2 * roundCornerRadius,
                    roundCornerRadius * 2, height), 90, 90);
            roundCornerPath.close();
        }
    }

    private void addBottomRightPath() {
        if (roundCornerRadius > 0) {
            int height = getHeight();
            int width = getWidth();
            roundCornerPath.moveTo(width - roundCornerRadius, height);
            roundCornerPath.lineTo(width, height);
            roundCornerPath.lineTo(width, height - roundCornerRadius);
            roundCornerPath.arcTo(new RectF(width - 2 * roundCornerRadius, height - 2
                    * roundCornerRadius, width, height), 0, 90);
            roundCornerPath.close();
        }
    }

    @Nullable
    PorterDuff.Mode getPorterDuffMode() {
        switch (overlayBlendMode) {
            default:
            case -1:
                return null;

            case 0:
                return PorterDuff.Mode.CLEAR;
            case 1:
                return PorterDuff.Mode.SRC;
            case 2:
                return PorterDuff.Mode.DST;
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 4:
                return PorterDuff.Mode.DST_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 6:
                return PorterDuff.Mode.DST_IN;
            case 7:
                return PorterDuff.Mode.SRC_OUT;
            case 8:
                return PorterDuff.Mode.DST_OUT;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 10:
                return PorterDuff.Mode.DST_ATOP;
            case 11:
                return PorterDuff.Mode.XOR;
            case 16:
                return PorterDuff.Mode.DARKEN;
            case 17:
                return PorterDuff.Mode.LIGHTEN;
            case 13:
                return PorterDuff.Mode.MULTIPLY;
            case 14:
                return PorterDuff.Mode.SCREEN;
            case 12:
                return PorterDuff.Mode.ADD;
            case 15:
                return PorterDuff.Mode.OVERLAY;
        }

    }

}
