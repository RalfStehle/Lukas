package de.project.lukas.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

public class WrappingGridView extends GridView {
    public WrappingGridView(Context context) {
        super(context);
    }

    public WrappingGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrappingGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);

        // Create a spec that tells the grid it has all available space, it makes calculations based on that.
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightSpec);

        View child = getChildAt(0);
        if (child == null) {
            return;
        }

        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (AbsListView.LayoutParams) generateDefaultLayoutParams();
        }

        // This is copied from the GridView, not sure how it works.
        int childHeightSpec = ViewGroup.getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 0, p.height);

        int childWidthSpec = ViewGroup.getChildMeasureSpec(
                MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.EXACTLY), 0, p.width);

        // Force the first child to be measured. We assume that all children have the same height.
        child.measure(childWidthSpec, childHeightSpec);

        int itemHeight = child.getMeasuredHeight();
        int numItems = getChildCount();
        int numColumns = 2;
        int numRows = numItems / numColumns;

        if (numItems > numColumns && numItems % numColumns != 0) {
            numRows++;
        }

        int totalHeight = (numRows * itemHeight) + (getVerticalSpacing() * (numRows - 1));

        setMeasuredDimension(parentWidth, totalHeight);
    }
}
