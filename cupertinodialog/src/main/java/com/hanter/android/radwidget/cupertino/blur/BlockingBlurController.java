package com.hanter.android.radwidget.cupertino.blur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Blur Controller that handles all blur logic for the attached View.
 * It honors View size changes, View animation and Visibility changes.
 * <p>
 * The basic idea is to draw the view hierarchy on a bitmap, excluding the attached View,
 * then blur and draw it on the system Canvas.
 * <p>
 * It uses {@link ViewTreeObserver.OnPreDrawListener} to detect when
 * blur should be updated.
 * <p>
 * Blur is done on the main thread.
 */
final class BlockingBlurController implements BlurController {

    private static final String TAG = "BlockingBlurController";

    private static final boolean OVERLAY_BY_CANVAS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    // Bitmap size should be divisible by ROUNDING_VALUE to meet stride requirement.
    // This will help avoiding an extra bitmap allocation when passing the bitmap to RenderScript for blur.
    // Usually it's 16, but on Samsung devices it's 64 for some reason.
    private static final int ROUNDING_VALUE = 64;
    private final float scaleFactor = DEFAULT_SCALE_FACTOR;
    private float blurRadius = DEFAULT_BLUR_RADIUS;
    private float roundingWidthScaleFactor = 1f;
    private float roundingHeightScaleFactor = 1f;

    private BlurAlgorithm blurAlgorithm;
    private Canvas internalCanvas;
    private Bitmap internalBitmap;

    private final View blurView;
    private final ViewGroup rootView;
    private int overlayColor;
    private final int[] rootLocation = new int[2];
    private final int[] blurViewLocation = new int[2];
    private final Rect bitmapRect = new Rect();

    private final ViewTreeObserver.OnPreDrawListener drawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            // Not invalidating a View here, just updating the Bitmap.
            // This relies on the HW accelerated bitmap drawing behavior in Android
            // If the bitmap was drawn on HW accelerated canvas, it holds a reference to it and on next
            // drawing pass the updated content of the bitmap will be rendered on the screen

