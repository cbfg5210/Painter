package com.ue.graffiti.event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.ue.graffiti.model.Pel;

import java.util.List;

/**
 * Created by hawk on 2018/1/19.
 */

public interface SimpleTouchListener {
    void invalidate();

    boolean isSensorRegistered();

    Bitmap getBackgroundBitmap();

    Bitmap getCopyOfBackgroundBitmap();

    void setSelectedPel(Pel pel);

    Paint getCurrentPaint();

    void updateSavedBitmap(Canvas canvas, Bitmap bitmap, List<Pel> pelList, Pel selectedPel, boolean isInvalidate);

    Context getContext();
}
