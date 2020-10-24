package com.yohan.go4lunch.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.yohan.go4lunch.BuildConfig;
import com.yohan.go4lunch.R;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.List;

public class FragmentMap extends Fragment {

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;
    private int ZOOM_LEVEL = 15; //This goes up to 21

    private PlacesClient mPlacesClient;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {


            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            gMap = googleMap;

            //Method to ask for location permission and move map to user's location
            getDeviceLocation();
        }
    };

    public void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Permission not granted so ask for it
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            //Permission is granted so retrieve the user's last position
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL));
                    gMap.setMyLocationEnabled(true);
                    gMap.getUiSettings().setMyLocationButtonEnabled(true);

                    //Find nearby locations
                    // Use fields to define the data types to return.
                    List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

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
                                        Log.d("NEARBOAT", String.format("Place '%s' has likelihood: %f",
                                                placeLikelihood.getPlace().getName(),
                                                placeLikelihood.getLikelihood()));
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
                //GRANTED
                getDeviceLocation();
            }
        }
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

}