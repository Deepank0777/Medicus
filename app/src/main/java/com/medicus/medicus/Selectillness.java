package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.goodiebag.pinview.Pinview;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class Selectillness extends AppCompatActivity {

    private Button nextbtn;
    private EditText name, address, email, blood_group, age;
    ViewFlipper viewFilpper1;
    private String token;
    String requestId1;
    String mobile1;
    Bundle b1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectillness);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        b1=getIntent().getExtras();
        name = (EditText) findViewById(R.id.name);
        address = (EditText) findViewById(R.id.Address);
        email = (EditText) findViewById(R.id.email);
        blood_group = (EditText) findViewById(R.id.blood_group);
        age = (EditText) findViewById(R.id.age);
        nextbtn = findViewById(R.id.nextbutton);
        viewFilpper1 =(ViewFlipper)findViewById(R.id.signup_flipper);

        final SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        final SharedPreferences.Editor editor;
        editor = share.edit();


        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(name.getText())){                                                          //if email field is empty
                    Toast.makeText(Selectillness.this,"Enter Name",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(email.getText())){                                                          //if email field is empty
                    Toast.makeText(Selectillness.this,"Enter Email",Toast.LENGTH_SHORT).show();
                    return;
                }
                                                                          //get data from intent
                if(b1!=null) {
                    String s1 = b1.getString("user_name");
                    if (s1 != null) {
                        mobile1 = "91" + s1;
                        registerUser(mobile1);
                        Pinview pinview = (Pinview) findViewById(R.id.pinview2);

                        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
                            @Override
                            public void onDataEntered(Pinview pinview, boolean fromUser) {
                                //Make api calls here or what not
                                String inputText = pinview.getValue();
                                if (inputText.length() == 4) {
                                    verifyUser(inputText, requestId1, mobile1);
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Incorrect Pin",
                                            Toast.LENGTH_LONG)
                                            .show();
                                    Log.d("Incorrect Pin", "Not Verfied");
                                }
                            }
                        });
                    } else {
                        String mobile1 = share.getString("mobile", null);
                        if (mobile1 != null) {
                            editor.putString("name", name.getText().toString());
                            editor.putString("address", address.getText().toString());
                            editor.putString("email", email.getText().toString());
                            editor.putString("blood_group", blood_group.getText().toString());
                            editor.putString("age", age.getText().toString());
                            editor.apply();
                            Toast.makeText(Selectillness.this, "Success", Toast.LENGTH_SHORT).show();
                            Intent main = new Intent(Selectillness.this,
                                    HomeScreenActivity.class);
                            Selectillness.this.startActivity(main);
                            Selectillness.this.finish();
                        } else {
                            Toast.makeText(Selectillness.this, "Enter Mobile No First", Toast.LENGTH_SHORT).show();
                            Intent main = new Intent(Selectillness.this,
                                    LoginActivity.class);
                            Selectillness.this.startActivity(main);
                            Selectillness.this.finish();
                        }
                    }
                } else {
                    String mobile1 = share.getString("mobile", null);
                    if (mobile1 != null) {
                        editor.putString("name", name.getText().toString());
                        editor.putString("address", address.getText().toString());
                        editor.putString("email", email.getText().toString());
                        editor.putString("blood_group", blood_group.getText().toString());
                        editor.putString("age", age.getText().toString());
                        editor.apply();
                        Toast.makeText(Selectillness.this, "Success", Toast.LENGTH_SHORT).show();
                        Intent main = new Intent(Selectillness.this,
                                HomeScreenActivity.class);
                        Selectillness.this.startActivity(main);
                        Selectillness.this.finish();
                    } else {
                        Toast.makeText(Selectillness.this, "Enter Mobile No First", Toast.LENGTH_SHORT).show();
                        Intent main = new Intent(Selectillness.this,
                                LoginActivity.class);
                        Selectillness.this.startActivity(main);
                        Selectillness.this.finish();
                    }
                }
            }
        });
    }

    private void registerUser(final String mobile) {
//        contact_no = mEmailView.getText().toString();

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new AsyncTask<Void, Void, String>() {
            int responseCode = 401;

            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/register";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;

                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("mobile", mobile);
                    jsonBody.put("actor", String.valueOf("user"));
                    jsonBody.put("fname", name.getText().toString());
                    jsonBody.put("email", email.getText().toString());
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
                        if (responseCode == 200) {
//                            Log.d("/*/*//*/*/*/*",""+response);
                            requestId1 = jsonObj.getString("response");
                            Log.d("ReqId",requestId1);
                            viewFilpper1.showNext();

                        } else if (responseCode == 401) {
                            Toast.makeText(getApplicationContext(),
                                    "Error in Creating User",
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
    private void verifyUser(final String OTP,final String requestId,final String contact_no) {
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
//                        Log.d("123",""+response);
                        JSONObject jsonObj = new JSONObject(response);

                        // Getting JSON Array node
                        if(responseCode==200) {
                            token = jsonObj.get("JWT").toString();
                            Toast.makeText(Selectillness.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                            changeInstance();

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
    private void changeInstance(){

        //Save the __TOKEN__ first and change the instance
        SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = share.edit();
        editor.putString("token",token);
        editor.putString("name", name.getText().toString());
        editor.putString("address", address.getText().toString());
        editor.putString("email", email.getText().toString());
        editor.putString("blood_group", blood_group.getText().toString());
        editor.putString("age", age.getText().toString());
        editor.putString("mobile",mobile1);
        editor.apply();

        Intent main = new Intent(Selectillness.this,
                HomeScreenActivity.class);
        Selectillness.this.startActivity(main);
        Selectillness.this.finish();
    }
}