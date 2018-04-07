package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import android.widget.ViewFlipper;

import com.goodiebag.pinview.Pinview;
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */

    private String otp;
    private String token;
    private String requestId;
    private String response1;

    private String contact_no;
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mLoginFormView;
    ViewFlipper viewFilpper;
    Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        viewFilpper = (ViewFlipper) findViewById(R.id.login_flipper);
//        populateAutoComplete();

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEmailView.getText().toString().length()==10){
                    try {
                        postRequest();
                    } catch (Exception e) {
                        Log.e("Url", "URL Exception");
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Invalid Number",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Pinview pinview = (Pinview) findViewById(R.id.pinview);

        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                //Make api calls here or what not
                String inputText = pinview.getValue();
                if (inputText.length() == 4) {
                    verifyUser(inputText);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Incorrect Pin",
                            Toast.LENGTH_LONG)
                            .show();
                    Log.d("Incorrect Pin", "Not Verfied");
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent main = new Intent(LoginActivity.this,
                IntroActivity1.class);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LoginActivity.this.startActivity(main);
        LoginActivity.this.finish();
    }

    private void changeInstance(String name,String email,String dob , String blood){

        //Save the __TOKEN__ first and change the instance
        SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = share.edit();
        editor.putString("token",token);
        editor.putString("mobile",mEmailView.getText().toString());
        editor.putString("name", name);
        editor.putString("email", email);
        if (blood!=null){editor.putString("blood_group", blood);}
        if (dob!=null){editor.putString("age", dob);}
        editor.apply();

        Intent main = new Intent(LoginActivity.this,
                HomeScreenActivity.class);
        LoginActivity.this.startActivity(main);
        LoginActivity.this.finish();
    }

    private void postRequest() {
        contact_no = "91"+mEmailView.getText().toString()+"_user";

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/login";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;
                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("mobile", contact_no);
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
                    response1=String.valueOf(urlConnection.getResponseCode());
//                    Log.d("Response",response);
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
                        String response_msg = jsonObj.getString("response");
                        Log.d("!!!!!!!!!!",response_msg);
                        Log.d("!!!!!!!!!!123",response1);
                        //if user is new
                        if (response_msg.equals("User Not Found")){
                            Intent main = new Intent(LoginActivity.this,
                                    Selectillness.class);
                            requestId = jsonObj.getString("response");
                            main.putExtra("user_name",mEmailView.getText().toString());
                            LoginActivity.this.startActivity(main);
                            LoginActivity.this.finish();
                        }
                        else {
                            Log.d("@@@@@@@@@@",response_msg);
                            viewFilpper.showNext();
                        }
                        // Getting JSON Array node
                        requestId = jsonObj.getString("response");


//                        otp = jsonObj.get("pin").toString();
//                        token = jsonObj.get("JWT").toString();
//                        Log.d("Response", token);
                    } catch (final JSONException e) {
                        Log.e("JSON", "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }
            }
        };
        async.execute();
    }

    private void verifyUser(final String OTP) {
//        contact_no = mEmailView.getText().toString();

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {
            int responseCode = 401;
            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/login/verify";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;

                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("requestId", requestId);
                    jsonBody.put("pin", OTP);
                    jsonBody.put("mobile",contact_no);
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
                        Log.d("User Data",""+jsonObj);
                        // Getting JSON Array node
                        if(responseCode==200) {
                            token = jsonObj.get("JWT").toString();
                            JSONObject jsonObj1=(org.json.JSONObject)jsonObj.get("data");
                            JSONObject jsonObj2=(org.json.JSONObject)jsonObj1.get("data");
                            String name = jsonObj2.get("first_name").toString();
                            String email = jsonObj2.get("email").toString();
                            String dob = jsonObj2.get("date_of_birth").toString();
                            String blood = jsonObj2.get("blood_group").toString();
                            changeInstance(name,email,dob,blood);
                        } else if(responseCode == 401){
                            Toast.makeText(getApplicationContext(),
                                    "Incorrect Pin please verify",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    } catch (final JSONException e) {
                        Log.e("JSON", "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }
            }
        };
        async.execute();
    }


}