            updateBlur();
            return true;
        }
    };

    private boolean blurEnabled = false;
    private int initWidth;
    private int initHeight;

    @Nullable
    private Drawable frameClearDrawable;
    private boolean hasFixedTransformationMatrix;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final RectF blurViewRect = new RectF();

    /**
     * @param blurView View which will draw it's blurred underlying content
     * @param rootView Root View where blurView's underlying content starts drawing.
     *                 Can be Activity's root content layout (android.R.id.content)
     *                 or some of your custom root layouts.
     */
    BlockingBlurController(@NonNull View blurView, @NonNull ViewGroup rootView,
                           @ColorInt int overlayColor) {
        this.rootView = rootView;
        this.blurView = blurView;
        this.overlayColor = overlayColor;
        this.blurAlgorithm = new NoOpBlurAlgorithm();

        int measuredWidth = blurView.getMeasuredWidth();
        int measuredHeight = blurView.getMeasuredHeight();

        if (isZeroSized(measuredWidth, measuredHeight)) {
            deferBitmapCreation();
            return;
        }

        init(measuredWidth, measuredHeight);
    }

    private int downScaleSize(float value) {
        return (int) Math.ceil(value / scaleFactor);
    }

    /**
     * Rounds a value to the nearest divisible by {@link #ROUNDING_VALUE} to meet stride requirement
     */
    private int roundSize(int value) {
        if (value % ROUNDING_VALUE == 0) {
            return value;
        }
        return value - (value % ROUNDING_VALUE) + ROUNDING_VALUE;
    }

    void init(int measuredWidth, int measuredHeight) {
        if (initWidth == measuredWidth && initHeight == measuredHeight) {
            return;
        }

        initWidth = measuredWidth;
        initHeight = measuredHeight;

        if (isZeroSized(measuredWidth, measuredHeight)) {
            blurEnabled = false;
            blurView.setWillNotDraw(true);
            setBlurAutoUpdateInternal(false);
            return;
        }

        blurEnabled = true;
        blurView.setWillNotDraw(false);
        allocateBitmap(measuredWidth, measuredHeight);
        internalCanvas = new Canvas(internalBitmap);
        setBlurAutoUpdateInternal(true);
        if (hasFixedTransformationMatrix) {
            setupInternalCanvasMatrix();
        }

        blurViewRect.set(0, 0, measuredWidth, measuredHeight);
    }

    private boolean isZeroSized(int measuredWidth, int measuredHeight) {
        return downScaleSize(measuredHeight) == 0 || downScaleSize(measuredWidth) == 0;
    }

    void updateBlur() {
        if (!blurEnabled) {
            return;
        }

        if (frameClearDrawable == null) {
            internalBitmap.eraseColor(Color.TRANSPARENT);
        } else {
            frameClearDrawable.draw(internalCanvas);
        }

        if (hasFixedTransformationMatrix) {
            rootView.draw(internalCanvas);
        } else {
            internalCanvas.save();
            setupInternalCanvasMatrix();
            rootView.draw(internalCanvas);
            internalCanvas.restore();
        }

        internalCanvas.drawColor(0x6604040F);

        blurAndSave();
    }

    /**
     * Deferring initialization until view is laid out
     */
    private void deferBitmapCreation() {
        blurView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    blurView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    blurView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                init(blurView.getMeasuredWidth(), blurView.getMeasuredHeight());
            }
        });
    }

    private void allocateBitmap(int measuredWidth, int measuredHeight) {
        int nonRoundedScaledWidth = downScaleSize(measuredWidth);
        int nonRoundedScaledHeight = downScaleSize(measuredHeight);

        int scaledWidth = roundSize(nonRoundedScaledWidth);
        int scaledHeight = roundSize(nonRoundedScaledHeight);

        roundingHeightScaleFactor = (float) nonRoundedScaledHeight / scaledHeight;
        roundingWidthScaleFactor = (float) nonRoundedScaledWidth / scaledWidth;

        internalBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, blurAlgorithm.getSupportedBitmapConfig());

        bitmapRect.set(0, 0, scaledWidth, scaledHeight);
    }

    /**
     * Set up matrix to draw starting from blurView's position
     */
    private void setupInternalCanvasMatrix() {
        rootView.getLocationOnScreen(rootLocation);
        blurView.getLocationOnScreen(blurViewLocation);

        int left = blurViewLocation[0] - rootLocation[0];
        int top = blurViewLocation[1] - rootLocation[1];

        float scaleFactorX = scaleFactor * roundingWidthScaleFactor;
        float scaleFactorY = scaleFactor * roundingHeightScaleFactor;

        float scaledLeftPosition = -left / scaleFactorX;
        float scaledTopPosition = -top / scaleFactorY;

        internalCanvas.translate(scaledLeftPosition, scaledTopPosition);
        internalCanvas.scale(1 / scaleFactorX, 1 / scaleFactorY);
    }

    @Override
    public boolean draw(Canvas canvas) {
        if (!blurEnabled) {
            return true;
        }
        // Not blurring own children
        if (canvas == internalCanvas) {
            return false;
        }

        updateBlur();

        canvas.drawBitmap(internalBitmap, bitmapRect, blurViewRect, paint);

        if (OVERLAY_BY_CANVAS) {
            canvas.drawColor(overlayColor, PorterDuff.Mode.OVERLAY);
        }

        return true;
    }

    private void blurAndSave() {
        if (OVERLAY_BY_CANVAS) {
            internalBitmap = blurAlgorithm.blur(internalBitmap, blurRadius);
        } else {
            internalBitmap = blurAlgorithm.blur(internalBitmap, blurRadius, overlayColor);
        }

        if (!blurAlgorithm.canModifyBitmap()) {
            internalCanvas.setBitmap(internalBitmap);
        }
    }

    @Override
    public void updateBlurViewSize() {
        init(blurView.getMeasuredWidth(), blurView.getMeasuredHeight());
    }

    @Override
    public void destroy() {
        setBlurAutoUpdateInternal(false);
        blurAlgorithm.destroy();
        if (internalBitmap != null) {
            internalBitmap.recycle();
        }
    }

    @Override
    public BlurViewFacade setBlurRadius(float radius) {
        this.blurRadius = radius;
        return this;
    }

    @Override
    public BlurViewFacade setBlurAlgorithm(BlurAlgorithm algorithm) {
        this.blurAlgorithm = algorithm;
        return this;
    }

    @Override
    public BlurViewFacade setFrameClearDrawable(@Nullable Drawable frameClearDrawable) {
        this.frameClearDrawable = frameClearDrawable;
        return this;
    }

    void setBlurEnabledInternal(boolean enabled) {
        this.blurEnabled = enabled;
        setBlurAutoUpdateInternal(enabled);
        blurView.invalidate();
    }

    @Override
    public BlurViewFacade setBlurEnabled(final boolean enabled) {
        blurView.post(new Runnable() {
            @Override
            public void run() {
                setBlurEnabledInternal(enabled);
            }
        });
        return this;
    }

    void setBlurAutoUpdateInternal(boolean enabled) {
        blurView.getViewTreeObserver().removeOnPreDrawListener(drawListener);
        if (enabled) {
            blurView.getViewTreeObserver().addOnPreDrawListener(drawListener);
        }
    }

    public BlurViewFacade setBlurAutoUpdate(final boolean enabled) {
        blurView.post(new Runnable() {
            @Override
            public void run() {
                setBlurAutoUpdateInternal(enabled);
            }
        });
        return this;
    }

    @Override
    public BlurViewFacade setHasFixedTransformationMatrix(boolean hasFixedTransformationMatrix) {
        this.hasFixedTransformationMatrix = hasFixedTransformationMatrix;
        return this;
    }

    @Override
    public BlurViewFacade setOverlayColor(int overlayColor) {
        if (this.overlayColor != overlayColor) {
            this.overlayColor = overlayColor;
            blurView.invalidate();
        }
        return this;
    }
}
