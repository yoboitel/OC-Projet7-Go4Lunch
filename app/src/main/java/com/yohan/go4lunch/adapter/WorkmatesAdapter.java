package com.yohan.go4lunch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class WorkmatesAdapter extends RecyclerView.Adapter<WorkmatesAdapter.ViewHolder> {

    private Context context;
    private List<User> list;
    private OnItemClickListener onItemClickListener;
    private String restaurantId;

    public WorkmatesAdapter(Context context, List<User> list, String restaurantId, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
        this.restaurantId = restaurantId;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);

        }

        public void bind(final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(getLayoutPosition()));
        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_workmates, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User item = list.get(position);

        holder.bind(onItemClickListener);

        TextView tvCell = holder.itemView.findViewById(R.id.itemWorkmatesTv);
        SimpleDraweeView ivCell = holder.itemView.findViewById(R.id.ivListRestaurantPreview);

        if (item.getChoosedRestaurantId() != null) {

            //Request detail to get restaurant name
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(item.getChoosedRestaurantId(), placeFields);
            com.google.android.libraries.places.api.Places.createClient(context).fetchPlace(request).addOnSuccessListener((response) -> {
                //SET INFOS//
                if (item.getChoosedRestaurantId().equals(restaurantId))
                    if (item.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        tvCell.setText("You are joining !");
                    else
                        tvCell.setText(item.getFirstnameAndName() + " is joining !");
                else {
                    if (item.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        tvCell.setText("You are eating at " + response.getPlace().getName());
                    else
                        tvCell.setText(item.getFirstnameAndName() + " is eating at " + response.getPlace().getName());
                }
            });
        } else {
            if (item.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                tvCell.setText("You didn't decided yet");
            else
                tvCell.setText(item.getFirstnameAndName() + " hasn't decided yet");
        }


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