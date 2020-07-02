package com.yohan.go4lunch;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.yohan.go4lunch.model.User;
import java.util.List;

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesAdapter.ViewHolder> {

    private Context context;
    private List<User> list;
    private OnItemClickListener onItemClickListener;

    public WorkmatesAdapter(Context context, List<User> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);

        }

        public void bind(final User model, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(getLayoutPosition());

                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_workmates, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User item = list.get(position);

        holder.bind(item, onItemClickListener);

        TextView tvCell = holder.itemView.findViewById(R.id.itemWorkmatesTv);
        SimpleDraweeView ivCell = holder.itemView.findViewById(R.id.itemWorkmatesProfilePicture);

        if (item.getChoosedRestaurantId() != null)
            tvCell.setText(item.getFirstnameAndName() + " is eating at DelArte");

        else
            tvCell.setText(item.getFirstnameAndName() + " hasn't decided yet");



        ivCell.setImageURI(item.getPhotoUrl());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}