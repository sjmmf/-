package com.example.yamigo.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
    private int mBottomOffset;

    public BottomOffsetDecoration(int mBottomOffset) {
        this.mBottomOffset = mBottomOffset;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int item_count = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);
        if (item_count > 0 && position == item_count - 1) {
            outRect.set(0, 0, 0, mBottomOffset);
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
