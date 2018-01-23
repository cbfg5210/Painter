package com.ue.graffiti.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class PenEffectView extends View {
    private Path path;
    private Paint mPaint;

    public PenEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPath();
        mPaint = new Paint();
    }

    public void setPaint(Paint newPaint) {
        mPaint = newPaint;
        invalidate();
    }

    //以当前选中的笔触（粗细、特效）画在矩形示意框里
    protected void onDraw(Canvas canvas) {
//        canvas.drawPath(path, DrawTouch.getCurPaint());
        canvas.drawPath(path, mPaint);
    }

    public void initPath() {
        path = new Path();

        float width = getResources().getDisplayMetrics().widthPixels;
        float height = 160;

        path.moveTo(0, height / 2);
        path.cubicTo(0, height / 2, width / 4, height / 4, width / 2, height / 2);

        Path path2 = new Path(); //下波浪 连接用
        path2.moveTo(width / 2, height / 2);
        path2.cubicTo(width / 2, height / 2, width / 4 * 3, height / 4 * 3, width, height / 2);

        path.addPath(path2);
    }
}
