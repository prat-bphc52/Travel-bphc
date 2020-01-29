package com.crux.pratd.travelbphc.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crux.pratd.travelbphc.ApiClient;
import com.crux.pratd.travelbphc.ApiInterface;
import com.crux.pratd.travelbphc.R;
import com.crux.pratd.travelbphc.model.TravelPlan;
import com.crux.pratd.travelbphc.activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by pratd on 20-01-2018.
 */

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.MyViewHolder> {

    public List<TravelPlan> plans;
    public Context context;

    public PlanAdapter(List<TravelPlan> travelPlans, Context context) {
        this.plans = travelPlans;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        final TravelPlan plan = plans.get(position);
        holder.source.setText(plan.getSource());
        holder.dest.setText(plan.getDest());
        holder.date.setText(plan.getDate());
        holder.time.setText(plan.getTime());
        holder.space_left.setText(plan.getSpace());

        Set<String> listTravellers = plan.getTravellers().keySet();
        final View disp[] = new View[listTravellers.size()];
        int i = 0;
        for (final String id : listTravellers) {

            View v = View.inflate(context, R.layout.display2, null);
            final TextView textView = v.findViewById(R.id.individual_name);
            final ImageView profilepic = v.findViewById(R.id.profilepic);
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                textView.setText(task.getResult().getString("Name"));
                                try {
                                    Picasso.
                                            with(context).
                                            load(task.getResult().getString("photos")).into(profilepic);
                                }
                                catch (Exception e){
                                }
                            }
                        }
                    });
            disp[i++] = v;
        }
        final LinearLayout container = new LinearLayout(holder.view_travellers.getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);
        for (View list : disp) {
            container.addView(list);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.view_travellers.getContext());
        builder.setView(container);
        final AlertDialog dialog = builder.create();
        holder.view_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        if (plan.getSpace().equals("1") || plan.getSpace().equals("2") || plan.getSpace().equals("0")) {
            holder.space_left.setTextColor(Color.rgb(255, 0, 0));
           // holder.indicator.setBackgroundColor(Color.rgb(255, 0, 0));
        } else {
            holder.space_left.setTextColor(Color.rgb(48, 252, 3));
            //holder.indicator.setBackgroundColor(Color.rgb(48, 252, 3));
        }
        if (plan.getSource().equalsIgnoreCase("station") || plan.getDest().equalsIgnoreCase("station"))
            holder.background.setImageResource(R.drawable.train);
        else
            holder.background.setImageResource(R.drawable.flight);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String creatorId = plan.getCreator();
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                if (creatorId.equals(LoginActivity.user.getUid()))
                    builder.setMessage("You cannot join your own plan!");
                else if (plan.getSpace().equals("0"))
                    builder.setMessage("No Space Left!");
                else if (plan.getTravellers().containsKey(LoginActivity.user.getUid())) {
                    builder.setMessage("You are already a part of this plan...\nDo you wish to leave?");
                    builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JsonObject json = new JsonObject();
                            json.addProperty("sender", LoginActivity.user.getUid());
                            json.addProperty("plan_id", plan.getCreator());
                            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                            Call<JsonObject> call = apiInterface.leavePlan(json);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.body() == null || !response.body().has("status")) {
                                        Toast.makeText(context, "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Log.d("Response", response.body().toString());
                                    int status = Integer.parseInt(response.body().get("status").toString());
                                    if (status == 1)
                                        Toast.makeText(context, "Plan left successfully", Toast.LENGTH_SHORT).show();
                                    else if (status == 0)
                                        Toast.makeText(context, "Request already sent", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Log.d("Response", "Error");
                                    Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    builder.setMessage("Do you wish to join the selected Plan?");
                    builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JsonObject json = new JsonObject();
                            json.addProperty("sender", LoginActivity.user.getUid());
                            json.addProperty("receiver", creatorId);
                            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                            Call<JsonObject> call = apiInterface.sendReq(json);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.body() == null || !response.body().has("status")) {
                                        Toast.makeText(context, "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Log.d("Response", response.body().toString());
                                    int status = Integer.parseInt(response.body().get("status").toString());
                                    if (status == 1)
                                        Toast.makeText(context, "Join request sent", Toast.LENGTH_SHORT).show();
                                    else if (status == 0)
                                        Toast.makeText(context, "Request already sent", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(context, "Unable to process your request, try again later", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.travel_card_new, parent, false));
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView background;
        TextView source, dest, date, time, space_left, view_travellers;
        View card;
        View indicator;

        MyViewHolder(View view) {
            super(view);
            source = view.findViewById(R.id.from_text);
            dest = view.findViewById(R.id.to_text);
            date = view.findViewById(R.id.date);
            time = view.findViewById(R.id.time);
            background = view.findViewById(R.id.back_img);
            space_left = view.findViewById(R.id.spaceleft);
            view_travellers = view.findViewById(R.id.viewtravellers);
            //indicator = view.findViewById(R.id.indicator);
            card = view.findViewById(R.id.cardView);
        }
    }
}
