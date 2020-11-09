package edu.neu.madcourse.buoy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.StickerViewHolder>{

    private ArrayList<FriendItemCard> friendList;
    private ItemClickListener listener;

    public interface ItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(ItemClickListener listener) {this.listener = listener;}

    public static class StickerViewHolder extends RecyclerView.ViewHolder{
        public TextView friendName;
        public TextView userName;

        public StickerViewHolder(@NonNull View itemView, final ItemClickListener listener) {
            super(itemView);
            friendName = itemView.findViewById(R.id.full_name);
            userName = itemView.findViewById(R.id.username);

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getLayoutPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    //recycler adapter constructor with list
    public StickerAdapter(ArrayList<FriendItemCard> friendList){ this.friendList = friendList;}

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item_card, parent, false);
        return new StickerViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        //list of users from database
        FriendItemCard current = friendList.get(position);
        holder.friendName.setText(current.getFirstName() +" "+ current.getLastName());
        holder.userName.setText(current.getUserName());

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }
}
