package com.mthaler.knittings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class GridViewAdapter extends ArrayAdapter<Photo> {
    private final Context context;
    private final int layoutResourceId;
    private final List<Photo> data;

    public GridViewAdapter(Context context, int layoutResourceId, List<Photo> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder h;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            h = new ViewHolder();
            h.imageTitle = row.findViewById(R.id.text);
            h.image = row.findViewById(R.id.image);
            row.setTag(h);
        } else {
            h = (ViewHolder) row.getTag();
        }

        final ViewHolder holder = h;

        final Photo item = data.get(position);
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        final ViewTreeObserver viewTreeObserver = holder.image.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
            holder.image.getViewTreeObserver().removeOnPreDrawListener(this);
            final int width = holder.image.getMeasuredWidth();
            final int height = holder.image.getMeasuredHeight();
            holder.image.setImageBitmap(PictureUtils.getScaledBitmap(item.getFilename().getAbsolutePath(), width, height));
            return true;
            }
        });

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}

