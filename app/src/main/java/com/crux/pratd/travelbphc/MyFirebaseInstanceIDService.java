package com.crux.pratd.travelbphc;

import android.util.Log;

import androidx.annotation.NonNull;

import com.crux.pratd.travelbphc.activities.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) { //Added onNewToken method
        // Get updated InstanceID token.
        sendRegistrationToServer(token);
    }

    public void sendRegistrationToServer(String token) {
        if (token == null)
            return;
        if (LoginActivity.user == null)
            return;
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        FirebaseFirestore.getInstance().collection("user").document(LoginActivity.user.getUid()).set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Token Service", "Updated Successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Token Service", "Updated Failed");
                    }
                });
    }
}
