package com.ue.graffiti.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ue.graffiti.R;
import com.ue.graffiti.constant.SPKeys;
import com.ue.graffiti.event.BarSensorListener;
import com.ue.graffiti.helper.DialogHelper;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.Picture;
import com.ue.graffiti.model.Text;
import com.ue.graffiti.widget.CanvasView;
import com.ue.graffiti.widget.TextImageView;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by hawk on 2018/1/15.
 */

public class DrawTextDialog extends DialogFragment implements View.OnClickListener {
    private List<Pel> pelList;
    private Bitmap savedBitmap; // 当前重绘位图
    private Canvas savedCanvas; //重绘画布

    private TextImageView drawTextVi;
    private View topToolbar;
    private View downToolbar;

    private OnDrawTextListener mDrawTextListener;

    private int mCanvasWidth;
    private int mCanvasHeight;
    private int paintColor;
    //显示动画：上、下
    private Animation[] showAnimations;
    //隐藏动画：上、下
    private Animation[] hideAnimations;

    public void setDrawTextListener(OnDrawTextListener drawTextListener) {
        mDrawTextListener = drawTextListener;
    }

    public static DrawTextDialog newInstance() {
        DrawTextDialog dialog = new DrawTextDialog();
        dialog.setStyle(STYLE_NORMAL, R.style.FullScreenDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_draw_text, null);
        drawTextVi = contentView.findViewById(R.id.dtvTextCanvas);
        drawTextVi.setBarSensorListener(new BarSensorListener() {
            @Override
            public boolean isTopToolbarVisible() {
                return topToolbar.getVisibility() == View.VISIBLE;
            }

            @Override
            public void openTools() {
                toggleMenuVisibility(true);
            }

            @Override
            public void closeTools() {
                toggleMenuVisibility(false);
            }
        });

        topToolbar = contentView.findViewById(R.id.vgDrawTextTopMenu);
        downToolbar = contentView.findViewById(R.id.vgDrawTextBottomMenu);

        contentView.findViewById(R.id.btnCancel).setOnClickListener(this);
        contentView.findViewById(R.id.btnOk).setOnClickListener(this);
        contentView.findViewById(R.id.btnTextContent).setOnClickListener(this);
        contentView.findViewById(R.id.btnTextColor).setOnClickListener(this);

        savedCanvas = new Canvas();
        if (savedBitmap != null) {
            //与画布建立联系
            savedCanvas.setBitmap(savedBitmap);
            drawPels();
            drawTextVi.setBitmap(savedBitmap, mCanvasWidth, mCanvasHeight, paintColor);
        }

        return contentView;
    }

    public void setGraffitiInfo(CanvasView cvGraffitiView) {
        this.mCanvasWidth = cvGraffitiView.getCanvasWidth();
        this.mCanvasHeight = cvGraffitiView.getCanvasHeight();
        this.paintColor = cvGraffitiView.getPaintColor();
        //由画布背景创建缓冲位图
        this.savedBitmap = cvGraffitiView.getSavedBitmap().copy(Bitmap.Config.ARGB_8888, true);
        this.pelList = cvGraffitiView.getPelList();
    }

    private void drawPels() {
        // 获取pelList对应的迭代器头结点
        ListIterator<Pel> pelIterator = pelList.listIterator();
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
            } else if (pel.picture != null) {
                Picture picture = pel.picture;
                savedCanvas.save();
                savedCanvas.translate(picture.getTransDx(), picture.getTransDy());
                savedCanvas.scale(picture.getScale(), picture.getScale(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.rotate(picture.getDegree(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.drawBitmap(picture.createContent(getContext()), picture.getBeginPoint().x, picture.getBeginPoint().y, null);
                savedCanvas.restore();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        demandContent();
        DialogHelper.showOnceHintDialog(getContext(), R.string.text_gesture_title, R.string.text_gesture_tip, R.string.got_it, SPKeys.INSTANCE.getSHOW_TEXT_GESTURE_HINT());
    }

    private void demandContent() {
        DialogHelper.showInputDialog(getContext(), getString(R.string.input_text), getString(R.string.finger_graffiti), result -> {
            drawTextVi.setTextContent((String) result);
            drawTextVi.invalidate();
        });
    }

    private void toggleMenuVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        Animation[] animations = getToggleAnimations(isVisible);

        topToolbar.startAnimation(animations[0]);
        downToolbar.startAnimation(animations[1]);

        downToolbar.setVisibility(visibility);
        topToolbar.setVisibility(visibility);
    }

    private Animation[] getToggleAnimations(boolean isVisible) {
        if (isVisible) {
            if (showAnimations == null) {
                showAnimations = new Animation[2];
                showAnimations[0] = AnimationUtils.loadAnimation(getContext(), R.anim.topappear);
                showAnimations[1] = AnimationUtils.loadAnimation(getContext(), R.anim.downappear);
            }
            return showAnimations;
        }
        if (hideAnimations == null) {
            hideAnimations = new Animation[2];
            hideAnimations[0] = AnimationUtils.loadAnimation(getContext(), R.anim.topdisappear);
            hideAnimations[1] = AnimationUtils.loadAnimation(getContext(), R.anim.downdisappear);
        }
        return hideAnimations;
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.btnCancel:
                dismiss();
                break;
            case R.id.btnOk:
                onDrawTextOk(v);
                break;
            case R.id.btnTextContent:
                demandContent();
                break;
            case R.id.btnTextColor:
                DialogHelper.showColorPickerDialog(getActivity(), color -> {
                    paintColor = color;
                    drawTextVi.setTextColor(color);
                });
                break;
        }
    }

    //确定
    public void onDrawTextOk(View v) {
        //构造该次的文本对象,并装入图元对象
        Pel newPel = new Pel();
        newPel.text = drawTextVi.getText(paintColor);

        drawPels();
        if (mDrawTextListener != null) {
            mDrawTextListener.onTextDrew(newPel, savedBitmap);
        }
        //结束该活动
        dismiss();
    }

    public interface OnDrawTextListener {
        void onTextDrew(Pel newPel, Bitmap newBitmap);
    }
}