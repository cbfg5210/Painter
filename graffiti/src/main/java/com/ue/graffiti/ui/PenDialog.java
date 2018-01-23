package com.ue.graffiti.ui;

import android.content.DialogInterface;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ue.adapterdelegate.Item;
import com.ue.adapterdelegate.OnDelegateClickListener;
import com.ue.graffiti.R;
import com.ue.graffiti.constant.SPKeys;
import com.ue.graffiti.model.PenCatTitleItem;
import com.ue.graffiti.model.PenShapeItem;
import com.ue.graffiti.util.PenUtils;
import com.ue.graffiti.util.ResourceUtils;
import com.ue.graffiti.util.SPUtils;
import com.ue.graffiti.widget.PenEffectView;

import java.util.ArrayList;
import java.util.List;

//调色板对话框
public class PenDialog extends DialogFragment implements OnSeekBarChangeListener {
    public static final int FLAG_SHAPE = 0;
    public static final int FLAG_EFFECT = 1;
    //获取当前绘制画笔
    private Paint paint;
    // 调整笔触相关控件
    private PenEffectView peneffectVi;
    private SeekBar penwidthSeekBar;
    private TextView penwidthTextVi;
    // 线形按钮
    private Matrix matrix;

    private int paintStrokeSize;
    private int lastShapeIndex;
    private int lastEffectIndex;
    private int lastShapeImage;
    private int lastEffectImage;

    private PenStyleAdapter adapter;

    public void setCurrentPaint(Paint currentPaint) {
        this.paint = currentPaint;
    }

