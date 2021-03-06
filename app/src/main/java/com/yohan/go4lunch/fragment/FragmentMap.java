package com.yohan.go4lunch.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yohan.go4lunch.BuildConfig;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.activity.RestaurantDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FragmentMap extends Fragment implements GoogleMap.OnMarkerClickListener {

    private final int ZOOM_LEVEL = 15; //This goes up to 21
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient mPlacesClient;
    private Location mLastKnownLocation;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            gMap = googleMap;

            //Method to ask for location permission and move map to user's location
            getDeviceLocation();
        }
    };

    //Find nearby locations
    public void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted so ask for it
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //Permission is granted so retrieve the user's last position
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {

                    mLastKnownLocation = location;

                    //Setup Google Map
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL));
                    gMap.setMyLocationEnabled(true);
                    gMap.setOnMarkerClickListener(this);
                    gMap.getUiSettings().setMyLocationButtonEnabled(true);

                    // Use fields to define the data types to return.
                    List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME);

                    // Use the builder to create a FindCurrentPlaceRequest.
                    FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                    // Call findCurrentPlace and handle the response (first check that the user has granted permission).
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
                        placeResponse.addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FindCurrentPlaceResponse response = task.getResult();
                                if (response != null) {
                                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                                        Place currPlace = placeLikelihood.getPlace();

                                        //Keep only results with restaurant type
                                        if (Objects.requireNonNull(currPlace.getTypes()).contains(Place.Type.RESTAURANT)) {

                                            final Integer[] participantsCount = {0};

                                            //Count all participants to this restaurant
                                            FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task2 -> {
                                                if (task2.getResult() != null) {
                                                    for (DocumentSnapshot querySnapshot : task2.getResult()) {
                                                        String restaurant = querySnapshot.getString("choosedRestaurantId");
                                                        if (restaurant != null) {
                                                            if (restaurant.equals(currPlace.getId())) {
                                                                participantsCount[0]++;
                                                            }
                                                        }
                                                    }
                                                }

                                                //Set the marker in green if at least one user eat here
                                                String defaultMarker = "ic_marker_orange";
                                                if (participantsCount[0] > 0)
                                                    defaultMarker = "ic_marker_green";

                                                if (currPlace.getLatLng() != null) {

                                                    //Put the marker on the map
                                                    gMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(currPlace.getLatLng().latitude, currPlace.getLatLng().longitude))
                                                            .title(currPlace.getName())
                                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(defaultMarker, 100, 135))))
                                                            .setTag(currPlace.getId());
                                                }
                                            });
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
                        // A local method to request required permissions;
                        // See https://developer.android.com/training/permissions/requesting
                        getDeviceLocation();
                    }


                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
                getDeviceLocation();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //MAPS
        String apiKey = BuildConfig.google_maps_key;
        com.google.android.libraries.places.api.Places.initialize(requireContext(), apiKey);
        mPlacesClient = com.google.android.libraries.places.api.Places.createClient(requireContext());

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    //Method to edit marker size
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


    //Open detail activity when marker is clicked
    @Override
    public boolean onMarkerClick(Marker marker) {

        Intent intent = new Intent(requireActivity(), RestaurantDetailActivity.class);
        intent.putExtra("EXTRA_RESTAURANT_ID", Objects.requireNonNull(marker.getTag()).toString());
        startActivity(intent);

        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.search_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (mLastKnownLocation != null) {

                    // Create a RectangularBounds object from 2 points around user location, southwest coordinates and northeast coordinates.
                    RectangularBounds bounds = RectangularBounds.newInstance(new LatLng(mLastKnownLocation.getLatitude() - 0.003, mLastKnownLocation.getLongitude() - 0.01), new LatLng(mLastKnownLocation.getLatitude() + 0.003, mLastKnownLocation.getLongitude() + 0.01));

                    // Use the builder to create a FindAutocompletePredictionsRequest.
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setLocationRestriction(bounds)
                            .setTypeFilter(TypeFilter.ESTABLISHMENT)
                            .setQuery(newText)
                            .build();

                    mPlacesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {

                            gMap.clear();
                            getPlaceLocationFromIdAndPlaceMarker(prediction);

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

    private void getPlaceLocationFromIdAndPlaceMarker(AutocompletePrediction prediction) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place mPlace = response.getPlace();
            LatLng latLng = mPlace.getLatLng();

            //Refresh the Map with the autocomplete predictions list pins
            if (latLng != null) {
                gMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude))
                        .title(mPlace.getName())
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("ic_marker_orange", 100, 135))))
                        .setTag(prediction.getPlaceId());
            }
        });
    }

}