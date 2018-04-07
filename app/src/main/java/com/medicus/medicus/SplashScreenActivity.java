package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class SplashScreenActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;

    public static String url = "http://"+"192.168.137.1"+":3000";
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        mHideHandler.postDelayed(changeInstance, 1000);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(1);
    }

    private final Runnable changeInstance = new Runnable(){
        public void run() {
            postRequest();

        }
    };

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }




    private void postRequest() {
//        contact_no = mEmailView.getText().toString();

        SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        final String verify_token = share.getString("token","");



        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/search";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;
                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("getAllSpecialization", "1");
                    requestBody = Utils.buildPostParameters(jsonBody);
//                    Log.d("JWT","xxxx-yyyy-zzzz");
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, verify_token, "application/json", requestBody);

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
                    Log.d("Response",String.valueOf(urlConnection.getResponseCode()));
                    //Save the __TOKEN__ first and change the instance
                    SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor editor;
                    editor = share.edit();
                    editor.putString("AllSpecialization",response);
                    editor.apply();
//                    Log.d("Specialization",share.getString("AllSpecialization",""));
                    response = String.valueOf(urlConnection.getResponseCode());
                    return response;
                } catch (JSONException | IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Cannot Connect To Internet",
                                    Toast.LENGTH_LONG)
                                    .show();
                            Intent main = new Intent(SplashScreenActivity.this,
                                    HomeScreenActivity.class);
                            SplashScreenActivity.this.startActivity(main);
                            SplashScreenActivity.this.finish();
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
                //get Value of otp
                if (response.equals("403")) {
                        String verification = "false";
                        checkLogin(verification);
                } else if(response.equals("200")){
                    checkLogin("true");
                }
            }
        };
        async.execute();
    }


    private void checkLogin(String status){
        if(status.equals("true")){
//            Log.d("TO Illnesss screen","Verified");
            Intent main = new Intent(SplashScreenActivity.this,
                    HomeScreenActivity
                            .class);
            SplashScreenActivity.this.startActivity(main);
            SplashScreenActivity.this.finish();
        } else if(status.equals("false")){
//            Log.d("TO Login screen","Not Verified");
            Intent main = new Intent(SplashScreenActivity.this,
                    IntroActivity1.class);
            SplashScreenActivity.this.startActivity(main);
            SplashScreenActivity.this.finish();
        }
    }


    //    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }

    // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

    //    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }
}
