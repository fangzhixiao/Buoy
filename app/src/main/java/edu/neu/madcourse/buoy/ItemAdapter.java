package edu.neu.madcourse.buoy;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Adapter for "parent" list --> could be List names, or tasks (which nest to buoy's)
 * For nested view to work, every individual parent item needs to have its own adapter--meaning
 * in main activity, use a loop to make a new adapter for each item.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    //private ArrayList<ItemCard> listItems;
    ItemCard listItems;
    private ItemClickListener listener;

    //Put in methods for when clicking the item card.
    public interface ItemClickListener {
        void onItemClick();
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView header;

        public ItemViewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);
            this.header = (TextView)itemView.findViewById(R.id.check_header);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick();
                    }
                }
            });
        }
    }

    public ItemAdapter(ItemCard list) {
        this.listItems = list;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemCard current = listItems;
        holder.header.setText(current.getHeader());
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}