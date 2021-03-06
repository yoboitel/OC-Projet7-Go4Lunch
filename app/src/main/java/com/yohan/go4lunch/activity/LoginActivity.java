package com.yohan.go4lunch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.model.User;

public class LoginActivity extends AppCompatActivity {

    private Button btnLoginFb, btnLoginGoogle;
    private GoogleSignInClient mgoogleSignInClient;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private LoginButton loginButton;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is already signed in (non-null) and start MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialization();

        //Click on Google login
        btnLoginGoogle.setOnClickListener(view -> {
            Intent signInIntent = mgoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 999);
        });

        //Click on Facebook login
        btnLoginFb.setOnClickListener(view -> {

            loginButton.setPermissions("email", "public_profile");
            loginButton.performClick();

            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    firebaseAuthWithFacebook(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException error) {
                }
            });
        });
    }

    //Method to initialize views
    private void initialization() {
        //INITIALIZATION
        btnLoginFb = findViewById(R.id.btnLoginFb);
        loginButton = findViewById(R.id.login_button_fb);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        //Initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();
        //Initialize Google auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_id_token))
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        // Initialize Facebook auth
        mCallbackManager = CallbackManager.Factory.create();
    }

    //Retrieve the google/fb auth result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //Result for google authentication
        if (requestCode == 999) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Call method to auth user in firebase
                if (account != null)
                    firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                //
            }
        }
    }

    //Method called to auth user in firebase after it sucessfully authenticated with google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        createUserInFirestore();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.toast_failed_auth, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Method called to auth user in firebase after it sucessfully authenticated with facebook
    private void firebaseAuthWithFacebook(AccessToken accessToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        createUserInFirestore();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.toast_failed_auth, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserInFirestore() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            User newUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null, null, true);
            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).set(newUser, SetOptions.merge()).addOnSuccessListener(aVoid -> startActivity(new Intent(LoginActivity.this, MainActivity.class))).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, R.string.toast_failed_usercreation, Toast.LENGTH_SHORT).show());
        }
    }

}