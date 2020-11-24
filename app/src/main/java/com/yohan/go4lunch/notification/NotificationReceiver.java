package com.yohan.go4lunch.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yohan.go4lunch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "100";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //Check if user has choosed a restaurant to eat so we can show him the notification or not
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {

                if (task.getResult() != null) {
                    String restaurantId = (String) task.getResult().get("choosedRestaurantId");
                    if (restaurantId != null) {
                        //Check if user enabled notification in setings or not
                        boolean isNotifEnabled = (boolean) task.getResult().get("notificationActive");
                        if (isNotifEnabled)
                            getPlaceInfosFromId(restaurantId);
                    }
                }
            });

        }

    }

    private void getPlaceInfosFromId(String placeId) {
        //Specify which fields to retrieve
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        PlacesClient mPlacesClient = com.google.android.libraries.places.api.Places.createClient(context);
        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            //SET INFOS//
            retrieveRestaurantParticipants(response.getPlace(), placeId);
        });
    }

    private void retrieveRestaurantParticipants(Place place, String restaurantId) {

        ArrayList<String> participantsNameList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("Users").get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                for (DocumentSnapshot querySnapshot : task.getResult()) {
                    String restaurant = querySnapshot.getString("choosedRestaurantId");
                    if (restaurant != null) {
                        if (restaurant.equals(restaurantId)) {
                            //For each users that goes to this restaurant , add its name in the list.
                            participantsNameList.add(querySnapshot.getString("firstnameAndName"));
                        }
                    }
                }
            }

            //Beautify participants list string
            StringBuilder participants = new StringBuilder();
            int i = 0;
            for (String str: participantsNameList)
            {
                participants.append(str);
                if (i != participantsNameList.size()-1)
                    participants.append(" and ");
                else
                    participants.append(".");
                i++;
            }

            //Create the message string with all the data
            String message = "At " + place.getName() + " in " + place.getAddress() + " with " + participants.toString();
            showNotification(message);
        });
    }

    public void showNotification(String content) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.bowl)
                .setContentTitle("Don't forget your lunch today !")
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            if (mNotificationManager != null) {
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
        if (mNotificationManager != null)
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

}
