package com.panniku.mp3player.Visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.Nullable;

public class BarVisualizer extends BaseVisualizer {

    private float density = 50;
    private int gap;

    public BarVisualizer(Context context) {
        super(context);
    }

    public BarVisualizer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BarVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        this.density = 50;
        this.gap = 4;
        paint.setStyle(Paint.Style.FILL);
    }

    // density is "amount" of bars to display
    public void setDensity(float density){
        this.density = density;
        if(density > 256){
            this.density = 256;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(bytes != null) {
            float barWidth = getWidth() / density;
            float div = bytes.length / density;
            paint.setStrokeWidth(barWidth - gap);
            for(int i = 0; i < density; i++){
                int bytePos = (int) Math.ceil(i * div);
                int top = getHeight() + ((byte) (Math.abs(bytes[bytePos]) + 128)) * getHeight() / 128;
                float barX = (i * barWidth) + (barWidth / 2);
                canvas.drawLine(barX, getHeight(), barX, top, paint);

                //Log.d("onDraw()", "bytePos: " + bytePos);
                //Log.d("onDraw()", "getHeight(): " + getHeight());
                //Log.d("onDraw()", "top: " + top);
                //Log.d("onDraw()", "barX: " + barX);

            }
            super.onDraw(canvas);
        }
    }
}