    public static PenDialog newInstance() {
        PenDialog dialog = new PenDialog();
        dialog.setStyle(STYLE_NO_TITLE, R.style.GraffitiDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.dialog_pen, null);

        peneffectVi = (PenEffectView) contentView.findViewById(R.id.pevPenEffect);
        peneffectVi.setPaint(paint);

        penwidthSeekBar = (SeekBar) contentView.findViewById(R.id.sbPenStroke);
        penwidthTextVi = (TextView) contentView.findViewById(R.id.tvPenStroke);

        matrix = new Matrix();
        matrix.setSkew(2, 2);
        // 设置监听
        penwidthSeekBar.setOnSeekBarChangeListener(this);

        contentView.findViewById(R.id.btnPickPen).setOnClickListener(v -> dismiss());

        // 以当前画笔风格初始化特效区域
        paintStrokeSize = (int) paint.getStrokeWidth();
        penwidthSeekBar.setProgress(paintStrokeSize);

        penwidthTextVi.setText(Integer.toString(paintStrokeSize));
        peneffectVi.invalidate();
        /*
        * init effect list
        * */
        adapter = new PenStyleAdapter(getActivity(), getPenList());
        adapter.setDelegateClickListener(penShapeListener);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanCount(position);
            }
        });

        RecyclerView rvPenStyles = contentView.findViewById(R.id.rvPenStyles);
        rvPenStyles.setHasFixedSize(true);
        rvPenStyles.setLayoutManager(layoutManager);
        rvPenStyles.setAdapter(adapter);

        return contentView;
    }

    private OnDelegateClickListener penShapeListener = new OnDelegateClickListener() {
        @Override
        public void onClick(View view, int i) {
            PenShapeItem item = (PenShapeItem) adapter.getItems().get(i);
            if (item.flag == FLAG_SHAPE) {
                lastShapeImage = item.image;
                lastShapeIndex = item.index;
                paint.setPathEffect(PenUtils.getPaintShapeByImage(lastShapeImage, penwidthSeekBar.getProgress(), matrix));
            } else {
                lastEffectImage = item.image;
                lastEffectIndex = item.index;
                paint.setMaskFilter(PenUtils.getPaintEffectByImage(lastEffectImage));
            }
            // 刷新特效区域
            peneffectVi.invalidate();
        }
    };

    // 拖动的时候时刻更新粗细文本
    public void onProgressChanged(SeekBar seekBar, int curWidth, boolean fromUser) {
        // 移到0的时候自动转换成1
        paintStrokeSize = curWidth;
        if (paintStrokeSize == 0) {
            seekBar.setProgress(1);
            paintStrokeSize = 1;
        }
        //更新粗细文本
        penwidthTextVi.setText(Integer.toString(paintStrokeSize));
        //对于PathDashPathEffect特效要特殊处理
        switch (lastShapeImage) {
            case R.drawable.ic_line_oval: {
                //椭圆
                Path p = new Path();
                p.addOval(new RectF(0, 0, paintStrokeSize, paintStrokeSize), Path.Direction.CCW);
                paint.setPathEffect(new PathDashPathEffect(p, paintStrokeSize + 10, 0, PathDashPathEffect.Style.ROTATE));
            }
            break;
            case R.drawable.ic_line_rect: {
                //正方形
                Path p = new Path();
                p.addRect(new RectF(0, 0, paintStrokeSize, paintStrokeSize), Path.Direction.CCW);
                paint.setPathEffect(new PathDashPathEffect(p, paintStrokeSize + 10, 0, PathDashPathEffect.Style.ROTATE));
            }
            break;
            case R.drawable.ic_line_brush: {
                //毛笔
                Path p = new Path();
                p.addRect(new RectF(0, 0, paintStrokeSize, paintStrokeSize), Path.Direction.CCW);
                p.transform(matrix);
                paint.setPathEffect(new PathDashPathEffect(p, 2, 0, PathDashPathEffect.Style.TRANSLATE));
            }
            break;
            case R.drawable.ic_line_mark_pen: {
                //马克笔
                Path p = new Path();
                p.addArc(new RectF(0, 0, paintStrokeSize + 4, paintStrokeSize + 4), -90, 90);
                p.addArc(new RectF(0, 0, paintStrokeSize + 4, paintStrokeSize + 4), 90, -90);
                paint.setPathEffect(new PathDashPathEffect(p, 2, 0, PathDashPathEffect.Style.TRANSLATE));
            }
            break;
        }
        //改变粗细
        paint.setStrokeWidth(paintStrokeSize);
        // 更新示意view
        peneffectVi.invalidate();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    // 放开拖动条后 重绘特效示意区域
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private List<Item> getPenList() {
        List<Item> items = new ArrayList<>();
        int[] images = ResourceUtils.getImageArray(getContext(), R.array.penShapeImages);
        String[] names = getResources().getStringArray(R.array.penShapeNames);

        lastShapeIndex = SPUtils.getInt(SPKeys.INSTANCE.getSP_PAINT_SHAPE_INDEX(), 0);
        lastEffectIndex = SPUtils.getInt(SPKeys.INSTANCE.getSP_PAINT_EFFECT_INDEX(), 0);

        items.add(new PenCatTitleItem(getString(R.string.line_shape)));

        for (int i = 0, len = images.length; i < len; i++) {
            PenShapeItem item = new PenShapeItem(FLAG_SHAPE, images[i], names[i]);
            item.index = i;
            item.isChecked = lastShapeIndex == i;
            items.add(item);
        }

        items.add(new PenCatTitleItem(getString(R.string.special_effect)));
        images = ResourceUtils.getImageArray(getContext(), R.array.penEffectImages);
        names = getResources().getStringArray(R.array.penEffectNames);

        for (int i = 0, len = images.length; i < len; i++) {
            PenShapeItem item = new PenShapeItem(FLAG_EFFECT, images[i], names[i]);
            item.index = i;
            item.isChecked = lastEffectIndex == i;
            items.add(item);
        }

        return items;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_SIZE(), paintStrokeSize);
        SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_SHAPE_INDEX(), lastShapeIndex);
        SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_EFFECT_INDEX(), lastEffectIndex);
        SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_SHAPE_IMAGE(), lastShapeImage);
        SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_EFFECT_IMAGE(), lastEffectImage);
    }
}