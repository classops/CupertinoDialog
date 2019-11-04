package com.hanter.android.radwidget.cupertino.blur;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import com.hanter.android.radwidget.cupertino.ScriptC_BlendOverlay;

/**
 * Blur using RenderScript, processed on GPU.
 * Uses Renderscript from support library
 */
public final class SupportRenderScriptBlur implements BlurAlgorithm {
    private final RenderScript renderScript;
    private final ScriptIntrinsicBlur blurScript;
    private ScriptC_BlendOverlay overlayScript;
    private Allocation outAllocation;

    private int lastBitmapWidth = -1;
    private int lastBitmapHeight = -1;


    /**
     * @param context Context to create the {@link RenderScript}
     */
    public SupportRenderScriptBlur(Context context) {
        renderScript = RenderScript.create(context);
        blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        this.overlayScript = new ScriptC_BlendOverlay(renderScript);
    }

    private boolean canReuseAllocation(Bitmap bitmap) {
        return bitmap.getHeight() == lastBitmapHeight && bitmap.getWidth() == lastBitmapWidth;
    }

    /**
     * @param bitmap     bitmap to blur
     * @param blurRadius blur radius (1..25)
     * @return blurred bitmap
     */
    @Override
    public final Bitmap blur(Bitmap bitmap, float blurRadius) {
        return blur(bitmap, blurRadius, false, 0);
    }

    @Override
    public Bitmap blur(Bitmap bitmap, float blurRadius, int overlayColor) {
        return blur(bitmap, blurRadius, true, overlayColor);
    }

    public final Bitmap blur(Bitmap bitmap, float blurRadius, boolean drawOverlay, int overlayColor) {
        //Allocation will use the same backing array of pixels as bitmap if created with USAGE_SHARED flag
        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bitmap);

        if (!canReuseAllocation(bitmap)) {
            if (outAllocation != null) {
                outAllocation.destroy();
            }
            outAllocation = Allocation.createTyped(renderScript, inAllocation.getType());
            lastBitmapWidth = bitmap.getWidth();
            lastBitmapHeight = bitmap.getHeight();
        }

        blurScript.setRadius(blurRadius);
        blurScript.setInput(inAllocation);
        //do not use inAllocation in forEach. it will cause visual artifacts on blurred Bitmap
        blurScript.forEach(outAllocation);

        if (drawOverlay) {
            overlayScript.invoke_setOverlayColor(overlayColor);
            overlayScript.forEach_overlay(outAllocation, outAllocation);
        }

        outAllocation.copyTo(bitmap);

        inAllocation.destroy();
        return bitmap;
    }

    @Override
    public final void destroy() {
        blurScript.destroy();
        renderScript.destroy();
        if (outAllocation != null) {
            outAllocation.destroy();
        }
    }

    @Override
    public boolean canModifyBitmap() {
        return true;
    }

    @NonNull
    @Override
    public Bitmap.Config getSupportedBitmapConfig() {
        return Bitmap.Config.ARGB_8888;
    }
}
