package com.ue.graffiti.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.ue.graffiti.R;
import com.ue.graffiti.constant.SPKeys;
import com.ue.graffiti.event.OnMultiTouchListener;
import com.ue.graffiti.event.SimpleTouchListener;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.Picture;
import com.ue.graffiti.model.Step;
import com.ue.graffiti.model.Text;
import com.ue.graffiti.touch.DrawFreehandTouch;
import com.ue.graffiti.touch.Touch;
import com.ue.graffiti.touch.TransformTouch;
import com.ue.graffiti.util.PenUtils;
import com.ue.graffiti.util.SPUtils;
import com.ue.graffiti.util.TouchUtils;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class CanvasView extends View implements SimpleTouchListener {
    // 图元平移缩放
    // 动画画笔（变换相位用）
    private float phase;
    // 动画效果画笔
    private Paint animPelPaint;
    // 画画用的画笔
    private Paint drawPelPaint;
    private Paint drawTextPaint;
    private Paint drawPicturePaint;

    private Paint currentPaint;
    //画布宽
    private int mCanvasWidth;
    //画布高
    private int mCanvasHeight;
    //undo栈
    private Stack<Step> undoStack;
    //redo栈
    private Stack<Step> redoStack;
    // 图元链表
    private List<Pel> pelList;
    // 画布裁剪区域
    private Region clipRegion;
    // 当前被选中的图元
    private Pel selectedPel;
    // 重绘位图
    private Bitmap savedBitmap;
    //重绘画布
    private Canvas savedCanvas;

    private Bitmap backgroundBitmap;
    //原图片副本，清空或还原时用
    private Bitmap copyOfBackgroundBitmap;
    private Bitmap originalBackgroundBitmap;
    //触摸操作
    private Touch touch;
    private Canvas cacheCanvas;

    private boolean isSensorRegistered;
    private OnMultiTouchListener mMultiTouchListener;

    public void setMultiTouchListener(OnMultiTouchListener multiTouchListener) {
        mMultiTouchListener = multiTouchListener;
        if (touch != null) {
            touch.setMultiTouchListener(multiTouchListener);
        }
    }

    public boolean isSensorRegistered() {
        return isSensorRegistered;
    }

    public void setSensorRegistered(boolean sensorRegistered) {
        isSensorRegistered = sensorRegistered;
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //初始化画布宽高为屏幕宽高
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCanvasWidth = metrics.widthPixels;
        mCanvasHeight = metrics.heightPixels;
        //初始化undo redo栈
        undoStack = new Stack<Step>();
        redoStack = new Stack<Step>();
        // 图元总链表
        pelList = new LinkedList<Pel>();
        savedCanvas = new Canvas();
        //获取画布裁剪区域
        clipRegion = new Region();
        //初始化为自由手绘操作
        setTouch(new DrawFreehandTouch(this));

        drawPelPaint = new Paint(Paint.DITHER_FLAG);
        drawPelPaint.setStyle(Paint.Style.STROKE);
        drawPelPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPelPaint.setAntiAlias(true);
        drawPelPaint.setDither(true);
        drawPelPaint.setStrokeJoin(Paint.Join.ROUND);

        int lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, getResources().getColor(R.color.col_298ecb));
        int lastStrokeWidth = SPUtils.getInt(SPKeys.SP_PAINT_SIZE, 1);
        int lastShapeImage = SPUtils.getInt(SPKeys.SP_PAINT_SHAPE_IMAGE, R.drawable.ic_line_solid);
        int lastEffectImage = SPUtils.getInt(SPKeys.SP_PAINT_EFFECT_IMAGE, R.drawable.ic_effect_solid);

        drawPelPaint.setColor(lastColor);
        drawPelPaint.setStrokeWidth(lastStrokeWidth);
        drawPelPaint.setPathEffect(PenUtils.getPaintShapeByImage(lastShapeImage, 1, null));
        drawPelPaint.setMaskFilter(PenUtils.getPaintEffectByImage(lastEffectImage));

        currentPaint = drawPelPaint;
        animPelPaint = new Paint(drawPelPaint);
        drawPicturePaint = new Paint();

        drawTextPaint = new Paint();
        drawTextPaint.setColor(drawPelPaint.getColor());
        drawTextPaint.setTextSize(50);

        initBitmap();
        updateSavedBitmap();
    }

    public int getPaintColor() {
        return currentPaint.getColor();
    }

    public void setPaintColor(int paintColor) {
        drawPelPaint.setColor(paintColor);
        drawTextPaint.setColor(paintColor);
    }

    public Paint getCurrentPaint() {
        return currentPaint;
    }

    public void initBitmap() {
        clipRegion.set(new Rect(0, 0, mCanvasWidth, mCanvasHeight));
        BitmapDrawable backgroundDrawable = (BitmapDrawable) this.getResources().getDrawable(R.drawable.bg_canvas0);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(backgroundDrawable.getBitmap(), mCanvasWidth, mCanvasHeight, true);

        TouchUtils.ensureBitmapRecycled(backgroundBitmap);
        backgroundBitmap = scaledBitmap.copy(Config.ARGB_8888, true);
        TouchUtils.ensureBitmapRecycled(scaledBitmap);

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap);
        copyOfBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap);
        originalBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);

        cacheCanvas = new Canvas();
        cacheCanvas.setBitmap(backgroundBitmap);
    }

    //重绘
    protected void onDraw(Canvas canvas) {
        // 画其余图元
        canvas.drawBitmap(savedBitmap, 0, 0, new Paint());

        if (selectedPel == null) {
            return;
        }
        if (!(touch instanceof TransformTouch)) {
            //画图状态不产生动态画笔效果
            canvas.drawPath(selectedPel.path, drawPelPaint);
            return;
        }
        //选中状态才产生动态画笔效果
        setAnimPaint();
        canvas.drawPath(selectedPel.path, animPelPaint);
        // 画笔动画效果
        invalidate();
    }

    @Override
    public void updateSavedBitmap(@NotNull Canvas canvas, @NotNull Bitmap bitmap, @NotNull List<? extends Pel> pelList, @NotNull Pel selectedPel, boolean isInvalidate) {
        //更新重绘背景位图用（当且仅当选择的图元有变化的时候才调用）
        //创建缓冲位图
        TouchUtils.ensureBitmapRecycled(bitmap);
        //由画布背景创建缓冲位图
        bitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //与画布建立联系
        canvas.setBitmap(bitmap);
        //画除selectedPel外的所有图元
        drawPels(canvas, pelList, selectedPel);

        savedBitmap = bitmap;

        if (isInvalidate) {
            invalidate();
        }
    }

    public void updateSavedBitmap() {
        updateSavedBitmap(savedCanvas, savedBitmap, pelList, selectedPel, true);
    }

    private void drawPels(Canvas savedCanvas, List<? extends Pel> pelList, Pel selectedPel) {
        // 获取pelList对应的迭代器头结点
        ListIterator<? extends Pel> pelIterator = pelList.listIterator();
        while (pelIterator.hasNext()) {
            Pel pel = pelIterator.next();
            //若是文本图元
            if (pel.text != null) {
                Text text = pel.text;
                savedCanvas.save();
                savedCanvas.translate(text.getTransDx(), text.getTransDy());
                savedCanvas.scale(text.getScale(), text.getScale(), text.getCenterPoint().x, text.getCenterPoint().y);
                savedCanvas.rotate(text.getDegree(), text.getCenterPoint().x, text.getCenterPoint().y);
                savedCanvas.drawText(text.getContent(), text.getBeginPoint().x, text.getBeginPoint().y, text.getPaint());
                savedCanvas.restore();
                continue;
            }
            if (pel.picture != null) {
                Picture picture = pel.picture;
                savedCanvas.save();
                savedCanvas.translate(picture.getTransDx(), picture.getTransDy());
                savedCanvas.scale(picture.getScale(), picture.getScale(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.rotate(picture.getDegree(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.drawBitmap(picture.createContent(getContext()), picture.getBeginPoint().x, picture.getBeginPoint().y, drawPicturePaint);
                savedCanvas.restore();
                continue;
            }
            if (!pel.equals(selectedPel)) {
                //若非选中的图元
                savedCanvas.drawPath(pel.path, pel.paint);
            }
        }
    }

    // 动画画笔更新
    private void setAnimPaint() {
        // 变相位
        phase++;

        Path p = new Path();
        // 路径单元是矩形（也可以为椭圆）
        p.addRect(new RectF(0, 0, 6, 3), Path.Direction.CCW);
        // 设置路径效果
        PathDashPathEffect effect = new PathDashPathEffect(p, 12, phase, PathDashPathEffect.Style.ROTATE);
        animPelPaint.setColor(Color.BLACK);
        animPelPaint.setPathEffect(effect);
    }

    /**
     * get()方法:获取CanvasView下指定成员
     */
    public int getCanvasWidth() {
        return mCanvasWidth;
    }

    public int getCanvasHeight() {
        return mCanvasHeight;
    }

    public Region getClipRegion() {
        return clipRegion;
    }

    public List<Pel> getPelList() {
        return pelList;
    }

    public Pel getSelectedPel() {
        return selectedPel;
    }

    public Bitmap getSavedBitmap() {
        return savedBitmap;
    }

    public Bitmap getBackgroundBitmap() {
        return backgroundBitmap;
    }

    public Bitmap getCopyOfBackgroundBitmap() {
        return copyOfBackgroundBitmap;
    }

    public Bitmap getOriginalBackgroundBitmap() {
        return originalBackgroundBitmap;
    }

    public Touch getTouch() {
        return touch;
    }

    public Stack<Step> getUndoStack() {
        return undoStack;
    }

    /*
     * set()方法:设置CanvasView下指定成员
     */
    public void setSelectedPel(Pel pel) {
        selectedPel = pel;
    }

    public void setBackgroundBitmap(int id) {
        //以已提供选择的背景图片换画布
        BitmapDrawable backgroundDrawable = (BitmapDrawable) this.getResources().getDrawable(id);
        Bitmap offeredBitmap = backgroundDrawable.getBitmap();

        TouchUtils.ensureBitmapRecycled(backgroundBitmap);
        backgroundBitmap = Bitmap.createScaledBitmap(offeredBitmap, mCanvasWidth, mCanvasHeight, true);

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap);
        copyOfBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap);
        originalBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);

        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap);//填充区域重新打印
        updateSavedBitmap();
    }

    public void setBackgroundBitmap(Bitmap photo) {
        //以图库或拍照得到的背景图片换画布
        TouchUtils.ensureBitmapRecycled(backgroundBitmap);
        backgroundBitmap = Bitmap.createScaledBitmap(photo, mCanvasWidth, mCanvasHeight, true);

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap);
        copyOfBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);

        TouchUtils.ensureBitmapRecycled(originalBackgroundBitmap);
        originalBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);
        //填充区域重新打印
        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap);
        updateSavedBitmap();
    }

    public void setProcessedBitmap(Bitmap imgPro) {
        //设置处理后的图片作为背景
        TouchUtils.ensureBitmapRecycled(backgroundBitmap);
        backgroundBitmap = Bitmap.createScaledBitmap(imgPro, mCanvasWidth, mCanvasHeight, true);

        TouchUtils.ensureBitmapRecycled(copyOfBackgroundBitmap);
        copyOfBackgroundBitmap = backgroundBitmap.copy(Config.ARGB_8888, true);
        //填充区域重新打印
        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap);
        updateSavedBitmap();
    }

    public void setBackgroundBitmap() {
        //清空画布时将之前保存的副本背景作为重绘（去掉填充）
        TouchUtils.ensureBitmapRecycled(backgroundBitmap);
        backgroundBitmap = copyOfBackgroundBitmap.copy(Config.ARGB_8888, true);
        //填充区域重新打印
        TouchUtils.reprintFilledAreas(undoStack, backgroundBitmap);
        updateSavedBitmap();
    }

    public void setTouch(Touch childTouch) {
        touch = childTouch;
        touch.setTouchListener(this, this);
        if (mMultiTouchListener != null) {
            childTouch.setMultiTouchListener(mMultiTouchListener);
        }
    }

    public void clearData() {
        pelList.clear();
        undoStack.clear();
        redoStack.clear();
        //若有选中的图元失去焦点
        setSelectedPel(null);
    }

    public void clearRedoStack() {
        if (!redoStack.empty()) {
            redoStack.clear();
        }
    }

    public void addPel(Pel newPel) {
        pelList.add(newPel);
    }

    public void pushUndoStack(Step step) {
        undoStack.push(step);
    }

    public void pushRedoStack(Step step) {
        redoStack.push(step);
    }

    public void removePel(Pel pel) {
        pelList.remove(pel);
    }

    public Step popUndoStack() {
        return undoStack.empty() ? null : undoStack.pop();
    }

    public Step popRedoStack() {
        return redoStack.empty() ? null : redoStack.pop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (touch != null) {
            touch.setTouchListener(null, new SimpleTouchListener() {
                @Override
                public void updateSavedBitmap(@NotNull Canvas canvas, @NotNull Bitmap bitmap, @NotNull List<? extends Pel> pelList, @NotNull Pel selectedPel, boolean isInvalidate) {
                }

                @Override
                public void invalidate() {
                }

                @Override
                public boolean isSensorRegistered() {
                    return true;
                }

                @Override
                public Bitmap getBackgroundBitmap() {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                }

                @Override
                public Bitmap getCopyOfBackgroundBitmap() {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                }

                @Override
                public void setSelectedPel(Pel pel) {
                }

                @Override
                public Paint getCurrentPaint() {
                    return new Paint();
                }

                @Override
                public Context getContext() {
                    return getContext().getApplicationContext();
                }
            });
        }
    }
}