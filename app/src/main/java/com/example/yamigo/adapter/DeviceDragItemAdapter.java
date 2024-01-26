package com.example.yamigo.adapter;

import androidx.recyclerview.widget.RecyclerView;

public interface DeviceDragItemAdapter {
    void onItemMove(RecyclerView.ViewHolder source, RecyclerView.ViewHolder target);

    void onItemSelect(RecyclerView.ViewHolder source);

    void onItemClear(RecyclerView.ViewHolder source);
}
