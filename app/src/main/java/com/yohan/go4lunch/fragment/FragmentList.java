package com.yohan.go4lunch.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.yohan.go4lunch.BuildConfig;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.adapter.RestaurantsAdapter;
import com.yohan.go4lunch.model.Restaurant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentList extends Fragment {

    private List<Restaurant> mRestaurantList;
    private RestaurantsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient mPlacesClient;
    private Place mPlace;
    private Location mLastKnownLocation;

    public FragmentList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        //MAPS
        String apiKey = BuildConfig.google_maps_key;
        com.google.android.libraries.places.api.Places.initialize(requireContext(), apiKey);
        mPlacesClient = com.google.android.libraries.places.api.Places.createClient(requireContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getDeviceLocation();

        mRestaurantList = new ArrayList<>();
        mRecyclerView = v.findViewById(R.id.rcRestaurants);
        mAdapter = new RestaurantsAdapter(requireContext(), mRestaurantList, new RestaurantsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(FragmentList.this.requireContext(), "Start RestaurantDetail Activity", Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    public void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted so ask for it
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            //Permission is granted so retrieve the user's last position
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {

                    mLastKnownLocation = location;

                    //Find nearby locations
                    // Use fields to define the data types to return.
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.ID);

                    // Use the builder to create a FindCurrentPlaceRequest.
                    FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                    // Call findCurrentPlace and handle the response (first check that the user has granted permission).
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
                        placeResponse.addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                FindCurrentPlaceResponse response = task.getResult();
                                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                    Log.d("NEARBOAT", String.format("Place '%s' has likelihood: %f", placeLikelihood.getPlace().getTypes(), placeLikelihood.getLikelihood()));

                                    Place currPlace = placeLikelihood.getPlace();
                                    if (currPlace.getTypes().contains(Place.Type.RESTAURANT)) {
                                        getPlaceInfosFromId(currPlace.getId(), mRestaurantList);
                                    }
                                }
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    Log.d("NEARBOAT", "Place not found: " + apiException.getStatusCode());
                                }
                            }
                        });
                    } else {
                        getDeviceLocation();
                    }
                }
            });
        }
    }

    private void getPlaceInfosFromId(String placeId, List<Restaurant> restaurantList) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            mPlace = response.getPlace();

            String placeName = mPlace.getName();
            String address = mPlace.getAddress();
            OpeningHours openingHours = mPlace.getOpeningHours();
            LatLng latLng = mPlace.getLatLng();
            Double rating = mPlace.getRating();
            String placeDistance = getDistanceFromLastKnownLocation(latLng.latitude, latLng.longitude);
            PhotoMetadata photos;
            if (mPlace.getPhotoMetadatas() == null) {
                photos = null;
            } else {
                photos = mPlace.getPhotoMetadatas().get(0);
            }


            restaurantList.add(new Restaurant(placeId, placeName, address, openingHours, latLng, placeDistance, rating, photos));
            mAdapter.notifyDataSetChanged();
        });
    }

    public String getDistanceFromLastKnownLocation(Double lat, Double lng) {
        Location targetLocation = new Location("");
        targetLocation.setLatitude(lat);
        targetLocation.setLongitude(lng);

        float distance =  targetLocation.distanceTo(mLastKnownLocation);
        return (int)distance + "m";
    }
}