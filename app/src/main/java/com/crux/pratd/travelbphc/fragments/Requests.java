package com.crux.pratd.travelbphc.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.crux.pratd.travelbphc.ApiClient;
import com.crux.pratd.travelbphc.ApiInterface;
import com.crux.pratd.travelbphc.R;
import com.crux.pratd.travelbphc.activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.crux.pratd.travelbphc.activities.LoginActivity.db;

public class Requests extends Fragment {
    DatabaseReference mRef;

    public Requests() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_requests, container, false);

        mRef = FirebaseDatabase.getInstance().getReference();

        final LinearLayout linearLayout = view.findViewById(R.id.disp_req);
        final View progress = view.findViewById(R.id.overlay);

        final TextView msg = new TextView(getActivity());
        msg.setTextSize(20);
        msg.setTextColor(Color.BLACK);
        msg.setGravity(Gravity.CENTER);
        msg.setText("No requests to show");
        msg.setGravity(Gravity.CENTER);
        Log.d("Requests","fetching requests for "+ LoginActivity.user.getUid());
        FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("receiver_id", LoginActivity.user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("Requests","Callback"+task.getResult().getDocuments().size());
                        if (task.getResult().getDocuments().size() != 0) {
                            linearLayout.removeAllViews();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                final String key = doc.getString("sender_id");
                                Log.d("Requests",key);
                                final View child = inflater.inflate(R.layout.individual_req, linearLayout, false);
                                final TextView tv = child.findViewById(R.id.req_message);
                                tv.setTextSize(16);
                                if (key != null) {
                                    db.get()
                                            .collection("Users")
                                            .document(key)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        String text = task.getResult().getString("Name") + " requested to join your plan";
                                                        tv.setText(text);
                                                    }
                                                }
                                            });
                                }
                                child.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progress.setVisibility(View.VISIBLE);
                                        JsonObject json = new JsonObject();
                                        json.addProperty("sender", LoginActivity.user.getUid());
                                        json.addProperty("receiver", key);
                                        json.addProperty("status", true);
                                        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call = apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                                progress.setVisibility(View.GONE);
                                                if (response.body() == null || !response.body().has("status")) {
                                                    Toast.makeText(getActivity(), "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("Response", response.body().toString());
                                                int status = Integer.parseInt(response.body().get("status").toString());
                                                if (status == 1) {
                                                    linearLayout.removeView(child);
                                                    Toast.makeText(getActivity(), "Request approved", Toast.LENGTH_SHORT).show();
                                                } else
                                                    Toast.makeText(getActivity(), "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                                Log.d("Response", "Error");
                                                progress.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), "Unknown error occurred, try again later", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                child.findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progress.setVisibility(View.VISIBLE);
                                        JsonObject json = new JsonObject();
                                        json.addProperty("sender", LoginActivity.user.getUid());
                                        json.addProperty("receiver", key);
                                        json.addProperty("status", false);
                                        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call = apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                                progress.setVisibility(View.GONE);
                                                if (response.body() == null || !response.body().has("status")) {
                                                    Toast.makeText(getActivity(), "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("Response", response.body().toString());
                                                int status = Integer.parseInt(response.body().get("status").toString());
                                                if (status == 1) {
                                                    linearLayout.removeView(child);
                                                    Toast.makeText(getActivity(), "Request rejected", Toast.LENGTH_SHORT).show();
                                                } else
                                                    Toast.makeText(getActivity(), "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                                Log.d("Response", "Error");
                                                progress.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                linearLayout.addView(child);
                            }
                        } else {
                            linearLayout.removeAllViews();
                            linearLayout.addView(msg);
                        }
                    }
                });
        return view;
    }
}
