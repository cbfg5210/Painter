package com.ue.graffiti.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hawk on 2018/1/17.
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void update(Object item);
}
