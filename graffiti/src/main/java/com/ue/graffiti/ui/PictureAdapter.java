package com.ue.graffiti.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ue.graffiti.R;
import com.ue.graffiti.model.PictureItem;

import java.util.List;

/**
 * Created by hawk on 2018/1/16.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {
    private List<PictureItem> mPictureItems;
    private OnPictureItemListener mItemClickListener;

    public void setItemClickListener(OnPictureItemListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public PictureAdapter(List<PictureItem> mPictureItems) {
        this.mPictureItems = mPictureItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);
        final ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                int position = holder.getAdapterPosition();
                mItemClickListener.onItemClick(position, mPictureItems.get(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PictureItem item = mPictureItems.get(position);
        holder.tvPicture.setText(item.name);
        holder.tvPicture.setCompoundDrawablesWithIntrinsicBounds(0, item.res, 0, 0);
    }

    @Override
    public int getItemCount() {
        return mPictureItems == null ? 0 : mPictureItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPicture;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPicture = itemView.findViewById(R.id.tvPicture);
        }
    }

    public interface OnPictureItemListener {
        void onItemClick(int position, PictureItem pictureItem);
    }
}