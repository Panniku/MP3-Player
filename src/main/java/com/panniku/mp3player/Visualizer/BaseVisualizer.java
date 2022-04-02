package com.panniku.mp3player.Visualizer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;

public abstract class BaseVisualizer extends View {

    protected byte[] bytes;
    protected Paint paint;
    protected Visualizer visualizer;
    protected int color = Color.argb(100, 157, 121, 168);

    public BaseVisualizer(Context context) {
        super(context);
        init(null);
        init();
    }

    public BaseVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        init();
    }

    public BaseVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
        init();
    }

    private void init(AttributeSet attributeSet) {
        paint = new Paint();
    }

    // set color to visualizer
    public void setColor(int color) {
        this.color = color;
        this.paint.setColor(this.color);
    }

    public void setPlayer(int audioId) {
        visualizer = new Visualizer(audioId);
        visualizer.setEnabled(false);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                BaseVisualizer.this.bytes = bytes;
                invalidate();
                //Log.d("visualizer", "getMaxCaptureRate(): " + Visualizer.getMaxCaptureRate());
                //Log.d("visualizer", "getMaxCaptureRate() / 2: " + Visualizer.getMaxCaptureRate() / 2);
                //Log.d("BaseVisualizer", "Updating on capture.");
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                //
            }
        }, (int) (Visualizer.getMaxCaptureRate() / 1.2), true, false);
        visualizer.setEnabled(true);
    }

    public void release() {
        if(visualizer == null) return;
        visualizer.release();
        bytes = null;
        invalidate();
    }

    public Visualizer getVisualizer() {
        return visualizer;
    }

    public byte[] getBytes(){
        return this.bytes;
    }

    protected abstract void init();
}
