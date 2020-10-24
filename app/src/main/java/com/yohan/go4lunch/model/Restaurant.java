package com.yohan.go4lunch.model;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;

public class Restaurant {

    private String id;
    private String name;
    private String address;
    private OpeningHours openingHours;
    private LatLng latLng;
    private String distance;
    private Double rating;
    private PhotoMetadata photo;

    public Restaurant(String id, String name, String address, OpeningHours openingHours, LatLng latLng, String distance, Double rating, PhotoMetadata photo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openingHours = openingHours;
        this.latLng = latLng;
        this.distance = distance;
        this.rating = rating;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public PhotoMetadata getPhoto() {
        return photo;
    }

    public void setPhoto(PhotoMetadata photo) {
        this.photo = photo;
    }

}
