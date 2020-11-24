package com.yohan.go4lunch.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.yohan.go4lunch.BuildConfig;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.activity.RestaurantDetailActivity;
import com.yohan.go4lunch.adapter.RestaurantsAdapter;
import com.yohan.go4lunch.model.Restaurant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentList extends Fragment {

    private List<Restaurant> mRestaurantList;
    private RestaurantsAdapter mAdapter;
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
        setHasOptionsMenu(true);
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

        //ADAPTER
        mRestaurantList = new ArrayList<>();
        mAdapter = new RestaurantsAdapter(requireContext(), mRestaurantList, position -> {
            //Start Restaurant Detail Activity sending the Restaurant Id in Extra
            Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
            intent.putExtra("EXTRA_RESTAURANT_ID", mRestaurantList.get(position).getId());
            startActivity(intent);
        });
        //RECYCLER VIEW SETTINGS AND SET ADAPTER
        RecyclerView mRecyclerView = v.findViewById(R.id.rcRestaurants);
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
                                if (response != null) {
                                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                                        Place currPlace = placeLikelihood.getPlace();
                                        if (currPlace.getTypes() != null) {
                                            if (currPlace.getTypes().contains(Place.Type.RESTAURANT)) {
                                                getPlaceInfosFromId(currPlace.getId(), mRestaurantList);
                                            }
                                        }
                                    }
                                }
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    Toast.makeText(requireActivity(), apiException.getMessage(), Toast.LENGTH_SHORT).show();
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
            String placeDistance = null;
            if (latLng != null) {
                placeDistance = getDistanceFromLastKnownLocation(latLng.latitude, latLng.longitude);
            }
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate( R.menu.search_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                
                if (mLastKnownLocation != null) {

                    // Create a RectangularBounds object from 2 points, southwest coordinates and northeast coordinates.
                    RectangularBounds bounds = RectangularBounds.newInstance(new LatLng(mLastKnownLocation.getLatitude() - 0.003, mLastKnownLocation.getLongitude() - 0.01), new LatLng(mLastKnownLocation.getLatitude() + 0.003, mLastKnownLocation.getLongitude() + 0.01));

                    // Use the builder to create a FindAutocompletePredictionsRequest.
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setLocationRestriction(bounds)
                            .setTypeFilter(TypeFilter.ESTABLISHMENT)
                            .setQuery(newText)
                            .build();

                    mPlacesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                            //Refresh the recyclerview with the autocomplete predictions list at each text change
                            mRestaurantList.clear();
                            getPlaceInfosFromId(prediction.getPlaceId(), mRestaurantList);
                        }
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Toast.makeText(requireContext(), apiException.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return false;
            }
        });
    }
}