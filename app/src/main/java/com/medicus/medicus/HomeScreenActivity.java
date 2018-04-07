package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static android.content.Context.MODE_PRIVATE;

public class HomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String verify = "false";
    NavigationView nav_view;
    ViewPager viewPager;
    BottomNavigationView navigation;
    MenuItem prevMenuItem;
    Toolbar toolbar;

    //Fragment Variables
    User_Profile user_profile;
    Appointments appointments;
    ContentHomeScreen contentHomeScreen;
    Favourite favourite;
    Reminder reminder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nav_view =  findViewById(R.id.nav_view);
        viewPager = findViewById(R.id.viewpager);
//        this.postRequest();
        this.checkLogin();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);

        viewPager.addOnPageChangeListener(onPageChangeListener);
        setupViewPager(viewPager);
//        viewPager.beginFakeDrag();
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
//        viewPager.setOnTouchListener(new );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        user_profile = new User_Profile();
        appointments = new Appointments();
        contentHomeScreen = new ContentHomeScreen();
        favourite = new Favourite();
        reminder = new Reminder();
        viewPagerAdapter.addFragment(contentHomeScreen);    // Index: 0
        viewPagerAdapter.addFragment(reminder);             // Index: 1
        viewPagerAdapter.addFragment(favourite);            // Index: 2
        viewPagerAdapter.addFragment(user_profile);         // Index: 3
        viewPagerAdapter.addFragment(appointments);         // Index: 4
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener(){
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
        @Override
        public void onPageSelected(int position) {
            if (prevMenuItem != null) {
                prevMenuItem.setChecked(false);
            } else {
                navigation.getMenu().getItem(0).setChecked(false);
            }
            switch (position){
                case 0:
                    toolbar.setTitle("Home");
                    navigation.getMenu().getItem(0).setChecked(true);
                    break;
                case 1:
                    toolbar.setTitle("Reminder");
                    navigation.getMenu().getItem(1).setChecked(true);
                    break;
                case 2:
                    toolbar.setTitle("Likes");
                    navigation.getMenu().getItem(2).setChecked(true);
                    break;
                case 3:
                    toolbar.setTitle("Profile");
                    navigation.getMenu().getItem(3).setChecked(true);
                    break;
                case 4:
                    // My Appointment Screen from the Drawer Menu
                    toolbar.setTitle("My Appointment");
                    break;
            }
            if(position < 4) {
                prevMenuItem = navigation.getMenu().getItem(position);
            }
        }
        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private  void checkLogin() {
        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String verify="false";
        if (preferences.getString("token",null)!=null){
            verify="true";
        }
        if (verify.equals("false")) {
            nav_view.getMenu().setGroupVisible(R.id.main_group, false);
            nav_view.getMenu().findItem(R.id.nav_logout).setVisible(false);
            nav_view.getMenu().findItem(R.id.nav_login).setVisible(true);
        } else if(verify.equals("true")){
            String refreshedToken=preferences.getString("Reference_token",null);
            String JWT=preferences.getString("token",null);
            sendRegistrationToServer(refreshedToken,JWT);
            nav_view.getMenu().setGroupVisible(R.id.main_group, true);
            nav_view.getMenu().findItem(R.id.nav_logout).setVisible(true);
            nav_view.getMenu().findItem(R.id.nav_login).setVisible(false);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Menu m = navigation.getMenu();
            for (int i=0;i<m.size();i++) {
                MenuItem mi = m.getItem(i);
                if (!(mi.getItemId() == item.getItemId())) {
                    mi.setCheckable(false);
                }
            }
            item.setCheckable(true);
            item.setChecked(true);
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    fragment = new ContentHomeScreen();
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.reminder:
//                    fragment = new Appointments();
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.likes:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.profile:
//                    fragment = new User_Profile();
                    viewPager.setCurrentItem(3);
                    break;
            }
            return true;
        }
    };

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.notification_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.notification) {
//            Toast.makeText(getApplicationContext(),"Notification",Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        android.support.v4.app.Fragment fragment = null;
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            navigation.getMenu().setGroupCheckable(0,true,true);
            viewPager.setCurrentItem(3);
        } else if (id == R.id.nav_appointment) {
            navigation.getMenu().setGroupCheckable(0,false,false);
            viewPager.setCurrentItem(4);
        } else if (id == R.id.nav_reminder) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_TOU) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_logout) {
            //Save the __TOKEN__ first and change the instance
            SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = share.edit();
            editor.putString("token",null);
            editor.apply();

            Intent main = new Intent(HomeScreenActivity.this,
                    LoginActivity.class);
            HomeScreenActivity.this.startActivity(main);
            HomeScreenActivity.this.finish();
        } else if (id == R.id.nav_login) {
            //Save the __TOKEN__ first and change the instance
            SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = share.edit();
            editor.putString("token",null);
            editor.putInt("isIntroDone",0);
            editor.apply();

            Intent main = new Intent(HomeScreenActivity.this,
                    IntroActivity1.class);
            HomeScreenActivity.this.startActivity(main);
            HomeScreenActivity.this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    //check if the user is logged in or out
    private void postRequest() {

        SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        final String verify_token = share.getString("token","");

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"api/v1/login/verify";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;
                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("JWT", verify_token);
                    requestBody = Utils.buildPostParameters(jsonBody);
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, null, "application/json", requestBody);

                    // the same logic to case #1
                    InputStream inputStream;
                    // get stream
                    if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        inputStream = urlConnection.getInputStream();
                    } else {
                        inputStream = urlConnection.getErrorStream();
                    }
                    // parse stream
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String temp,response = "";
                    while ((temp = bufferedReader.readLine()) != null) {
                        response += temp;
                    }
                    return response;
                } catch (JSONException | IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Cannot Connect To Internet",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    e.printStackTrace();
                    return e.toString();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                // do something...
                // Verify the user
                if (response != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);
                        String verification = jsonObj.get("status").toString();
                        verify = verification;
                    } catch (final JSONException e) {
                        Log.e("JSON", "Json parsing error: " + e.getMessage());
                    }
                }
                checkLogin();
            }
        };
        async.execute();
    }

    private void sendRegistrationToServer(final String token , final String JWT) {
        // TODO: Implement this method to send token to your app server.

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {
            int responseCode = 401;
            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/users";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;

                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("refreshToken", token);
                    requestBody = Utils.buildPostParameters(jsonBody);
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, null, "application/json", requestBody);

                    // the same logic to case #1
                    InputStream inputStream;
                    // get stream
                    if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                        inputStream = urlConnection.getInputStream();
                    } else {
                        inputStream = urlConnection.getErrorStream();
                    }
                    // parse stream
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String temp, response = "";
                    while ((temp = bufferedReader.readLine()) != null) {
                        response += temp;
                    }
                    responseCode = urlConnection.getResponseCode();
                    return response;
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    return e.toString();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                // do something...
                //get Value of otp
                if (response != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(response);

                        // Getting JSON Array node
                        if(responseCode==200) {
                        } else if(responseCode == 401){
                        }
                    } catch (final JSONException e) {
                    }
                }
            }
        };
        async.execute();
    }

}
