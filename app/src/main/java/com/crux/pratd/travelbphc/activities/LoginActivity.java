package com.crux.pratd.travelbphc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crux.pratd.travelbphc.fragments.PhoneVerification;
import com.crux.pratd.travelbphc.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.lang.ref.WeakReference;

/**
 * Created by rutvora (www.github.com/rutvora)
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static WeakReference<FirebaseFirestore> db;
    public static FirebaseAuth firebaseAuth;
    public static GoogleSignInAccount account;
    public static FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize firebase and corresponding requirements
        FirebaseApp.initializeApp(this);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build();
        db.setFirestoreSettings(settings);
        LoginActivity.db = new WeakReference<>(db);
        firebaseAuth = FirebaseAuth.getInstance();

        //Check for firebase Sign-In
        user = firebaseAuth.getCurrentUser();

        if (!(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS)) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        } else {

            if (user == null) {
                try {
                    assert getSupportActionBar() != null;
                    getSupportActionBar().hide();
                    setContentView(R.layout.activity_login);
                    findViewById(R.id.sign_in_button).setOnClickListener(this);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 0) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
    }

    private void signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .setHostedDomain(getString(R.string.domain))
                .requestIdToken(getString(R.string.oauth_client))
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }

    private void firebaseAuthWithGoogle(@NonNull final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            updateUserAccountDetails(acct);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Error Signing in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void updateUserAccountDetails(GoogleSignInAccount account) {
        user = firebaseAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(account.getDisplayName())
                .setPhotoUri(account.getPhotoUrl())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent intent;

                            if(user.getPhoneNumber()==null) intent = new Intent(LoginActivity.this, PhoneVerification.class);
                            else intent = new Intent(LoginActivity.this, HomeActivity.class);

                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(LoginActivity.this, "Error creating profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
