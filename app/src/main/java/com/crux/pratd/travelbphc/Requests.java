package com.crux.pratd.travelbphc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
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

import static com.facebook.FacebookSdk.getApplicationContext;

public class Requests extends Fragment {
    DatabaseReference mRef;
    public Requests() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRef=FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_requests, container, false);

        mRef=FirebaseDatabase.getInstance().getReference();

        final LinearLayout linearLayout=view.findViewById(R.id.disp_req);
        final View progress=view.findViewById(R.id.overlay);

        final TextView msg=new TextView(getActivity());
        msg.setTextSize(20);
        msg.setTextColor(Color.BLACK);
        msg.setGravity(Gravity.CENTER);
        msg.setText("No requests to show");
        msg.setGravity(Gravity.CENTER);
        FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("receiver_id",Profile.getCurrentProfile().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().getDocuments().size() != 0) {
                            linearLayout.removeAllViews();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                final String key=doc.get("sender_id").toString();
                                final View child=inflater.inflate(R.layout.individual_req, linearLayout,false);
                                final TextView tv=child.findViewById(R.id.req_message);
                                new GraphRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        "/"+key,
                                        null,
                                        HttpMethod.GET,
                                        new GraphRequest.Callback() {
                                            @Override
                                            public void onCompleted(GraphResponse response) {
                                                try{
                                                    Log.d("json",response.getJSONObject().getString("name"));
                                                    tv.setText(response.getJSONObject().getString("name")+" requested to join your plan.");
                                                    tv.setTextSize(16);
                                                }
                                                catch (Exception e)
                                                {
                                                    Log.d("Json",e.toString());
                                                }
                                            }
                                        }).executeAsync();
                                child.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progress.setVisibility(View.VISIBLE);
                                        JsonObject json=new JsonObject();
                                        json.addProperty("sender",Profile.getCurrentProfile().getId());
                                        json.addProperty("receiver",key);
                                        json.addProperty("status",true);
                                        ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call=apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                progress.setVisibility(View.GONE);
                                                if(response.body()==null || !response.body().has("status")){
                                                    Toast.makeText(getApplicationContext(),"Unable to process your request, try again later",Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("Response",response.body().toString());
                                                int status=Integer.parseInt(response.body().get("status").toString());
                                                if(status==1) {
                                                    linearLayout.removeView(child);
                                                    Toast.makeText(getApplicationContext(), "Request approved", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                    Toast.makeText(getApplicationContext(),"Unable to process your request, try again later",Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                Log.d("Response","Error");
                                                progress.setVisibility(View.GONE);
                                                Toast.makeText(getContext(),"Unknown error occurred, try again later",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                child.findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progress.setVisibility(View.VISIBLE);
                                        JsonObject json=new JsonObject();
                                        json.addProperty("sender",Profile.getCurrentProfile().getId());
                                        json.addProperty("receiver",key);
                                        json.addProperty("status",false);
                                        ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call=apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                progress.setVisibility(View.GONE);
                                                if(response.body()==null || !response.body().has("status")){
                                                    Toast.makeText(getApplicationContext(),"Unable to process your request, try again later",Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                Log.d("Response",response.body().toString());
                                                int status=Integer.parseInt(response.body().get("status").toString());
                                                if(status==1) {
                                                    linearLayout.removeView(child);
                                                    Toast.makeText(getApplicationContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                    Toast.makeText(getApplicationContext(),"Unable to process your request, try again later",Toast.LENGTH_SHORT).show();
                                            }
                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                Log.d("Response","Error");
                                                progress.setVisibility(View.GONE);
                                                Toast.makeText(getContext(),"Check your internet connection",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                linearLayout.addView(child);
                            }
                        }
                        else {
                            linearLayout.removeAllViews();
                            linearLayout.addView(msg);
                        }
                    }
                });
        return view;
    }
}
