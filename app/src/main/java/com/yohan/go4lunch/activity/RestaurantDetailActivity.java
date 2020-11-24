package com.yohan.go4lunch.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.adapter.WorkmatesAdapter;
import com.yohan.go4lunch.model.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ImageView ivPhoto, ivRating;
    private TextView tvName, tvAddress;
    private Button btnPhone, btnLike,btnWebsite;
    private String restaurantId;
    private PlacesClient mPlacesClient;
    private FloatingActionButton fabGoToRestaurant;
    private ArrayList<User> participantsList;
    private RecyclerView rcParticipants;
    private WorkmatesAdapter participantsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        ivPhoto = findViewById(R.id.ivDetailPhoto);
        tvName = findViewById(R.id.tvDetailName);
        ivRating = findViewById(R.id.ivDetailRating);
        tvAddress = findViewById(R.id.tvDetailAddress);
        btnPhone = findViewById(R.id.btnDetailCall);
        btnLike = findViewById(R.id.btnDetailLike);
        btnWebsite = findViewById(R.id.btnDetailWebsite);
        fabGoToRestaurant = findViewById(R.id.fabDetailSelected);
        //Init RecyclerView And Adapter
        participantsList = new ArrayList<>();
        rcParticipants = findViewById(R.id.rcDetailWorkmates);
        rcParticipants.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rcParticipants.setHasFixedSize(true);

        //Initialize Maps Place SDK
        mPlacesClient = com.google.android.libraries.places.api.Places.createClient(getBaseContext());

        //Retrieve the restaurant Id we need details for from the Extras
        restaurantId = getIntent().getStringExtra("EXTRA_RESTAURANT_ID");
        getPlaceInfosFromId(restaurantId);

        //Init Participants list
        loadParticipantsFromFirestore();

        //Init FAB with right image ressource

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {

                if (task.getResult() != null) {
                    String result = task.getResult().getString("choosedRestaurantId");
                    if (result != null) {
                        if (result.equals(restaurantId))
                            fabGoToRestaurant.setImageResource(R.drawable.ic_check_activated);
                    }
                }
            });
        }

        //Handle click on FAB
        fabGoToRestaurant.setOnClickListener(view -> {

            //Check if user already choosed a restaurant
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {

                if (task.getResult() != null) {
                    String result = task.getResult().getString("choosedRestaurantId");

                    if (result == null || !result.equals(restaurantId))
                        goToThisRestaurant(true);
                    else
                        goToThisRestaurant(false);
                }
            });
        });

        //Init Like Button State
        initLikeButton();
        //Handle click on Like Button
        btnLike.setOnClickListener(view -> {

            //Check if user already liked this restaurant
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                @SuppressWarnings("unchecked")
                List<String> likedRestaurants = (List<String>) Objects.requireNonNull(task.getResult()).get("liked");
                likeRestaurant();
                if (likedRestaurants != null){
                    //Like Restaurant
                    for (String likedRestaurantId : likedRestaurants){
                        if (likedRestaurantId.equals(restaurantId)){

                            //Unlike Restaurant
                            unlikeRestaurant();
                        }
                    }
                }
            });
        });

    }

    private void getPlaceInfosFromId(String placeId) {
        //Specify which fields to retrieve
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {

            //SET INFOS//
            fillWidgetsWithRestaurantData(response.getPlace());
        });
    }

    private void fillWidgetsWithRestaurantData(Place mPlace) {
        //Set photo
        if (mPlace.getPhotoMetadatas() != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(mPlace.getPhotoMetadatas().get(0)).build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                ivPhoto.setImageBitmap(bitmap);
            });
        } else{
            ivPhoto.setImageResource(R.drawable.ic_no_photo);
        }
        //Set name
        tvName.setText(mPlace.getName());
        //Set rating
        if (mPlace.getRating() != null){
            if (mPlace.getRating() >= 4)
                ivRating.setImageResource(R.drawable.stars3);
            if (mPlace.getRating() < 4 && mPlace.getRating() >= 3)
                ivRating.setImageResource(R.drawable.stars2);
            if (mPlace.getRating() < 3)
                ivRating.setImageResource(R.drawable.stars1);
        }
        //Set address
        if (mPlace.getAddress() != null) {
            String shorterAddress = mPlace.getAddress().substring(0, mPlace.getAddress().indexOf(','));
            tvAddress.setText(shorterAddress);
        }
        //Set phone
        if (mPlace.getPhoneNumber() != null){
            btnPhone.setOnClickListener(view -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+mPlace.getPhoneNumber()));
                startActivity(callIntent);
            });
        } else
            btnPhone.setVisibility(View.GONE);
        //Set website
        if (mPlace.getWebsiteUri() != null) {
            btnWebsite.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,mPlace.getWebsiteUri())));
        } else
            btnWebsite.setVisibility(View.GONE);
    }

    private void goToThisRestaurant(boolean isTrue){
        int drawable;
        //Update in firestore
        Map<String, Object> data = new HashMap<>();
        if (isTrue) {
            data.put("choosedRestaurantId", restaurantId);
            drawable = R.drawable.ic_check_activated;
        }
        else {
            data.put("choosedRestaurantId", null);
            drawable = R.drawable.ic_check_desactivated;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        fabGoToRestaurant.setImageResource(drawable);
                        loadParticipantsFromFirestore();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RestaurantDetailActivity.this.getBaseContext(), getString(R.string.firestore_fail_message) + e, Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void loadParticipantsFromFirestore() {

        if(participantsList.size()>0)
            participantsList.clear();

        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {

            if (task.getResult() != null) {

                for (DocumentSnapshot querySnapshot : task.getResult()) {

                    String restaurant = querySnapshot.getString("choosedRestaurantId");
                    if (restaurant != null) {
                        if (restaurant.equals(restaurantId)) {

                            //For each users that goes to this restaurant ,create a user object with its info and add it in the list.
                            User user = new User(
                                    querySnapshot.getString("uid"),
                                    querySnapshot.getString("firstnameAndName"),
                                    querySnapshot.getString("photoUrl"),
                                    querySnapshot.getString("choosedRestaurantId"),
                                    querySnapshot.getBoolean("notificationActive"));

                            participantsList.add(user);

                        }
                    }
                }

            }

            participantsAdapter = new WorkmatesAdapter(getBaseContext(), participantsList, restaurantId, position -> {
                //Do nothing
            });
            rcParticipants.setAdapter(participantsAdapter);
            participantsAdapter.notifyDataSetChanged();
        });
    }

    private void likeRestaurant(){
        //Like Restaurant
        btnLike.setText(R.string.liked);
        btnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_filled, 0, 0);
        FirebaseFirestore.getInstance().collection("Users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .update("liked", FieldValue.arrayUnion(restaurantId));
    }

    private void unlikeRestaurant(){
        //Unlike Restaurant
        btnLike.setText(R.string.like);
        btnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_unfilled, 0, 0);
        FirebaseFirestore.getInstance().collection("Users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .update("liked", FieldValue.arrayRemove(restaurantId));
    }

    private void initLikeButton() {
        //Check if user already liked this restaurant
        FirebaseFirestore.getInstance().collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).get().addOnCompleteListener(task -> {
            @SuppressWarnings("unchecked")
            List<String> likedRestaurants = (List<String>) Objects.requireNonNull(task.getResult()).get("liked");
            if (likedRestaurants != null){

                for (String likedRestaurantId : likedRestaurants){
                    if (likedRestaurantId.equals(restaurantId)){

                        btnLike.setText(R.string.liked);
                        btnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_filled, 0, 0);

                    }
                }
            }
        });
    }
}