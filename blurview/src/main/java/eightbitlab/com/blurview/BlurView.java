package eightbitlab.com.blurview;

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

/**
 * FrameLayout that blurs its underlying content.
 * Can have children and draw them over blurred background.
 */
public class BlurView extends FrameLayout {

    private static final String TAG = BlurView.class.getSimpleName();

    private Paint imagePaint;
    private Paint roundPaint;
    BlurController blurController = new NoOpController();
    private boolean round;
    private float roundCornerRadius;
    private float[] radii = new float[8];

    @ColorInt
    private int overlayColor;

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
        overlayColor = a.getColor(R.styleable.BlurView_blv_overlayColor, Color.TRANSPARENT);
        round = a.getBoolean(R.styleable.BlurView_blv_hasRound, false);
        roundCornerRadius = a.getDimensionPixelSize(R.styleable.BlurView_blv_roundRadius, 0);
        a.recycle();

        roundPaint = new Paint();
        roundPaint.setAntiAlias(true);
        roundPaint.setColor(Color.WHITE);
        roundPaint.setStyle(Paint.Style.FILL);
        roundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        imagePaint = new Paint();
        roundPaint.setAntiAlias(true);
        imagePaint.setColor(Color.WHITE);

        for (int i = 0; i < radii.length; i++) {
            radii[i] = roundCornerRadius;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!round || roundCornerRadius == 0) {
            super.dispatchDraw(canvas);
            return;
        }

        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);

        drawTopLeft(canvas);
        drawTopRight(canvas);
        drawBottomLeft(canvas);
        drawBottomRight(canvas);

        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!round || roundCornerRadius == 0) {
            boolean shouldDraw = blurController.draw(canvas);
            if (shouldDraw) {
                super.draw(canvas);
            }
            return;
        }

        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), imagePaint, Canvas.ALL_SAVE_FLAG);

        boolean shouldDraw = blurController.draw(canvas);
        if (shouldDraw) {
            super.draw(canvas);
        }

        drawTopLeft(canvas);
        drawTopRight(canvas);
        drawBottomLeft(canvas);
        drawBottomRight(canvas);

        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        blurController.updateBlurViewSize();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        blurController.setBlurAutoUpdate(false);
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

    /**
     * @param rootView root to start blur from.
     *                 Can be Activity's root content layout (android.R.id.content)
     *                 or (preferably) some of your layouts. The lower amount of Views are in the root, the better for performance.
     * @return {@link BlurView} to setup needed params.
     */
    public BlurViewFacade setupWith(@NonNull ViewGroup rootView) {
        BlurController blurController = new BlockingBlurController(this, rootView, overlayColor);
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
     * @see BlurViewFacade#setOverlayColor(int)
     */
    public BlurViewFacade setOverlayColor(@ColorInt int overlayColor) {
        this.overlayColor = overlayColor;
        return blurController.setOverlayColor(overlayColor);
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

    private void drawRoundPath(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, width, height), radii, Path.Direction.CCW);
        canvas.drawPath(path, roundPaint);
    }

    private void drawTopLeft(Canvas canvas) {
        if (roundCornerRadius > 0) {
            Path path = new Path();
            path.moveTo(0, roundCornerRadius);
            path.lineTo(0, 0);
            path.lineTo(roundCornerRadius, 0);
            path.arcTo(new RectF(0, 0, roundCornerRadius * 2, roundCornerRadius * 2),
                    -90, -90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawTopRight(Canvas canvas) {
        if (roundCornerRadius > 0) {
            int width = getWidth();
            Path path = new Path();
            path.moveTo(width - roundCornerRadius, 0);
            path.lineTo(width, 0);
            path.lineTo(width, roundCornerRadius);
            path.arcTo(new RectF(width - 2 * roundCornerRadius, 0, width,
                    roundCornerRadius * 2), 0, -90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawBottomLeft(Canvas canvas) {
        if (roundCornerRadius > 0) {
            int height = getHeight();
            Path path = new Path();
            path.moveTo(0, height - roundCornerRadius);
            path.lineTo(0, height);
            path.lineTo(roundCornerRadius, height);
            path.arcTo(new RectF(0, height - 2 * roundCornerRadius,
                    roundCornerRadius * 2, height), 90, 90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

    private void drawBottomRight(Canvas canvas) {
        if (roundCornerRadius > 0) {
            int height = getHeight();
            int width = getWidth();
            Path path = new Path();
            path.moveTo(width - roundCornerRadius, height);
            path.lineTo(width, height);
            path.lineTo(width, height - roundCornerRadius);
            path.arcTo(new RectF(width - 2 * roundCornerRadius, height - 2
                    * roundCornerRadius, width, height), 0, 90);
            path.close();
            canvas.drawPath(path, roundPaint);
        }
    }

}
