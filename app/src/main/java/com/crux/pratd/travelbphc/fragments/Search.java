package com.crux.pratd.travelbphc.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crux.pratd.travelbphc.CreatePlan;
import com.crux.pratd.travelbphc.R;
import com.crux.pratd.travelbphc.adapters.PlanAdapter;
import com.crux.pratd.travelbphc.model.TravelPlan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Search extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    Calendar myCalendar;
    TextView fil_date, fil_time, status;
    private PlanAdapter adapter;
    private int y, m, d, h, s;
    private List<TravelPlan> plan_list = new ArrayList<>();
    private List<TravelPlan> plan_list_filtered = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCalendar = Calendar.getInstance();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        //TODO remove the next two lines after creating new activity layout
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CreatePlan.class);
                startActivity(intent);
                /*mRef.child(Profile.getCurrentProfile().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null)
                            Toast.makeText(getActivity(),"You can create only 1 plan at a time!",Toast.LENGTH_LONG).show();
                        else {
                            Intent intent = new Intent(getContext(), CreatePlan.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });*/
            }
        });
        y = m = d = h = s = -1;
        recyclerView = view.findViewById(R.id.rec_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        final TextView recycler_status = view.findViewById(R.id.status);
        final ProgressBar progress = view.findViewById(R.id.recycler_progress);
        FirebaseFirestore.getInstance().collection("plans")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        plan_list.clear();
                        if (task.getResult().getDocuments().size() != 0) {

                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                TravelPlan p = doc.toObject(TravelPlan.class);
                                Log.d("Search",doc.getData().toString()+p.getTime());
                                if (!p.getSpace().equals("0"))
                                    plan_list.add(p);
                            }
                            progress.setVisibility(View.GONE);
                            recycler_status.setVisibility(View.INVISIBLE);
                        } else {
                            progress.setVisibility(View.GONE);
                            recycler_status.setVisibility(View.VISIBLE);
                            recycler_status.setText("You haven't joined or created any plan...");
                        }
                        applyFilter();
                    }
                });

        fil_date = view.findViewById(R.id.date);
        fil_time = view.findViewById(R.id.time);
        status = view.findViewById(R.id.textView);
        final DatePickerDialog.OnDateSetListener dplistener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                fil_date.setText(i2 + "/" + (i1 + 1) + "/" + i);
                y = i;
                m = i1;
                d = i2;
            }
        };
        fil_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    DatePickerDialog dp = new DatePickerDialog(getActivity(), dplistener, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                    dp.create();
                    dp.show();
                }
            }
        });

        final TimePickerDialog.OnTimeSetListener tplistener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                //fil_time.setText((i<10?"0"+i:(i>12?i-12:i))+":"+(i1<10?"0"+i1:i1)+(i>=12?" PM":" AM"));
                h = i;
                s = i1;
                fil_time.setText((i < 10 ? "0" + i : i) + ":" + (i1 < 10 ? "0" + i1 : i1));
            }
        };
        fil_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), tplistener, 12, 0, false);
                tpd.create();
                tpd.show();
            }
        });
        view.findViewById(R.id.findPlans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (h == -1) {
                    Toast.makeText(getActivity(), "You haven't set the date", Toast.LENGTH_SHORT).show();
                    //y = m = d = h = s = -1;
                } else if (d == -1) {
                    Toast.makeText(getActivity(), "You haven't set the time", Toast.LENGTH_SHORT).show();
                    //y = m = d = h = s = -1;
                } else {
                    status.setText("SEARCH RESULTS");
                    applyFilter();
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.planner, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter) {
            if (y != -1) {
                fil_date.setText("");
                fil_time.setText("");
                status.setText("RECENT");
                y = m = d = h = s = -1;
                applyFilter();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyFilter() {
        plan_list_filtered.clear();
        if (d != -1) {
            Calendar filter = Calendar.getInstance();
            Calendar plan = Calendar.getInstance();
            filter.set(y, m, d, h, s);
            for (TravelPlan p : plan_list) {
                int y1 = Integer.parseInt(p.getDate().substring(p.getDate().lastIndexOf('.') + 1));
                int m1 = Integer.parseInt(p.getDate().substring(p.getDate().indexOf('.') + 1, p.getDate().lastIndexOf('.'))) - 1;
                int d1 = Integer.parseInt(p.getDate().substring(0, p.getDate().indexOf('.')));
                int h1 = Integer.parseInt(p.getTime().substring(0, 2));
                int s1 = Integer.parseInt(p.getTime().substring(3, 5));
                plan.set(y1, m1, d1, h1, s1);
                if (Math.abs(filter.getTimeInMillis() - plan.getTimeInMillis()) < 3600000)
                    plan_list_filtered.add(p);
            }
        } else {
            plan_list_filtered = new ArrayList<>(plan_list);
            Log.d("Refreshing Recyler", "" + plan_list.size() + "," + plan_list_filtered.size());
        }
        adapter = new PlanAdapter(plan_list_filtered, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
