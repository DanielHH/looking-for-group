package com.example.daniel.lookingforgroup.HelpClasses;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class PopUpList extends LinearLayout {

    private Context ctx;
    private List<String> names = new ArrayList<>();
    private List<NameRow> nameRows = new ArrayList<>();
    private InteractiveSearcher parent;
    private Paint textPaint;
    private Rect bounds = new Rect();

    public  PopUpList(Context context) {
        super(context);
        this.ctx = context;
        init();
    }

    public PopUpList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init();
    }

    private void init(){
        this.setOrientation(VERTICAL);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(50);
        fillView();
    }

    private void fillView(){
        this.removeAllViews();
        nameRows.clear();
        int longestWidthBound = 0;

        for (int i = 0; i < names.size(); i++) {
            //calculate text paint bounds here for 100% accurate longest width
            String name = names.get(i);
            textPaint.getTextBounds(name, 0, name.length(), bounds);
            int nameWidthBound = bounds.width();

            if (nameWidthBound > longestWidthBound) {
                longestWidthBound = nameWidthBound;
            }
        }

        for (int i = 0; i < names.size(); i++) {
            NameRow nameRow = new NameRow(ctx);
            nameRow.setName(names.get(i));
            nameRow.setParent(this);
            nameRows.add(nameRow);
            nameRow.setViewWidth(longestWidthBound);
            this.addView(nameRow);
        }
    }

    private void reDraw() {
        fillView();
        invalidate();
        requestLayout();
    }

    public void setNames(List<String> names){
        this.names = names;
        reDraw();
    }

    public void clearNames() {
        this.names.clear();
        reDraw();
    }

    public void setParent(InteractiveSearcher interactiveSearcher) {
        this.parent = interactiveSearcher;
    }

    public void selectChild(String name){
        parent.goToUserPage(name);
    }
}