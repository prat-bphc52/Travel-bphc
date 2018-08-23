package com.crux.pratd.travelbphc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class plannerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Fragment fragment = new Search();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        setupProfile();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu contents = navigationView.getMenu();
        MenuItem mt = contents.getItem(0);
        mt.setChecked(true);
        myCalendar = Calendar.getInstance();


        /*new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/"+fbProfile.getId()+"/groups",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here
                        Log.d("fbGroup info",response+"");

                    }
                }).executeAsync();*/

        /*TextView reqCount=(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_req));
        reqCount.setGravity(Gravity.CENTER_VERTICAL);
        reqCount.setText("1");*/
    }

    public void setupProfile() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        TextView uName = hView.findViewById(R.id.userName);
        ImageView iv = hView.findViewById(R.id.userDP);
        final ProgressBar profileLoad = hView.findViewById(R.id.progressBar2);
        Picasso.with(getApplicationContext()).load(LoginActivity.user.getPhotoUrl()).into(iv, new Callback() {
            @Override
            public void onSuccess() {
                profileLoad.setVisibility(View.GONE);
            }

            @Override
            public void onError() {

            }
        });
        uName.setText(LoginActivity.user.getDisplayName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment frag = null;
        if (id == R.id.nav_search) frag = new Search();
        else if (id == R.id.nav_my_plans) frag = new MyPlans();
        else if (id == R.id.nav_req) frag = new Requests();
        else if (id == R.id.time_line) frag = new Timeline();
        else if (id == R.id.log_out) {
            //Sign out of firebase
            FirebaseAuth.getInstance().signOut();

            //Sign out of google account
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .setHostedDomain(getString(R.string.domain))
                    .requestIdToken(getString(R.string.Web_Client_Api_Key))
                    .requestEmail()
                    .requestProfile()
                    .build();
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);
            signInClient.signOut();

            //Show login screen
            Intent intent = new Intent(plannerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, frag);
        ft.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
