package com.mthaler.knittings.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.mthaler.knittings.R;
import com.mthaler.knittings.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final View.OnClickListener onItemClick;
    private final View.OnLongClickListener onItemLongClick;

    public CategoryAdapter(View.OnClickListener onItemClick, View.OnLongClickListener onItemLongClick) {
        this.onItemClick = onItemClick;
        this.onItemLongClick = onItemLongClick;
    }

    private List<Category> categories = new ArrayList<Category>();

    void setCategories(List<Category> cats) {
        this.categories.clear();
        this.categories.addAll(cats);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_category, viewGroup, false);
        return new ViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(categories.get(position), this.onItemClick, this.onItemLongClick);
        viewHolder.getTextView().setText(categories.get(position).getName());
        viewHolder.getColorPickerSwatch().setColor(categories.get(position).getColor());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ColorPickerSwatch colorPickerSwatch;
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            colorPickerSwatch = view.findViewById(R.id.color);
            textView = view.findViewById(R.id.name);
        }

        void bind(Category category, View.OnClickListener onItemClick, View.OnLongClickListener onItemLongClick) {
            Integer color = category.getColor();
            if (color != null) {
                int c = color;
                this.colorPickerSwatch.setColor(c);
            }
            this.textView.setText(category.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClick.onClick(view);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemLongClick.onLongClick(view);
                    return true;
                }
            });
        }

        public ColorPickerSwatch getColorPickerSwatch() {
            return colorPickerSwatch;
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
