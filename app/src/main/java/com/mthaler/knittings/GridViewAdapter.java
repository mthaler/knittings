package com.mthaler.knittings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.text);
            holder.image = row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Photo item = data.get(position);
        holder.image.setImageBitmap(PictureUtils.getScaledBitmap(item.getFilename().getAbsolutePath(), 400, 400));
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}

