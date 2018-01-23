package com.ue.graffiti.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.trello.rxlifecycle2.android.ActivityEvent;
import com.ue.graffiti.R;
import com.ue.graffiti.touch.DrawBesselTouch;
import com.ue.graffiti.touch.DrawBrokenLineTouch;
import com.ue.graffiti.touch.DrawFreehandTouch;
import com.ue.graffiti.touch.DrawLineTouch;
import com.ue.graffiti.touch.DrawOvalTouch;
import com.ue.graffiti.touch.DrawPolygonTouch;
import com.ue.graffiti.touch.DrawRectTouch;
import com.ue.graffiti.touch.KeepDrawingTouch;
import com.ue.graffiti.touch.Touch;
import com.ue.graffiti.widget.CanvasView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hawk on 2018/1/16.
 */

public class MainPresenter {
    private static final String BASE_PATH = "/painter/graffiti";

    private MainActivity mMainActivity;
    private PopupWindow pelsPopupWindow;
    private PopupWindow canvasBgsPopupWindow;
    //四个方向的显示动画：左、上、右、下
    private Animation[] showAnimations;
    //四个方向的隐藏动画：左、上、右、下
    private Animation[] hideAnimations;

    public MainPresenter(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    public Animation[] getToggleAnimations(boolean isShown) {
        if (isShown) {
            if (showAnimations == null) {
                showAnimations = new Animation[4];
                showAnimations[0] = AnimationUtils.loadAnimation(mMainActivity, R.anim.leftappear);
                showAnimations[1] = AnimationUtils.loadAnimation(mMainActivity, R.anim.topappear);
                showAnimations[2] = AnimationUtils.loadAnimation(mMainActivity, R.anim.rightappear);
                showAnimations[3] = AnimationUtils.loadAnimation(mMainActivity, R.anim.downappear);
            }
            return showAnimations;
        }
        if (hideAnimations == null) {
            hideAnimations = new Animation[4];
            hideAnimations[0] = AnimationUtils.loadAnimation(mMainActivity, R.anim.leftdisappear);
            hideAnimations[1] = AnimationUtils.loadAnimation(mMainActivity, R.anim.topdisappear);
            hideAnimations[2] = AnimationUtils.loadAnimation(mMainActivity, R.anim.rightdisappear);
            hideAnimations[3] = AnimationUtils.loadAnimation(mMainActivity, R.anim.downdisappear);
        }
        return hideAnimations;
    }

    public void capturePhoto(int REQUEST_CODE_GRAPH) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg")));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name());
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, Configuration.ORIENTATION_LANDSCAPE);
        mMainActivity.startActivityForResult(intent, REQUEST_CODE_GRAPH);
    }

    public void showPelsPopupWindow(View vgBottomMenu) {
        showPopupWindow(canvasBgsPopupWindow, pelsPopupWindow, vgBottomMenu);
    }

    public void showCanvasBgsPopupWindow(View vgBottomMenu) {
        showPopupWindow(pelsPopupWindow, canvasBgsPopupWindow, vgBottomMenu);
    }

    public void dismissPopupWindows() {
        canvasBgsPopupWindow.dismiss();
        pelsPopupWindow.dismiss();
    }

    private void showPopupWindow(PopupWindow dismissPopup, PopupWindow showPopup, View vgBottomMenu) {
        dismissPopup.dismiss();
        if (showPopup.isShowing()) {
            showPopup.dismiss();
            return;
        }
        showPopup.showAtLocation(vgBottomMenu, Gravity.BOTTOM, 0, vgBottomMenu.getHeight());
    }

    public ImageView initPelsPopupWindow(int layoutRes, int groupId, CanvasView cvGraffitiView, OnPickPelListener pickPelListener) {
        View layoutView = mMainActivity.getLayoutInflater().inflate(layoutRes, null);
        pelsPopupWindow = initPopupWindow(layoutView, groupId, v -> {
            pickPelListener.onPelPick(v, getPelTouchByViewId(v.getId(), cvGraffitiView));
        });
        return layoutView.findViewById(R.id.ivFreehand);
    }

    public Touch getPelTouchByViewId(int viewId, CanvasView cvGraffitiView) {
        switch (viewId) {
            case R.id.ivFreehand:
                return new DrawFreehandTouch(cvGraffitiView);
            case R.id.ivRect:
                return new DrawRectTouch(cvGraffitiView);
            case R.id.ivBessel:
                return new DrawBesselTouch(cvGraffitiView);
            case R.id.ivOval:
                return new DrawOvalTouch(cvGraffitiView);
            case R.id.ivLine:
                return new DrawLineTouch(cvGraffitiView);
            case R.id.ivBrokenLine:
                return new DrawBrokenLineTouch(cvGraffitiView);
            case R.id.ivPolygon:
                return new DrawPolygonTouch(cvGraffitiView);
            case R.id.ivKeepDrawing:
                return new KeepDrawingTouch(cvGraffitiView);
            default:
                return null;
        }
    }

    public ImageView initCanvasBgsPopupWindow(int layoutRes, int groupId, View.OnClickListener clickListener) {
        View layoutView = mMainActivity.getLayoutInflater().inflate(layoutRes, null);
        canvasBgsPopupWindow = initPopupWindow(layoutView, groupId, v -> {
            clickListener.onClick(v);

            if (v.getId() == R.id.btnCanvasBg8) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MainActivity.IMAGE_UNSPECIFIED);
                mMainActivity.startActivityForResult(intent, MainActivity.REQUEST_CODE_PICTURE);
                return;
            }
            if (v.getId() == R.id.btnCanvasBg9) {
                capturePhoto(MainActivity.REQUEST_CODE_GRAPH);
                return;
            }
        });
        return layoutView.findViewById(R.id.btnCanvasBg0);
    }

    private PopupWindow initPopupWindow(View layoutView, int groupId, View.OnClickListener clickListener) {
        ViewGroup viewGroup = layoutView.findViewById(groupId);
        for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
            viewGroup.getChildAt(i).setOnClickListener(clickListener);
        }
        return new PopupWindow(layoutView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public int getDrawRes(int viewId) {
        switch (viewId) {
            case R.id.ivBessel:
                return R.drawable.sel_pel_bessel;
            case R.id.ivBrokenLine:
                return R.drawable.sel_pel_broken_line;
            case R.id.ivFreehand:
                return R.drawable.sel_pel_free_hand;
            case R.id.ivLine:
                return R.drawable.sel_pel_line;
            case R.id.ivOval:
                return R.drawable.sel_pel_oval;
            case R.id.ivPolygon:
                return R.drawable.sel_pel_polygon;
            case R.id.ivRect:
                return R.drawable.sel_pel_rect;
            case R.id.ivKeepDrawing:
                return R.drawable.sel_pel_keep_drawing;
            default:
                return 0;
        }
    }

    public int getBgSelectedRes(int viewId) {
        switch (viewId) {
            case R.id.btnCanvasBg0:
                return R.drawable.bg_canvas0;
            case R.id.btnCanvasBg1:
                return R.drawable.bg_canvas1;
            case R.id.btnCanvasBg2:
                return R.drawable.bg_canvas2;
            case R.id.btnCanvasBg3:
                return R.drawable.bg_canvas3;
            case R.id.btnCanvasBg4:
                return R.drawable.bg_canvas4;
            case R.id.btnCanvasBg5:
                return R.drawable.bg_canvas5;
            case R.id.btnCanvasBg6:
                return R.drawable.bg_canvas6;
            case R.id.btnCanvasBg7:
                return R.drawable.bg_canvas7;
            default:
                return 0;
        }
    }

    public void onSaveGraffitiClicked(final Bitmap savedBitmap, final String workName, View.OnClickListener saveListener) {
        if (TextUtils.isEmpty(workName)) {
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_error_null), Toast.LENGTH_SHORT).show();
            return;
        }
        String path = Environment.getExternalStorageDirectory().getPath() + BASE_PATH;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String savedPath = path + "/" + workName + ".png";
        file = new File(savedPath);
        if (!file.exists()) {
            saveGraffiti(savedBitmap, savedPath, saveListener);
            return;
        }
        //询问用户是否覆盖提示框
        new AlertDialog.Builder(mMainActivity)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(R.string.name_conflict)
                .setPositiveButton(R.string.cover, (dialog, which) -> saveGraffiti(savedBitmap, savedPath, saveListener))
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void saveGraffiti(Bitmap bitmap, String savedPath, View.OnClickListener saveListener) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(savedPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_to, savedPath), Toast.LENGTH_SHORT).show();
            if (saveListener != null) {
                saveListener.onClick(null);
            }
        } catch (Exception e) {
            Toast.makeText(mMainActivity, mMainActivity.getString(R.string.save_error, e.getMessage()), Toast.LENGTH_SHORT).show();
        } finally {
            if (fileOutputStream == null) {
                return;
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Observable<Bitmap> loadPictureFromIntent(final Intent data, final int canvasWidth, final int canvasHeight) {
        return Observable
                .create((ObservableOnSubscribe<Bitmap>) e -> {
                    try {
                        Uri uri = data.getData();
                        File file = new File(uri.getPath());
                        FileInputStream fis = new FileInputStream(file);
                        Bitmap pic = BitmapFactory.decodeStream(fis);
                        fis.close();

                        e.onNext(pic);

                    } catch (Exception exp) {
                        Uri uri = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = mMainActivity.getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        BitmapFactory.Options op = new BitmapFactory.Options();
                        op.inJustDecodeBounds = true;
                        Bitmap pic = BitmapFactory.decodeFile(picturePath, op);
                        int xScale = op.outWidth / canvasWidth;
                        int yScale = op.outHeight / canvasHeight;
                        op.inSampleSize = xScale > yScale ? xScale : yScale;
                        op.inJustDecodeBounds = false;
                        pic = BitmapFactory.decodeFile(picturePath, op);

                        e.onNext(pic);
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mMainActivity.bindUntilEvent(ActivityEvent.DESTROY));
    }

    public Observable<Bitmap> loadCapturePhoto() {
        return Observable
                .create((ObservableOnSubscribe<Bitmap>) e -> {
                    try {
                        //获取图片
                        File file = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
                        FileInputStream fis = new FileInputStream(file);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = false;
                        opts.inSampleSize = 2;

                        e.onNext(BitmapFactory.decodeStream(fis, null, opts));
                        e.onComplete();

                        file.delete();
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                    e.onComplete();
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mMainActivity.bindUntilEvent(ActivityEvent.DESTROY));
    }

    public void setListenerForChildren(int parentId, View.OnClickListener listener) {
        ViewGroup viewGroup = mMainActivity.findViewById(parentId);
        View child;
        for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
            child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                setListenerForChildren(child.getId(), listener);
            } else {
                viewGroup.getChildAt(i).setOnClickListener(listener);
            }
        }
    }

    public interface OnPickPelListener {
        void onPelPick(View v, Touch pelTouch);
    }
}
