package com.ue.graffiti.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ue.graffiti.R;
import com.ue.graffiti.event.BarSensorListener;
import com.ue.graffiti.model.Picture;
import com.ue.graffiti.model.Text;
import com.ue.graffiti.touch.TextImageTouch;
import com.ue.graffiti.util.TouchUtils;

public class TextImageView extends View {
    private Bitmap savedBitmap;
    private TextImageTouch touch;
    private PointF textPoint;//文字坐标
    private PointF centerPoint;//文字中心

    private int contentId;
    private Bitmap imageContent;

    private Paint drawTextPaint;
    private String textContent = "";

    private BarSensorListener mBarSensorListener;
    //0:text,1:image
    private int type;

    public TextImageView(Context context) {
        this(context, null);
    }

    public TextImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextImageView);
        type = ta.getInt(R.styleable.TextImageView_type, 0);
        if (type == 0) {
            //text
            drawTextPaint = new Paint();
            drawTextPaint.setTextSize(50);
            drawTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            //image
            textPoint = new PointF();
            centerPoint = new PointF();
        }
        ta.recycle();
    }

    public void setBarSensorListener(BarSensorListener barSensorListener) {
        mBarSensorListener = barSensorListener;
    }

    public void setTextColor(int color) {
        drawTextPaint.setColor(color);
        invalidate();
    }

    public void setBitmap(Bitmap savedBitmap, int canvasWidth, int canvasHeight, int paintColor) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true);

        touch = new TextImageTouch(true, canvasWidth, canvasHeight);
        if (mBarSensorListener != null) {
            touch.setBarSensorListener(mBarSensorListener);
        }

        drawTextPaint.setColor(paintColor);

        textPoint = new PointF();
        textPoint.set(touch.getTextPoint());
        centerPoint = new PointF();
        centerPoint.set(touch.getCenterPoint());
    }

    public void setBitmap(Bitmap savedBitmap, int canvasWidth, int canvasHeight) {
        this.savedBitmap = Bitmap.createScaledBitmap(savedBitmap, canvasWidth, canvasHeight, true);

        textPoint.set(canvasWidth / 2.5f, canvasHeight / 2.5f);
        centerPoint.set(textPoint);

        touch = new TextImageTouch(false, canvasWidth, canvasHeight);
        if (mBarSensorListener != null) {
            touch.setBarSensorListener(mBarSensorListener);
        }
    }

    public Text getText(int paintColor) {
        return new Text(textContent,
                touch.getDx(), touch.getDy(), touch.getScale(), touch.getDegree(),
                new PointF(touch.getCenterPoint().x, touch.getCenterPoint().y),
                new PointF(touch.getTextPoint().x, touch.getTextPoint().y), paintColor);
    }

    public Picture getPicture() {
        return new Picture(contentId,
                touch.getDx(), touch.getDy(), touch.getScale(), touch.getDegree(),
                new PointF(centerPoint.x, centerPoint.y),
                new PointF(textPoint.x, textPoint.y));
    }

    //触摸事件
    public boolean onTouchEvent(MotionEvent event) {
        touch.setCurPoint(new PointF(event.getX(0), event.getY(0)));
        touch.setSecPoint(event.getPointerCount() > 1 ? new PointF(event.getX(1), event.getY(1)) : new PointF(1, 1));

        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            // 第一只手指按下
            case MotionEvent.ACTION_DOWN:
                if (mBarSensorListener != null && mBarSensorListener.isTopToolbarVisible()) {
                    mBarSensorListener.closeTools();
                    touch.setDis(Float.MAX_VALUE);
                }
                touch.down1();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 第二个手指按下
                touch.down2();
                break;
            case MotionEvent.ACTION_MOVE:
                touch.move();
                break;
            // 第一只手指抬起
            case MotionEvent.ACTION_UP:
                //第二只手抬起
            case MotionEvent.ACTION_POINTER_UP:
                touch.up();
                break;
        }
        invalidate();

        return true;
    }

    //重绘
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(savedBitmap, 0, 0, new Paint());// 画其余图元
        canvas.translate(touch.getDx(), touch.getDy());
        canvas.scale(touch.getScale(), touch.getScale(), centerPoint.x, centerPoint.y);
        canvas.rotate(touch.getDegree(), centerPoint.x, centerPoint.y);

        if (type == 0) {
            //text
            canvas.drawText(textContent, textPoint.x, textPoint.y, drawTextPaint);
            return;
        }
        //image
        if (imageContent != null) {
            canvas.drawBitmap(imageContent, textPoint.x, textPoint.y, null);
        }
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setContentAndCenterPoint(int contentId) {
        this.contentId = contentId;
        TouchUtils.INSTANCE.ensureBitmapRecycled(imageContent);
        this.imageContent = BitmapFactory.decodeResource(getContext().getResources(), contentId);
        centerPoint.set(textPoint.x + imageContent.getWidth() / 2, textPoint.y + imageContent.getHeight() / 2);
    }

    public TextImageTouch getTouch() {
        return touch;
    }

    public Bitmap getImageContent() {
        return imageContent;
    }
}