package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;


public class Reminder extends Fragment {

    Bundle bundle;
    String appointmentData;
    JSONObject jsonObject;
    JSONArray jsonArray;
    ParseDate pd;
    TextView startTime;
    TextView endTime;
    TextView amPm1;
    TextView amPm2;
    Button refresh;
    Button cancel;
    TextView docName;
    ProgressDialog progressDialog;
    SharedPreferences share;
    private String verify_token = null;
    String appointmentId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, null);
        startTime = view.findViewById(R.id.starting);
        endTime = view.findViewById(R.id.ending);
        amPm1 = view.findViewById(R.id.amopm);
        amPm2 = view.findViewById(R.id.amopm2);
        refresh = view.findViewById(R.id.refresh);
        cancel = view.findViewById(R.id.cancel);
        docName = view.findViewById(R.id.doc_name);
        postRequest();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRequest();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAppointment();
            }
        });

        return view;
    }

    // TODO: get a better way to solve the problem
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        share = activity.getSharedPreferences("PREFS", 0);
        verify_token = share.getString("token","");
    }


    private void postRequest() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {

            ArrayList<String> arr = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Please Wait...");
                progressDialog.setMessage("Getting All your Appointment");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/appointment/get";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;

                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("getAllAppointments", "1");
                    requestBody = Utils.buildPostParameters(jsonBody);
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
                    String temp,response = "";
                    while ((temp = bufferedReader.readLine()) != null) {
                        response += temp;
                    }
//                    Log.d("Doc",doc_response);
                    jsonArray = new JSONArray(response);
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
                //for Layout search page
                if(jsonArray!=null) {
                    try {
                        jsonObject = jsonArray.getJSONObject(0);
                        docName.setText(jsonObject.getString("first_name")+" "+jsonObject.getString("last_name"));
                        pd = new ParseDate(jsonObject.getString("probable_start_time"));
                        String[] time = pd.getParsedTime().split("_");
                        startTime.setText(time[0]);
                        appointmentId = jsonObject.getString("id");
                        amPm1.setText(time[1].toUpperCase());
                        pd = new ParseDate(jsonObject.getString("actual_end_time"));
                        time = pd.getParsedTime().split("_");
                        endTime.setText(time[0]);
                        amPm2.setText(time[1].toUpperCase());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
            }
        };
        async.execute();
    }

    private void cancelAppointment() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {

            ArrayList<String> arr = new ArrayList<>();
            JSONObject jObj;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/appointment";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;

                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("actor", "user");
                    jsonBody.put("id", appointmentId);
                    requestBody = Utils.buildPostParameters(jsonBody);
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
                    String temp,response = "";
                    while ((temp = bufferedReader.readLine()) != null) {
                        response += temp;
                    }
//                    Log.d("Doc",doc_response);
                    jObj = new JSONObject(response);
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
                //for Layout search page
                Log.d("cancel",response);
                try {
                    if(jObj.getString("response").equals("done")){
                        Toast.makeText(getContext(),
                                "Appointment canceled",
                                Toast.LENGTH_LONG)
                                .show();
                        appointmentCancel();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        };
        async.execute();
    }
    public void appointmentCancel() {
        NotificationManagerCompat my = NotificationManagerCompat.from(getActivity());
        NotificationCompat.Builder myNoti = new NotificationCompat.Builder(getActivity());
        myNoti.setContentText("Appointment Canceled Successfully");
        myNoti.setContentTitle("MEDICUS");
        myNoti.setSmallIcon(android.R.drawable.arrow_down_float);
        Intent i1 = new Intent(getActivity(), Reminder.class);
        PendingIntent pd = PendingIntent.getActivity(getActivity(), 1, i1, 0);
        myNoti.setContentIntent(pd);
        myNoti.setAutoCancel(true);
        my.notify(1, myNoti.build());

    }
}
