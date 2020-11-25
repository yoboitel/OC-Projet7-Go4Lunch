package com.yohan.go4lunch;

import com.yohan.go4lunch.model.Message;
import com.yohan.go4lunch.model.Restaurant;
import com.yohan.go4lunch.model.User;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class ExampleUnitTest {

    private User user;
    private Restaurant restaurant;
    private Message message;

    @Before
    public void setUp() {
        user = new User("123456", "Yohan Boitel", "photoUrl", "123456", true);
        restaurant = new Restaurant("123456", "LeBeaufort", "1 rue du marché", null, null, "300m", 4.3, null);
        message = new Message("Contenu", "Yohan", "photoUrl", "123456", null);
    }

    @Test
    public void testGetUserData() {
        assertEquals("123456", user.getUid());
        assertEquals("Yohan Boitel", user.getFirstnameAndName());
        assertEquals("photoUrl", user.getPhotoUrl());
        assertEquals("123456", user.getChoosedRestaurantId());
        assertEquals(true, user.isNotificationActive());
    }

    @Test
    public void testGetRestaurantDetails() {
        assertEquals("123456", restaurant.getId());
        assertEquals("LeBeaufort", restaurant.getName());
        assertEquals("1 rue du marché", restaurant.getAddress());
        assertNull(restaurant.getOpeningHours());
        assertNull(restaurant.getLatLng());
        assertEquals("300m", restaurant.getDistance());
        assertEquals(Double.valueOf(4.3), restaurant.getRating());
        assertNull(restaurant.getPhoto());
    }

    @Test
    public void testGetMessageData() {
        assertEquals("Contenu", message.getMessage());
        assertEquals("Yohan", message.getAuthorName());
        assertEquals("photoUrl", message.getAuthorPhotoUrl());
        assertEquals("123456", message.getAuthorUid());
        assertNull(message.getTimestamp());
    }

    @Test
    public void testSetUserData() {
        user.setUid("654321");
        user.setFirstnameAndName("Anthony");
        user.setPhotoUrl("picLink");
        user.setChoosedRestaurantId("654321");
        user.setNotificationActive(false);

        assertEquals("654321", user.getUid());
        assertEquals("Anthony", user.getFirstnameAndName());
        assertEquals("picLink", user.getPhotoUrl());
        assertEquals("654321", user.getChoosedRestaurantId());
        assertEquals(false, user.isNotificationActive());
    }

    @Test
    public void testSetRestaurantDetails() {

        restaurant.setId("654321");
        restaurant.setName("Five Guys");
        restaurant.setAddress("1 rue Elysee");
        restaurant.setOpeningHours(null);
        restaurant.setLatLng(null);
        restaurant.setDistance("973m");
        restaurant.setRating(4.9);
        restaurant.setPhoto(null);

        assertEquals("654321", restaurant.getId());
        assertEquals("Five Guys", restaurant.getName());
        assertEquals("1 rue Elysee", restaurant.getAddress());
        assertNull(restaurant.getOpeningHours());
        assertNull(restaurant.getLatLng());
        assertEquals("973m", restaurant.getDistance());
        assertEquals(Double.valueOf(4.9), restaurant.getRating());
        assertNull(restaurant.getPhoto());
    }

    @Test
    public void testSetMessageData() {

        message.setMessage("Message");
        message.setAuthorName("Anthony");
        message.setAuthorPhotoUrl("picLink");
        message.setAuthorUid("654321");
        message.setTimestamp(null);

        assertEquals("Message", message.getMessage());
        assertEquals("Anthony", message.getAuthorName());
        assertEquals("picLink", message.getAuthorPhotoUrl());
        assertEquals("654321", message.getAuthorUid());
        assertNull(message.getTimestamp());
    }
}