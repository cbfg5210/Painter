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

public class DrawPictureDialog extends DialogFragment implements View.OnClickListener {
    private List<Pel> pelList;
    // 当前重绘位图
    private Bitmap savedBitmap;
    //重绘画布
    private Canvas savedCanvas;
    private TextImageView drawPictureVi;
    private View topToolbar;
    private View downToolbar;
    //调色板对话框
    private PictureDialog pictureDialog;

    private OnDrawPictureListener mDrawPictureListener;
    private int canvasWidth;
    private int canvasHeight;

    public void setDrawPictureListener(OnDrawPictureListener drawPictureListener) {
        mDrawPictureListener = drawPictureListener;
    }

    public static DrawPictureDialog newInstance() {
        DrawPictureDialog dialog = new DrawPictureDialog();
        dialog.setStyle(STYLE_NORMAL, R.style.FullScreenDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_draw_picture, null);
        drawPictureVi = contentView.findViewById(R.id.drawpicture_canvas);
        drawPictureVi.setBarSensorListener(new BarSensorListener() {
            @Override
            public boolean isTopToolbarVisible() {
                return topToolbar.getVisibility() == View.VISIBLE;
            }

            @Override
            public void openTools() {
                DrawPictureDialog.this.openTools();
            }

            @Override
            public void closeTools() {
                DrawPictureDialog.this.closeTools();
            }
        });

        pictureDialog = PictureDialog.newInstance();
        pictureDialog.setPickPictureListener(contentId -> {
            //重置插图位置、缩放、旋转信息
            drawPictureVi.getTouch().clear();
            //传入插图信息
            drawPictureVi.setContentAndCenterPoint(contentId);
            drawPictureVi.invalidate();
        });

        topToolbar = contentView.findViewById(R.id.drawpicture_toptoolbar);
        downToolbar = contentView.findViewById(R.id.drawpicture_downtoolbar);

        contentView.findViewById(R.id.drawpicture_refuse).setOnClickListener(this);
        contentView.findViewById(R.id.drawpicture_sure).setOnClickListener(this);
        contentView.findViewById(R.id.drawpicture_select).setOnClickListener(this);

        savedCanvas = new Canvas();
        if (savedBitmap != null) {
            savedCanvas.setBitmap(savedBitmap); //与画布建立联系
            drawPels();

            drawPictureVi.setBitmap(savedBitmap, canvasWidth, canvasHeight);
        }
        return contentView;
    }

    public void setGraffitiInfo(CanvasView cvGraffitiView) {
        this.canvasWidth = cvGraffitiView.getCanvasWidth();
        this.canvasHeight = cvGraffitiView.getCanvasHeight();
        this.savedBitmap = cvGraffitiView.getSavedBitmap().copy(Bitmap.Config.ARGB_8888, true);//由画布背景创建缓冲位图
        this.pelList = cvGraffitiView.getPelList();
    }

    public void drawPels() {
        // 获取pelList对应的迭代器头结点
        ListIterator<Pel> pelIterator = pelList.listIterator();
        while (pelIterator.hasNext()) {
            Pel pel = pelIterator.next();

            //若是文本图元
            if (pel.getText() != null) {
                Text text = pel.getText();
                savedCanvas.save();
                savedCanvas.translate(text.getTransDx(), text.getTransDy());
                savedCanvas.scale(text.getScale(), text.getScale(), text.getCenterPoint().x, text.getCenterPoint().y);
                savedCanvas.rotate(text.getDegree(), text.getCenterPoint().x, text.getCenterPoint().y);
                savedCanvas.drawText(text.getContent(), text.getBeginPoint().x, text.getBeginPoint().y, text.getPaint());
                savedCanvas.restore();
            } else if (pel.getPicture() != null) {
                Picture picture = pel.getPicture();
                savedCanvas.save();
                savedCanvas.translate(picture.getTransDx(), picture.getTransDy());
                savedCanvas.scale(picture.getScale(), picture.getScale(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.rotate(picture.getDegree(), picture.getCenterPoint().x, picture.getCenterPoint().y);
                savedCanvas.drawBitmap(picture.createContent(getContext()), picture.getBeginPoint().x, picture.getBeginPoint().y, null);
                savedCanvas.restore();
            }
//            else if (!pel.equals(selectedPel))//若非选中的图元
//                savedCanvas.drawPath(pel.getPath(), pel.getPaint());
        }
    }

    //关闭工具箱
    private void closeTools() {
        Animation downDisappearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.downdisappear);
        Animation topDisappearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.topdisappear);

        downToolbar.startAnimation(downDisappearAnim);
        topToolbar.startAnimation(topDisappearAnim);

        downToolbar.setVisibility(View.GONE);
        topToolbar.setVisibility(View.GONE);
    }

    //打开工具箱
    private void openTools() {
        Animation downAppearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.downappear);
        Animation topAppearAnim = AnimationUtils.loadAnimation(getContext(), R.anim.topappear);

        downToolbar.startAnimation(downAppearAnim);
        topToolbar.startAnimation(topAppearAnim);

        downToolbar.setVisibility(View.VISIBLE);
        topToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.drawpicture_refuse:
                dismiss();
                break;
            case R.id.drawpicture_sure:
                onDrawPictureOkBtn(v);
                break;
            case R.id.drawpicture_select:
                pictureDialog.show(getChildFragmentManager(), "");
                break;
        }
    }

    public void onDrawPictureOkBtn(View v) {
        //插入了图
        if (drawPictureVi.getImageContent() != null) {
            Pel newPel = new Pel();
            newPel.setPicture(drawPictureVi.getPicture());

            drawPels();
            if (mDrawPictureListener != null) {
                mDrawPictureListener.onPictureDrew(newPel, savedBitmap);
            }
        }
        //结束该活动
        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        DialogHelper.INSTANCE.showOnceHintDialog(getContext(), R.string.image_gesture_title, R.string.image_gesture_tip, R.string.got_it, SPKeys.INSTANCE.getSHOW_IMAGE_GESTURE_HINT());
    }

    public interface OnDrawPictureListener {
        void onPictureDrew(Pel newPel, Bitmap newBitmap);
    }
}