package com.ue.graffiti.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.ue.graffiti.R;
import com.ue.graffiti.constant.DrawPelFlags;
import com.ue.graffiti.constant.SPKeys;
import com.ue.graffiti.helper.DialogHelper;
import com.ue.graffiti.model.DrawPelStep;
import com.ue.graffiti.model.FillPelStep;
import com.ue.graffiti.model.Pel;
import com.ue.graffiti.model.Step;
import com.ue.graffiti.model.TransformPelStep;
import com.ue.graffiti.touch.CrossFillTouch;
import com.ue.graffiti.touch.DrawBesselTouch;
import com.ue.graffiti.touch.DrawBrokenLineTouch;
import com.ue.graffiti.touch.DrawPolygonTouch;
import com.ue.graffiti.touch.KeepDrawingTouch;
import com.ue.graffiti.touch.Touch;
import com.ue.graffiti.touch.TransformTouch;
import com.ue.graffiti.util.SPUtils;
import com.ue.graffiti.util.StepUtils;
import com.ue.graffiti.util.TouchUtils;
import com.ue.graffiti.widget.CanvasView;


public class MainActivity extends RxAppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_NONE = 0;
    protected static final int REQUEST_CODE_GRAPH = 1;//拍照
    protected static final int REQUEST_CODE_PICTURE = 2; //缩放
    protected static final String IMAGE_UNSPECIFIED = "image/*";

    private CanvasView cvGraffitiView;

    public View vgTopMenu;
    private RadioGroup vgBottomMenu;
    private View btnUndo;
    private View btnRedo;
    private Button btnDraw;
    private View vgRightMenu;
    private View ivToggleOptions;
    private View vgEditOptions;

    private Button btnColor;

    private View curToolVi;
    private ImageView curPelVi;
    private ImageView curCanvasBgVi, whiteCanvasBgVi;

    private PointF lastPoint = new PointF();
    private Pel newPel;
    private SensorManager sensorManager;
    private int responseCount;

    private MainPresenter mMainPresenter;

    private Matrix transMatrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private Pel savedPel;
    private Step step;
    private PointF centerPoint = new PointF();
    private int lastEditActionId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainPresenter = new MainPresenter(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        initViews();

        DialogHelper.INSTANCE.showOnceHintDialog(this, R.string.draw_gesture_title, R.string.draw_gesture_tip, R.string.got_it, SPKeys.INSTANCE.getSHOW_DRAW_GESTURE_HINT());
    }

    private void initViews() {
        cvGraffitiView = findViewById(R.id.cvGraffitiView);
        btnDraw = findViewById(R.id.btnDraw);
        vgTopMenu = findViewById(R.id.vgTopMenu);
        vgBottomMenu = findViewById(R.id.vgBottomMenu);
        btnUndo = findViewById(R.id.ivUndo);
        btnRedo = findViewById(R.id.ivRedo);
        vgRightMenu = findViewById(R.id.vgRightMenu);
        ivToggleOptions = findViewById(R.id.ivToggleOptions);
        vgEditOptions = findViewById(R.id.vgEditOptions);
        btnColor = findViewById(R.id.btnColor);

        vgBottomMenu.check(R.id.btnDraw);
        ivToggleOptions.setSelected(true);
        curToolVi = btnDraw;

        int lastColor = SPUtils.getInt(SPKeys.INSTANCE.getSP_PAINT_COLOR(), getResources().getColor(R.color.col_298ecb));
        btnColor.setTextColor(lastColor);
        cvGraffitiView.setPaintColor(lastColor);

        curCanvasBgVi = whiteCanvasBgVi = mMainPresenter.initCanvasBgsPopupWindow(R.layout.popup_canvas_bgs, R.id.vgCanvasBgs, v -> updateCanvasBgAndIcons((ImageView) v));
        curPelVi = mMainPresenter.initPelsPopupWindow(R.layout.popup_pels, R.id.vgPels, cvGraffitiView, (v, pelTouch) -> {
            mMainPresenter.dismissPopupWindows();
            updatePelBarIcons((ImageView) v);
            registerKeepDrawingSensor(pelTouch);
            cvGraffitiView.setTouch(pelTouch);
        });

        setListeners();
    }

    private void setListeners() {
        btnUndo.setOnClickListener(this);
        btnRedo.setOnClickListener(this);

        mMainPresenter.setListenerForChildren(R.id.vgTopMenu, this);
        mMainPresenter.setListenerForChildren(R.id.vgBottomMenu, this);
        mMainPresenter.setListenerForChildren(R.id.vgRightMenu, v -> {
            onSwitchEditMenuAction(v);
        });
        cvGraffitiView.setMultiTouchListener(() -> {
            if (vgTopMenu.getVisibility() == View.VISIBLE) {
                closeTools();
                return;
            }
            ensurePelFinished();
            openTools();
        });
    }

    private void openTools() {
        if (vgRightMenu.getVisibility() == View.VISIBLE) {
            vgRightMenu.setVisibility(View.GONE);
        }
        toggleMenuVisibility(true);
    }

    private void closeTools() {
        mMainPresenter.dismissPopupWindows();
        cvGraffitiView.clearRedoStack();

        toggleMenuVisibility(false);
    }

    private void onOpenPelBarBtn(View v) {
        ensurePelFinished();
        mMainPresenter.showPelsPopupWindow(vgBottomMenu);

        curToolVi = v;

        Touch pelTouch = mMainPresenter.getPelTouchByViewId(curPelVi.getId(), cvGraffitiView);
        registerKeepDrawingSensor(pelTouch);

        cvGraffitiView.setTouch(pelTouch);
    }

    private void registerKeepDrawingSensor(Touch pelTouch) {
        if (!(pelTouch instanceof KeepDrawingTouch)) {
            return;
        }
        ((KeepDrawingTouch) pelTouch).setKeepDrawingListener(new KeepDrawingTouch.KeepDrawingTouchListener() {
            @Override
            public void onDownPoint(PointF downPoint) {
                lastPoint.set(downPoint);
            }

            @Override
            public void registerKeepDrawingSensor() {
                MainActivity.this.registerKeepDrawingSensor();
            }
        });
    }

    private void toggleMenuVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        Animation[] animations = mMainPresenter.getToggleAnimations(isVisible);

        btnRedo.startAnimation(animations[0]);
        vgTopMenu.startAnimation(animations[1]);
        btnUndo.startAnimation(animations[2]);
        vgBottomMenu.startAnimation(animations[3]);

        vgBottomMenu.setVisibility(visibility);
        vgTopMenu.setVisibility(visibility);
        btnUndo.setVisibility(visibility);
        btnRedo.setVisibility(visibility);
    }

    private void onOpenTransBarBtn(View v) {
        mMainPresenter.dismissPopupWindows();
        curToolVi = v;
        closeTools();

        if (curToolVi.getId() == R.id.btnEdit) {
            Animation leftAppearAnim = AnimationUtils.loadAnimation(this, R.anim.leftappear);
            vgRightMenu.setVisibility(View.VISIBLE);
            ivToggleOptions.setVisibility(View.VISIBLE);
            vgRightMenu.startAnimation(leftAppearAnim);
        }

        toggleSensor(false);

        cvGraffitiView.setTouch(new TransformTouch(cvGraffitiView));
    }

    private void onOpenDrawTextBtn(View v) {
        DialogHelper.INSTANCE.showDrawTextDialog(this, cvGraffitiView, (newPel, newBitmap) -> {
            //添加至文本总链表
            cvGraffitiView.addPel(newPel);
            //记录栈中信息
            cvGraffitiView.pushUndoStack(new DrawPelStep(DrawPelFlags.INSTANCE.getDRAW(), cvGraffitiView.getPelList(), newPel));
            //更新画布
            cvGraffitiView.updateSavedBitmap();
        });
    }

    private void onOpenDrawPictureBtn(View v) {
        DialogHelper.INSTANCE.showDrawPictureDialog(this, cvGraffitiView, (newPel, newBitmap) -> {
            //添加至文本总链表
            cvGraffitiView.addPel(newPel);
            //记录栈中信息
            cvGraffitiView.pushUndoStack(new DrawPelStep(DrawPelFlags.INSTANCE.getDRAW(), cvGraffitiView.getPelList(), newPel));
            //更新画布
            cvGraffitiView.updateSavedBitmap();
        });
    }

    private void onCopyPelClick(Pel selectedPel) {
        Pel pel = (Pel) selectedPel.clone();
        pel.path.offset(10, 10);
        pel.region.setPath(pel.path, cvGraffitiView.getClipRegion());

        cvGraffitiView.addPel(pel);
        cvGraffitiView.pushUndoStack(new DrawPelStep(DrawPelFlags.INSTANCE.getCOPY(), cvGraffitiView.getPelList(), pel));

        cvGraffitiView.setSelectedPel(null);
        cvGraffitiView.updateSavedBitmap();
    }

    private void onDeletePelClick(Pel selectedPel) {
        cvGraffitiView.pushUndoStack(new DrawPelStep(DrawPelFlags.INSTANCE.getDELETE(), cvGraffitiView.getPelList(), selectedPel));
        cvGraffitiView.removePel(selectedPel);

        cvGraffitiView.setSelectedPel(null);
        cvGraffitiView.updateSavedBitmap();
    }

    private void updatePelBarIcons(ImageView v) {
        curPelVi.setImageDrawable(null);
        v.setImageResource(R.drawable.bg_highlight_frame);
        curPelVi = v;

        Drawable fatherDrawable = getResources().getDrawable(mMainPresenter.getDrawRes(v.getId()));
        btnDraw.setCompoundDrawablesWithIntrinsicBounds(null, fatherDrawable, null, null);
    }

    private void updateCanvasBgAndIcons(View v) {
        curCanvasBgVi.setImageDrawable(null);
        curCanvasBgVi = (ImageView) v;
        curCanvasBgVi.setImageResource(R.drawable.bg_highlight_frame);

        int backgroundDrawable = mMainPresenter.getBgSelectedRes(v.getId());
        if (backgroundDrawable != 0) {
            cvGraffitiView.setBackgroundBitmap(backgroundDrawable);
        }
    }

    //确保未画完的图元能够真正敲定
    private void ensurePelFinished() {
        Pel selectedPel = cvGraffitiView.getSelectedPel();
        if (selectedPel == null) {
            return;
        }
        Touch touch = cvGraffitiView.getTouch();
        if (touch instanceof DrawBesselTouch) {
            touch.control = true;
            touch.up();
            return;
        }
        if (touch instanceof DrawBrokenLineTouch) {
            ((DrawBrokenLineTouch) touch).hasFinished = true;
            touch.up();
            return;
        }
        if (touch instanceof DrawPolygonTouch) {
            touch.curPoint.set(touch.beginPoint);
            touch.up();
            return;
        }
        cvGraffitiView.setSelectedPel(null);
        cvGraffitiView.updateSavedBitmap();
    }

    private void completeKeepDrawing() {
        toggleSensor(false);

        newPel.region.setPath(newPel.path, cvGraffitiView.getClipRegion());
        newPel.paint.set(cvGraffitiView.getCurrentPaint());

        cvGraffitiView.addPel(newPel);

        cvGraffitiView.pushUndoStack(new DrawPelStep(DrawPelFlags.INSTANCE.getDRAW(), cvGraffitiView.getPelList(), newPel));

        cvGraffitiView.setSelectedPel(null);
        cvGraffitiView.updateSavedBitmap();
    }

    private void registerKeepDrawingSensor() {
        newPel = new Pel();
        newPel.closure = true;
        lastPoint.set(cvGraffitiView.getTouch().curPoint);
        newPel.path.moveTo(lastPoint.x, lastPoint.y);

        toggleSensor(true);
    }

    private void toggleSensor(boolean open) {
        if (open) {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(singleHandSensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            sensorManager.unregisterListener(singleHandSensorEventListener);
        }
        cvGraffitiView.setSensorRegistered(open);
    }

    //单手操作传感器监听者
    private SensorEventListener singleHandSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (curToolVi.getId() != R.id.btnDraw) {
                return;
            }
            //单手加速度作图
            if (responseCount % 2 == 0) {
                float dx = -event.values[0];
                float dy = event.values[1];
                PointF nowPoint = new PointF(lastPoint.x + dx, lastPoint.y + dy);

                newPel.path.quadTo(lastPoint.x, lastPoint.y, (lastPoint.x + nowPoint.x) / 2, (lastPoint.y + nowPoint.y) / 2);
                lastPoint.set(nowPoint);

                cvGraffitiView.setSelectedPel(newPel);
                cvGraffitiView.invalidate();
            }
            responseCount++;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void onUndoBtn(View v) {
        final Step step = cvGraffitiView.popUndoStack();
        if (step == null) {
            return;
        }

        cvGraffitiView.getTouch().setProcessing(true, getString(R.string.undoing));
        StepUtils.toRedoUpdate(this, step, cvGraffitiView.getBackgroundBitmap(), cvGraffitiView.getCopyOfBackgroundBitmap(), () -> {
            if (step instanceof TransformPelStep) {
                cvGraffitiView.setSelectedPel(null);
            }
            //重绘位图
            cvGraffitiView.updateSavedBitmap();
            cvGraffitiView.pushRedoStack(step);

            cvGraffitiView.getTouch().setProcessing(false, null);
        });
    }

    private void onRedoBtn(View v) {
        Step step = cvGraffitiView.popRedoStack();
        if (step == null) {
            return;
        }
        cvGraffitiView.getTouch().setProcessing(true, getString(R.string.redoing));
        StepUtils.toUndoUpdate(this, step, cvGraffitiView.getBackgroundBitmap(), () -> {
            //重绘位图
            cvGraffitiView.updateSavedBitmap();
            cvGraffitiView.getTouch().setProcessing(false, null);
        });
        cvGraffitiView.pushUndoStack(step);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE_NONE) {
            return;
        }
        // 触发拍照模式的“确定”、“取消”、“返键”按钮后
        if (requestCode == REQUEST_CODE_GRAPH) {
            cvGraffitiView.getTouch().setProcessing(true, getString(R.string.loading));
            mMainPresenter.loadCapturePhoto()
                    .subscribe(bitmap -> {
                        cvGraffitiView.getTouch().setProcessing(false, null);
                        cvGraffitiView.setBackgroundBitmap(bitmap);
                    });
            return;
        }
        // 触发图库模式的“选择”、“返键”按钮后
        if (requestCode == REQUEST_CODE_PICTURE) {
            cvGraffitiView.getTouch().setProcessing(true, getString(R.string.loading));
            mMainPresenter.loadPictureFromIntent(data, cvGraffitiView.getCanvasWidth(), cvGraffitiView.getCanvasHeight())
                    .subscribe(bitmap -> {
                        cvGraffitiView.getTouch().setProcessing(false, null);
                        cvGraffitiView.setBackgroundBitmap(bitmap);
                    });
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        DialogHelper.INSTANCE.showExitDialog(this,
                v -> DialogHelper.INSTANCE.showInputDialog(this, getString(R.string.input_graffiti_name),
                        result -> mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.getSavedBitmap(), (String) result, vi -> finish())));
    }

    @Override
    public void onClick(View v) {
        if (!prepareToggleSwitchMenuAction(v)) {
            return;
        }
        int viewId = v.getId();
        switch (viewId) {
            /*
            * top menu listener
            * */
            case R.id.btnColor:
                DialogHelper.INSTANCE.showColorPickerDialog(MainActivity.this, color -> {
                    SPUtils.putInt(SPKeys.INSTANCE.getSP_PAINT_COLOR(), color);
                    btnColor.setTextColor(color);
                    cvGraffitiView.setPaintColor(color);
                });
                break;
            case R.id.btnPen:
                DialogHelper.INSTANCE.showPenDialog(MainActivity.this, cvGraffitiView.getCurrentPaint());
                break;
            case R.id.btnClear:
                DialogHelper.INSTANCE.showClearDialog(this, (dialog, which) -> {
                    //清空内部所有数据
                    cvGraffitiView.clearData();
                    //画布背景图标复位
                    updateCanvasBgAndIcons(whiteCanvasBgVi);
                    //清除填充过颜色的地方
                    cvGraffitiView.setBackgroundBitmap();
                });
                break;
            case R.id.btnSave:
                DialogHelper.INSTANCE.showInputDialog(this, getString(R.string.input_graffiti_name),
                        result -> mMainPresenter.onSaveGraffitiClicked(cvGraffitiView.getSavedBitmap(), (String) result, null));
                break;
            /*
            * bottom menu listener
            * */
            case R.id.btnDraw:
                onOpenPelBarBtn(v);
                break;
            case R.id.btnEdit:
                onOpenTransBarBtn(v);
                break;
            case R.id.btnFill:
                curToolVi = v;
                cvGraffitiView.setTouch(new CrossFillTouch(cvGraffitiView));
                break;
            case R.id.btnBg:
                mMainPresenter.showCanvasBgsPopupWindow(vgBottomMenu);
                break;
            case R.id.btnText:
                onOpenDrawTextBtn(v);
                break;
            case R.id.btnInsertPicture:
                onOpenDrawPictureBtn(v);
                break;
            /**
             * other listener:undo,redo,extend
             */
            case R.id.ivUndo:
                onUndoBtn(v);
                break;
            case R.id.ivRedo:
                onRedoBtn(v);
                break;
        }
    }

    private void onSwitchEditMenuAction(View view) {
        if (!prepareToggleSwitchMenuAction(view)) {
            return;
        }
        int viewId = view.getId();
        if (viewId == R.id.ivToggleOptions) {
            onOpenTransChildren();
            return;
        }
        Pel selectedPel = cvGraffitiView.getSelectedPel();
        if (selectedPel == null) {
            Toast.makeText(MainActivity.this, R.string.select_pel_first, Toast.LENGTH_LONG).show();
            return;
        }
        switch (viewId) {
            case R.id.ivDelete:
                onDeletePelClick(selectedPel);
                break;
            case R.id.ivCopy:
                onCopyPelClick(selectedPel);
                break;
            case R.id.ivRotate:
            case R.id.ivZoomIn:
            case R.id.ivZoomOut:
                onSwitchRotateZoom(viewId, selectedPel);
                break;
            case R.id.ivFill:
                onFillPelClick(selectedPel);
                break;
        }
    }

    private void onSwitchRotateZoom(int viewId, Pel selectedPel) {
        savedPel = new Pel();
        savedPel.path.set(selectedPel.path);
        savedMatrix.set(TouchUtils.calPelSavedMatrix(savedPel));

        if (lastEditActionId == 0) {
            lastEditActionId = viewId;
        }
        if (lastEditActionId != viewId) {
            step = new TransformPelStep(cvGraffitiView.getPelList(), cvGraffitiView.getClipRegion(), selectedPel);
            selectedPel.region.setPath(selectedPel.path, cvGraffitiView.getClipRegion());
            ((TransformPelStep) step).setToUndoMatrix(transMatrix);
            cvGraffitiView.pushUndoStack(step);
            cvGraffitiView.updateSavedBitmap();

            lastEditActionId = viewId;
        }

        switch (viewId) {
            case R.id.ivRotate:
                centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel));
                transMatrix.set(savedMatrix);
                transMatrix.setRotate(10, centerPoint.x, centerPoint.y);
                break;
            case R.id.ivZoomIn:
                centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel));
                transMatrix.set(savedMatrix);
                transMatrix.postScale(1.1f, 1.1f, centerPoint.x, centerPoint.y);
                break;
            case R.id.ivZoomOut:
                centerPoint.set(TouchUtils.calPelCenterPoint(selectedPel));
                transMatrix.set(savedMatrix);
                transMatrix.postScale(0.9f, 0.9f, centerPoint.x, centerPoint.y);
                break;
        }
        selectedPel.path.set(savedPel.path);
        selectedPel.path.transform(transMatrix);

        cvGraffitiView.invalidate();
    }

    private void onFillPelClick(Pel selectedPel) {
        Paint oldPaint = new Paint(selectedPel.paint);
        selectedPel.paint.set(cvGraffitiView.getCurrentPaint());
        selectedPel.paint.setStyle(selectedPel.closure ? Paint.Style.FILL : Paint.Style.STROKE);

        Paint newPaint = new Paint(selectedPel.paint);
        cvGraffitiView.pushUndoStack(new FillPelStep(cvGraffitiView.getPelList(), selectedPel, oldPaint, newPaint));

        cvGraffitiView.setSelectedPel(null);
        cvGraffitiView.updateSavedBitmap();
    }

    private void onOpenTransChildren() {
        boolean isEditOptionsVisible = vgEditOptions.getVisibility() == View.GONE;
        ivToggleOptions.setSelected(isEditOptionsVisible);
        vgEditOptions.setVisibility(isEditOptionsVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 在显示/隐藏/切换菜单项前的处理
     *
     * @return false:prepare failed;true:prepare ok
     */
    private boolean prepareToggleSwitchMenuAction(View view) {
        if (cvGraffitiView.getTouch().isProcessing()) {
            Toast.makeText(this, getString(R.string.task_processing), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvGraffitiView.isSensorRegistered()) {
            //结束sensor绘图
            completeKeepDrawing();
        }
        int viewId = view.getId();
        if (viewId != R.id.btnDraw && viewId != R.id.btnBg) {
            //点击的不是弹窗按钮则隐藏弹窗
            mMainPresenter.dismissPopupWindows();
        }
        return true;
    }
}