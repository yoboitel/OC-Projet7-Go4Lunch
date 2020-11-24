package com.yohan.go4lunch.model;

public class User {

    private String uid;
    private String firstnameAndName;
    private String photoUrl;
    private String choosedRestaurantId;
    private Boolean notificationActive;

    public User(String uid, String firstnameAndName, String photoUrl, String choosedRestaurantId, Boolean notificationActive) {
        this.uid = uid;
        this.firstnameAndName = firstnameAndName;
        this.photoUrl = photoUrl;
        this.choosedRestaurantId = choosedRestaurantId;
        this.notificationActive = notificationActive;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstnameAndName() {
        return firstnameAndName;
    }

    public void setFirstnameAndName(String firstnameAndName) {
        this.firstnameAndName = firstnameAndName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getChoosedRestaurantId() {
        return choosedRestaurantId;
    }

    public void setChoosedRestaurantId(String choosedRestaurantId) {
        this.choosedRestaurantId = choosedRestaurantId;
    }

    public Boolean isNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(Boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

}
