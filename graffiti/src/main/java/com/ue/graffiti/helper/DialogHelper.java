package com.ue.graffiti.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ue.graffiti.R;
import com.ue.graffiti.event.OnSingleResultListener;
import com.ue.graffiti.ui.ColorPickerDialog;
import com.ue.graffiti.ui.ColorPickerDialog.OnColorPickerListener;
import com.ue.graffiti.ui.DrawPictureDialog;
import com.ue.graffiti.ui.DrawTextDialog;
import com.ue.graffiti.ui.PenDialog;
import com.ue.graffiti.util.SPUtils;
import com.ue.graffiti.widget.CanvasView;

/**
 * Created by hawk on 2018/1/17.
 */

public class DialogHelper {
    public static void showColorPickerDialog(FragmentActivity activity, OnColorPickerListener colorPickerListener) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = ColorPickerDialog.class.getSimpleName();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        ColorPickerDialog colorPickerDialog = fragment == null ? ColorPickerDialog.newInstance() : (ColorPickerDialog) fragment;
        if (colorPickerDialog.isAdded()) {
            return;
        }
        colorPickerDialog.setColorPickerListener(colorPickerListener);
        colorPickerDialog.show(fragmentManager, tag);
    }

    public static void showPenDialog(FragmentActivity activity, Paint paint) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = PenDialog.class.getSimpleName();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        PenDialog penDialog = fragment == null ? PenDialog.newInstance() : (PenDialog) fragment;
        if (penDialog.isAdded()) {
            return;
        }
        penDialog.setCurrentPaint(paint);
        penDialog.show(fragmentManager, tag);
    }

    public static void showDrawTextDialog(FragmentActivity activity, CanvasView cvGraffitiView, DrawTextDialog.OnDrawTextListener drawTextListener) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = DrawTextDialog.class.getSimpleName();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        DrawTextDialog dialog;
        if (fragment == null) {
            dialog = DrawTextDialog.newInstance();
            dialog.setDrawTextListener(drawTextListener);
        } else {
            dialog = (DrawTextDialog) fragment;
        }
        dialog.setGraffitiInfo(cvGraffitiView);
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(fragmentManager, tag);
    }

    public static void showDrawPictureDialog(FragmentActivity activity, CanvasView cvGraffitiView, DrawPictureDialog.OnDrawPictureListener drawPictureListener) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String tag = DrawPictureDialog.class.getSimpleName();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        DrawPictureDialog dialog;
        if (fragment == null) {
            dialog = DrawPictureDialog.newInstance();
            dialog.setDrawPictureListener(drawPictureListener);
        } else {
            dialog = (DrawPictureDialog) fragment;
        }
        dialog.setGraffitiInfo(cvGraffitiView);
        if (dialog.isAdded()) {
            return;
        }
        dialog.show(fragmentManager, tag);
    }

    public static void showExitDialog(Activity activity, View.OnClickListener saveListener) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.exit)
                .setMessage(R.string.exit_tip)
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(R.string.exit_save, (dialog, which) -> {
                    saveListener.onClick(null);
                })
                .setNeutralButton(R.string.exit_not_save, (dialog, which) -> activity.finish())
                .create()
                .show();
    }

    public static void showInputDialog(Context context, String tip, OnSingleResultListener singleResultListener) {
        showInputDialog(context, tip, null, singleResultListener);
    }

    public static void showInputDialog(Context context, String tip, String defText, OnSingleResultListener singleResultListener) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_input, null);
        final EditText editTxt = contentView.findViewById(R.id.etInput);
        editTxt.setText(defText);
        new AlertDialog.Builder(context)
                .setTitle(tip)
                .setView(contentView)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    singleResultListener.onResult(editTxt.getText().toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    public static void showClearDialog(Context context, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(R.string.ensure_clear)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    public static void showSensorDrawingDialog(Context context, String tip, DialogInterface.OnClickListener okListener) {
        //实例化确认对话框
        new AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    public static void showOnceHintDialog(Context context, int titleRes, int hintRes, int positiveRes, String spKey) {
        boolean showHint = SPUtils.getBoolean(spKey, true);
        if (!showHint) {
            return;
        }
        View checkBoxLayout = LayoutInflater.from(context).inflate(R.layout.layout_check_box, null);
        new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(hintRes)
                .setPositiveButton(positiveRes, (dialog, which) -> {
                    if (((CheckBox) checkBoxLayout.findViewById(R.id.cbCheck)).isChecked()) {
                        SPUtils.putBoolean(spKey, false);
                    }
                })
                .setView(checkBoxLayout)
                .create()
                .show();
    }
}