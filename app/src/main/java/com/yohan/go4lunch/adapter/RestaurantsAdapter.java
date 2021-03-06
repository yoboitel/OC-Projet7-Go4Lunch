package com.yohan.go4lunch.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.model.Restaurant;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.ViewHolder> {

    private final Context context;
    private final List<Restaurant> list;
    private final OnItemClickListener onItemClickListener;
    private PlacesClient mPlacesClient;

    public RestaurantsAdapter(Context context, List<Restaurant> list, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onItemClickListener;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_restaurants, parent, false);

        String key = parent.getResources().getString(R.string.google_maps_key);
        Places.initialize(parent.getContext(), key);
        mPlacesClient = Places.createClient(parent.getContext());

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Restaurant item = list.get(position);

        holder.bind(onItemClickListener);

        //Link views
        TextView tvRestaurantName = holder.itemView.findViewById(R.id.tvListRestaurantName);
        TextView tvRestaurantAddress = holder.itemView.findViewById(R.id.tvListRestaurantAddress);
        ImageView ivRestaurantRating = holder.itemView.findViewById(R.id.ivListRestaurantRating);
        TextView tvRestaurantOpeningHour = holder.itemView.findViewById(R.id.tvListRestaurantOpeningHours);
        TextView tvRestaurantDistance = holder.itemView.findViewById(R.id.tvListRestaurantDistance);
        ImageView ivRestaurantPhoto = holder.itemView.findViewById(R.id.ivListRestaurantPhoto);
        TextView tvRestaurantParticipants = holder.itemView.findViewById(R.id.tvListRestaurantNbPeopleEating);

        //Display Restaurant Name
        tvRestaurantName.setText(item.getName());

        //Display Restaurant Address
        String shorterAddress = item.getAddress().substring(0, item.getAddress().indexOf(','));
        tvRestaurantAddress.setText(shorterAddress);

        //Display Restaurant Stars
        if (item.getRating() != null) {
            if (item.getRating() >= 4)
                ivRestaurantRating.setImageResource(R.drawable.stars3);
            if (item.getRating() < 4 && item.getRating() >= 3)
                ivRestaurantRating.setImageResource(R.drawable.stars2);
            if (item.getRating() < 3)
                ivRestaurantRating.setImageResource(R.drawable.stars1);
        }

        //Display Restaurant Opening Hours
        if (item.getOpeningHours() != null) {
            tvRestaurantOpeningHour.setText(item.getOpeningHours().getWeekdayText().get(getDayOfWeek()));
        } else
            tvRestaurantOpeningHour.setText(R.string.cant_find_opening_hours);

        //Display Restaurant Distance
        if (item.getDistance() != null) {
            tvRestaurantDistance.setText(item.getDistance());
        }

        //Display Restaurant Photo
        if (item.getPhoto() != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(item.getPhoto()).build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                ivRestaurantPhoto.setImageBitmap(bitmap);
            });
        } else {
            ivRestaurantPhoto.setImageResource(R.drawable.ic_no_photo);
        }

        //Display Restaurant Participants Count
        displayParticipantsCountFromFirestore(item.getId(), tvRestaurantParticipants);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //Retrieve the day of the week to display the opening hours of today
    private int getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == 1) {
            dayOfWeek = 6;
        } else {
            dayOfWeek = dayOfWeek - 2;
        }
        return dayOfWeek;
    }

    //Count number of participants for each restaurant
    private void displayParticipantsCountFromFirestore(String restaurantId, TextView tvParticipantsCount) {

        final Integer[] participantsCount = {0};

        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {

            if (task.getResult() != null) {

                for (DocumentSnapshot querySnapshot : task.getResult()) {
                    String restaurant = querySnapshot.getString("choosedRestaurantId");
                    if (restaurant != null) {
                        if (restaurant.equals(restaurantId)) {

                            participantsCount[0]++;
                            tvParticipantsCount.setText(String.format(Locale.getDefault(), "(%d)", participantsCount[0]));
                            tvParticipantsCount.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

        });
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        //Create a listener on the restaurant item view so we can use it when we create the adapter
        public void bind(final OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(getLayoutPosition()));
        }
    }

}