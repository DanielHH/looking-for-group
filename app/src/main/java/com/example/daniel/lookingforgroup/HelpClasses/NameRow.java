package com.example.daniel.lookingforgroup.HelpClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class NameRow extends View {

    private Paint textPaint;
    private Paint linePaint;
    private String name;
    private PopUpList parent;
    private int width;

    public NameRow(Context context) {
        super(context);
        init();
    }

    public NameRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NameRow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NameRow(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText(name, 0,  70, textPaint);
        canvas.drawLine(0,100,840,100,linePaint);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width + 20,  100 );
    }


    private void init(){
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.selectChild(name);
            }
        });

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setTextAlign(Paint.Align.LEFT);
        linePaint.setColor(Color.BLACK);

    }

    public void setName(String name) {
        this.name = name;
        invalidate();
        requestLayout();
    }

    public void setParent(PopUpList popUpList) {
        this.parent = popUpList;
    }

    public void setViewWidth(int widthBound){
        this.width = widthBound;
        invalidate();
        requestLayout();
    }

}