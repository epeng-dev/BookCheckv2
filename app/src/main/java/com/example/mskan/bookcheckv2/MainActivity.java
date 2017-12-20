package com.example.mskan.bookcheckv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MainFragment mainFragment = new MainFragment();
    BorrowFragment borrowFragment = null;
    ReturnBookFragment returnBookFragment = null;
    TextView userName;
    TextView userGrade;
    public boolean isAdmin = false;
    public String UserID;
    public String UserToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserID = getIntent().getStringExtra("ID");
        UserToken = getIntent().getStringExtra("Token");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content_fragment_layout, mainFragment);
        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.Acountname);
        userGrade = (TextView) navigationView.getHeaderView(0).findViewById(R.id.Acountgrade);

        userName.setText(UserID);
        if(!isAdmin)
            userGrade.setText("일반사용자");
        else
            userGrade.setText("관리자");

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        WebView webView = mainFragment.webView;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(webView.canGoBack()) {
            webView.goBack();
        }else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        Bundle bundle;

        if(id == R.id.search){
            fragment = mainFragment;
            title = "책첵";
        } else if (id == R.id.borrow) {
            if(borrowFragment == null){
                borrowFragment = new BorrowFragment();
                bundle = new Bundle();
                bundle.putString("UserToken", UserToken);
                borrowFragment.setArguments(bundle);
            }
            fragment = borrowFragment;
            title = "대출";
        } else if (id == R.id.returnbook) {
                if (returnBookFragment == null) {
                    returnBookFragment = new ReturnBookFragment();
                    bundle = new Bundle();
                    bundle.putString("UserToken", UserToken);
                    returnBookFragment.setArguments(bundle);
                }
                fragment = returnBookFragment;
                title= "반납";
        } else if (id == R.id.logOut){
            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if(fragment != null){
            Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment_layout);
            if(fragment.equals(mFragment)){
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                if(getSupportActionBar() != null){
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_fragment_layout, fragment);
            fragmentTransaction.commit();
        }

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
