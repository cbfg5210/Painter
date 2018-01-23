package com.ue.graffiti.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.ue.graffiti.R;
import com.ue.graffiti.constant.SPKeys;
import com.ue.graffiti.util.SPUtils;

//调色板对话框
public class ColorPickerDialog extends DialogFragment {
    //调色板控件相关
    public ColorPicker picker;
    private OnColorPickerListener mColorPickerListener;

    public void setColorPickerListener(OnColorPickerListener colorPickerListener) {
        mColorPickerListener = colorPickerListener;
    }

    public static ColorPickerDialog newInstance() {
        ColorPickerDialog dialog = new ColorPickerDialog();
        //如果在onCreateDialog初始化界面的话设置的主题没有效果
        dialog.setStyle(STYLE_NORMAL, R.style.GraffitiDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_picker, null);
        //找到实例对象
        picker = (ColorPicker) contentView.findViewById(R.id.cpColorPicker);
        OpacityBar opacityBar = (OpacityBar) contentView.findViewById(R.id.obTransparencyBar);
        SaturationBar saturationBar = (SaturationBar) contentView.findViewById(R.id.sbSaturation);
        ValueBar valueBar = (ValueBar) contentView.findViewById(R.id.vbDeepness);
        //使环形取色器和拖动条建立关系
        picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addValueBar(valueBar);

        int lastColor = SPUtils.getInt(SPKeys.SP_PAINT_COLOR, getResources().getColor(R.color.col_298ecb));
        picker.setColor(lastColor);

        contentView.findViewById(R.id.ivPickColor).setOnClickListener(v -> {
            if (mColorPickerListener != null) {
                mColorPickerListener.onColorPicked(picker.getColor());
            }
            dismiss();
        });

        return contentView;
    }

    public interface OnColorPickerListener {
        void onColorPicked(int color);
    }
}