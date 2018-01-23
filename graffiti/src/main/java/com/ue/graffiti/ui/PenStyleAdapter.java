package com.ue.graffiti.ui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ue.adapterdelegate.BaseAdapterDelegate;
import com.ue.adapterdelegate.DelegationAdapter;
import com.ue.adapterdelegate.Item;
import com.ue.adapterdelegate.OnDelegateClickListener;
import com.ue.graffiti.R;
import com.ue.graffiti.base.BaseViewHolder;
import com.ue.graffiti.model.PenCatTitleItem;
import com.ue.graffiti.model.PenShapeItem;

import java.util.List;

/**
 * Created by hawk on 2018/1/17.
 */
class PenStyleAdapter extends DelegationAdapter<Item> implements OnDelegateClickListener {

    private OnDelegateClickListener mDelegateClickListener;
    private int lastShapeIndex;
    private int lastEffectIndex;

    public void setDelegateClickListener(OnDelegateClickListener delegateClickListener) {
        mDelegateClickListener = delegateClickListener;
    }

    public int getSpanCount(int position) {
        return (getItems().get(position) instanceof PenCatTitleItem) ? 4 : 1;
    }

    PenStyleAdapter(Activity activity, List<Item> items) {
        if (items != null) {
            for (int i = 0, len = items.size(); i < len; i++) {
                Item item = items.get(i);
                if (!(item instanceof PenShapeItem)) {
                    continue;
                }
                PenShapeItem shapeItem = (PenShapeItem) item;
                if (!shapeItem.isChecked) {
                    continue;
                }
                if (shapeItem.flag == PenDialog.FLAG_SHAPE) {
                    lastShapeIndex = i;
                } else {
                    lastEffectIndex = i;
                }
            }

            getItems().addAll(items);
        }

        PenItemDelegate delegate = new PenItemDelegate(activity);
        delegate.setOnDelegateClickListener(this);
        this.addDelegate(delegate);

        addDelegate(new PenTitleItemDelegate(activity));
    }

    @Override
    public void onClick(View view, int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }

        PenShapeItem item = (PenShapeItem) getItems().get(position);
        if (item.isChecked) {
            return;
        }
        if (item.flag == PenDialog.FLAG_SHAPE) {
            ((PenShapeItem) getItems().get(lastShapeIndex)).isChecked = false;
            notifyItemChanged(lastShapeIndex, 0);
            lastShapeIndex = position;
        } else {
            ((PenShapeItem) getItems().get(lastEffectIndex)).isChecked = false;
            notifyItemChanged(lastEffectIndex, 0);
            lastEffectIndex = position;
        }
        item.isChecked = true;
        notifyItemChanged(position, 0);

        if (mDelegateClickListener != null) {
            mDelegateClickListener.onClick(view, position);
        }
    }

    private static class PenTitleItemDelegate extends BaseAdapterDelegate<Item> {
        public PenTitleItemDelegate(Activity activity) {
            super(activity, R.layout.item_pen_cat_title);
        }

        @NonNull
        @Override
        protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
            return new ViewHolder(itemView);
        }

        @Override
        public boolean isForViewType(@NonNull Item item) {
            return item instanceof PenCatTitleItem;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
            ((BaseViewHolder) holder).update(item);
        }

        class ViewHolder extends BaseViewHolder {
            TextView tvPenCatTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                tvPenCatTitle = itemView.findViewById(R.id.tvPenCatTitle);
            }

            public void update(Object aItem) {
                PenCatTitleItem item = (PenCatTitleItem) aItem;
                tvPenCatTitle.setText(item.title);
            }
        }
    }

    private static class PenItemDelegate extends BaseAdapterDelegate<Item> {
        public PenItemDelegate(Activity activity) {
            super(activity, R.layout.item_pen_shape);
        }

        @NonNull
        @Override
        protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull View itemView) {
            return new ViewHolder(itemView);
        }

        @Override
        public boolean isForViewType(@NonNull Item item) {
            return item instanceof PenShapeItem;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @NonNull Item item, @NonNull List payloads) {
            ((BaseViewHolder) holder).update(item);
        }

        class ViewHolder extends BaseViewHolder {
            RadioButton rbShapeName;

            public ViewHolder(View itemView) {
                super(itemView);
                rbShapeName = itemView.findViewById(R.id.rbShapeName);
            }

            public void update(Object aItem) {
                PenShapeItem item = (PenShapeItem) aItem;
                rbShapeName.setText(item.name);
                rbShapeName.setCompoundDrawablesWithIntrinsicBounds(0, item.image, 0, 0);
                rbShapeName.setChecked(item.isChecked);
            }
        }
    }
}