package com.crux.pratd.travelbphc;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
        ArrayList<String> options = new ArrayList<>();
        options.add("Cab Sharing");
        options.add("Option 2");
        GridView grid = findViewById(R.id.gridOptions);
        CustomAdapter adapter = new CustomAdapter(options);
        grid.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public class CustomAdapter  extends BaseAdapter {
        ArrayList<String> list;
        public CustomAdapter(ArrayList<String> list) { // isFriend, isExplorer, isOwner
            this.list=list;
        }
        public int getCount() {
            return list.size();
        }
        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView==null) convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_section,parent,false);
            switch (position){
                case 0:
                    convertView.setBackgroundColor(Color.parseColor("#FDB813"));
                    ((TextView)convertView.findViewById(R.id.sectionText)).setText("Cab Sharing");
                    ((ImageView)convertView.findViewById(R.id.sectionLogo)).setImageResource(R.drawable.cab_icon);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ChooserActivity.this, plannerActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;

                default:
                    ((TextView)convertView.findViewById(R.id.sectionText)).setText("Position "+position);
                    convertView.setOnClickListener(null);
            }
            return convertView;
        }
    }
}
