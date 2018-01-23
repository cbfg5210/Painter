package com.ue.graffiti.ui;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ue.graffiti.R;
import com.ue.graffiti.model.PictureItem;

import java.util.ArrayList;
import java.util.List;

public class PictureDialog extends DialogFragment {

    private OnPickPictureListener mPickPictureListener;

    public void setPickPictureListener(OnPickPictureListener pickPictureListener) {
        mPickPictureListener = pickPictureListener;
    }

    public static PictureDialog newInstance() {
        PictureDialog dialog = new PictureDialog();
        dialog.setStyle(STYLE_NO_TITLE, R.style.GraffitiDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_picture, null);

        RecyclerView rvPictures = (RecyclerView) rootView;
        rvPictures.setHasFixedSize(true);

        Resources resources = getResources();
        String[] pictureNameArray = resources.getStringArray(R.array.pictureNameArray);
        TypedArray pictureResArrayTa = resources.obtainTypedArray(R.array.pictureResArray);

        List<PictureItem> pictureItems = new ArrayList<>(pictureNameArray.length);
        for (int i = 0, len = pictureNameArray.length; i < len; i++) {
            pictureItems.add(new PictureItem(pictureNameArray[i], pictureResArrayTa.getResourceId(i, -1)));
        }
        pictureResArrayTa.recycle();

        PictureAdapter adapter = new PictureAdapter(pictureItems);
        adapter.setItemClickListener((position, pictureItem) -> {
            if (mPickPictureListener != null) {
                mPickPictureListener.onPicturePicked(pictureItem.getRes());
            }
            dismiss();
        });
        rvPictures.setAdapter(adapter);

        return rootView;
    }

    public interface OnPickPictureListener {
        void onPicturePicked(int contentId);
    }
}