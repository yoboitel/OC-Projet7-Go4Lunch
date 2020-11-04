package com.yohan.go4lunch.activity;

import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.yohan.go4lunch.R;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotification;
    private Boolean temporarySwitchBoolean;
    private Boolean valueFromFirestore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(view -> {

            //Save when user quit the settings activity if needed
            if (temporarySwitchBoolean != valueFromFirestore){

                Toast.makeText(SettingsActivity.this, "Saving...", Toast.LENGTH_SHORT).show();

                Map<String, Object> data = new HashMap<>();
                data.put("notificationActive", temporarySwitchBoolean);

                //Save in Firestore
                FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(data, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SettingsActivity.this, "Settings Saved", Toast.LENGTH_SHORT).show();
                            //Leave Activity when saving is finished
                            SettingsActivity.this.onBackPressed();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getBaseContext(), "Failed", Toast.LENGTH_SHORT).show());

            }
            else
                //Leave Activity if no need to save
                SettingsActivity.this.onBackPressed();
        });

        switchNotification = findViewById(R.id.switchNotif);
        //Init Switch State
        initSwitchStateFromFirestore();
        switchNotification.setOnCheckedChangeListener((compoundButton, b) -> {

            temporarySwitchBoolean = b;

        });
    }

    private void initSwitchStateFromFirestore() {
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            Boolean isNotifActive = (Boolean) task.getResult().get("notificationActive");

            if (isNotifActive != null){

                //Check or Uncheck switch if notification are enabled or not on Firestore
                valueFromFirestore = isNotifActive;
                temporarySwitchBoolean = isNotifActive;
                switchNotification.setChecked(valueFromFirestore);
                switchNotification.jumpDrawablesToCurrentState();
            }
        });
    }
}
