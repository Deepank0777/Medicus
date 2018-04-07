package com.medicus.medicus;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class Appointment_calender extends AppCompatActivity {


//    public Appointment_calender() {
//        // Required empty public constructor
//    }
    String doctorId = null;
    String offcId = null;
    CalendarView calendarView;
    DatePicker datePicker;
    JSONObject jsonObject;
    Button button;
    RadioGroup radioGroup;
    String avgTime;
    String start;
    String end;
    String day;
    String JWT;
    JSONObject avibility;
    String lastAppointmentTime;
    Calendar calendar = Calendar.getInstance();
    ProgressBar spinner;

    TextView availInfo;
    TextView totalInfo;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_calender);
        SharedPreferences preferences = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        JWT = preferences.getString("token",null);
        savedInstanceState = getIntent().getExtras();
        if(savedInstanceState!= null){
            doctorId = savedInstanceState.getString("doctor_id",null);
        }
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        getDoctorAvailability(doctorId);
        datePicker = findViewById(R.id.datePicker);
        datePicker.setMinDate(System.currentTimeMillis());
        availInfo = findViewById(R.id.avail_info);
        totalInfo = findViewById(R.id.total_info);
        button = findViewById(R.id.Bokbutton);
        radioGroup = findViewById(R.id.slots);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioGroup.getCheckedRadioButtonId() <0){
                    Toast.makeText(getApplicationContext(),
                            "Please select a Slot",
                            Toast.LENGTH_LONG)
                            .show();
                } else{
                    RadioButton rb= findViewById(radioGroup.getCheckedRadioButtonId());
                    String s = (String) rb.getText();
                    int day = datePicker.getDayOfMonth();
                    int month = datePicker.getMonth() + 1;
                    int year = datePicker.getYear();
                    String date = String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day);
                    String parseDate = String.valueOf(day)+"/"+String.valueOf(month)+"/"+String.valueOf(year);
                    String dayOfWeek = getDayOfWeek(day,month,year);
                    makeAppointment(s,date,dayOfWeek);
                }
            }
        });

        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int _year, int _month, int _day) {
                if(radioGroup.getCheckedRadioButtonId() <0){
                    Toast.makeText(getApplicationContext(),
                            "Please select a Slot",
                            Toast.LENGTH_LONG)
                            .show();
                } else{
                    RadioButton rb= findViewById(radioGroup.getCheckedRadioButtonId());
                    String s = (String) rb.getText();
                    int day = _day;
                    int month = _month;
                    int year = _year;
                    String date = String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(day);
                    String parseDate = String.valueOf(day)+"/"+String.valueOf(month)+"/"+String.valueOf(year);
                    String dayOfWeek = getDayOfWeek2(day,month,year);
                    getAvailability(s,date,dayOfWeek);
                }
            }
        });
    }

    private String getDayOfWeek(int day, int month, int year){
        String dayOfTheWeek = null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
//        GregorianCalendar GregorianCalendar = new GregorianCalendar(year,month,day-1);
        Date d_name = new Date(year,month,day-3);
        dayOfTheWeek = sdf.format(d_name);
        Log.d("day",dayOfTheWeek);
        return dayOfTheWeek;
    }
    private String getDayOfWeek2(int day, int month, int year){
        String dayOfTheWeek = null;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
//        GregorianCalendar GregorianCalendar = new GregorianCalendar(year,month,day-1);
        Date d_name = new Date(year,month,day-1);
        dayOfTheWeek = sdf.format(d_name);
        Log.d("day",dayOfTheWeek);
        return dayOfTheWeek;
    }

    private void getDoctorAvailability(final String id) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new AsyncTask<Void, Void, String>() {
            int responseCode = 401;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {

                // CASE 2: For JSONObject parameter
                String url = SplashScreenActivity.url+"/api/v1/doctor/info";
                JSONObject jsonBody;
                String requestBody;
                HttpURLConnection urlConnection = null;
                try {
                    jsonBody = new JSONObject();
                    jsonBody.put("doc_id", id);
//                    jsonBody.put("action", "get_doc_office");
                    requestBody = Utils.buildPostParameters(jsonBody);
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, JWT, "application/json", requestBody);

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
//                    Log.d("Doctor",response);
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
                        JSONArray jsonObj = new JSONArray(response);
                        disableDates(jsonObj);
                    } catch (final JSONException e) {
                        Log.e("JSON", "Json parsing error: " + e.getMessage());

                    }
                }
                spinner.setVisibility(View.GONE);
            }
        };
        async.execute();
    }

    private void disableDates(JSONArray jsonArray){
        JSONObject available = null;
        for (int i=0; i<jsonArray.length();i++){
            try {
                if(!jsonArray.getJSONObject(i).getString("availability").equals("null")){
                    offcId = jsonArray.getJSONObject(i).getString("office_id");
                    avgTime = jsonArray.getJSONObject(i).getString("time_slot_per_client_in_min");
                    avibility = new JSONObject(jsonArray.getJSONObject(i).getString("availability"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeAppointment(final String radioId,final String date, final String day) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new AsyncTask<Void, Void, String>() {
            int responseCode = 401;
            JSONObject jsonBody1;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    String slotsTime = avibility.getString(day);
                    JSONObject morning = new JSONObject(slotsTime).getJSONObject("Morning");
                    JSONObject afternoon = new JSONObject(slotsTime).getJSONObject("Noon");
                    JSONObject evening = new JSONObject(slotsTime).getJSONObject("Evening");
                    if(radioId.equals("Morning")){
                        start = date+"T"+(morning.getString("start_time")+":00");
                        end = date+"T"+(morning.getString("end_time")+":00");
                    } else if(radioId.equals("Afternoon")){
                        start = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("start_time"))+12)+":00");
                        end = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("end_time"))+12)+":00");
                    } else if(radioId.equals("Evening")){
                        start = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("start_time"))+12)+":00");
                        end = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("end_time"))+12)+":00");
                    }
                    jsonBody1 = new JSONObject();
                    jsonBody1.put("start_time", start);
                    jsonBody1.put("end_time", end);
                    jsonBody1.put("avg_time", avgTime);
                    Log.d("MyREquest", radioId);
                    Log.d("MyREquest", start+" "+end);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    jsonBody.put("office_id", offcId);
                    jsonBody.put("slot", jsonBody1);
                    jsonBody.put("last_appointment_end_time",lastAppointmentTime);
                    requestBody = Utils.buildPostParameters(jsonBody);
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, JWT, "application/json", requestBody);

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
//                    Log.d("Doctor",response);
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
                    if(responseCode == 200){
                        Log.d("AppointmentDone","YEssssssssssssssssssssssssss");
                        Toast.makeText(getApplicationContext(),
                                "Your Appointment is done",
                                Toast.LENGTH_SHORT)
                                .show();
                        doSomething();
                        Intent intent = new Intent(Appointment_calender.this,HomeScreenActivity.class);
                        Appointment_calender.this.startActivity(intent);
                        Appointment_calender.this.finish();
                    }
                    if(responseCode == 401){
                        Log.d("AppointmentNotDone","nooooooooooooooooo");
                        Toast.makeText(getApplicationContext(),
                                "Sorry the appointment was not scheduled!!!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        };
        async.execute();
    }

    private void getAvailability(final String radioId,final String date, final String day) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new AsyncTask<Void, Void, String>() {
            int responseCode = 401;

            JSONObject jsonBody1;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                try {
                    String slotsTime = avibility.getString(day);
                    JSONObject morning = new JSONObject(slotsTime).getJSONObject("Morning");
                    JSONObject afternoon = new JSONObject(slotsTime).getJSONObject("Noon");
                    JSONObject evening = new JSONObject(slotsTime).getJSONObject("Evening");
                    if(radioId.equals("Morning")){
                        start = date+"T"+(morning.getString("start_time")+":00");
                        end = date+"T"+(morning.getString("end_time")+":00");
                    } else if(radioId.equals("Afternoon")){
                        start = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("start_time"))+12)+":00");
                        end = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("end_time"))+12)+":00");
                    } else if(radioId.equals("Evening")){
                        start = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("start_time"))+12)+":00");
                        end = date+"T"+(String.valueOf(Integer.parseInt(morning.getString("end_time"))+12)+":00");
                    }
                    jsonBody1 = new JSONObject();
                    jsonBody1.put("start_time", start);
                    jsonBody1.put("end_time", end);
                    jsonBody1.put("avg_time", avgTime);
                    Log.d("MyREquest", radioId);
                    Log.d("MyREquest", start+" "+end);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                spinner.setVisibility(View.VISIBLE);
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
                    jsonBody.put("getAvailability", "1");
                    jsonBody.put("office_id", offcId);
                    jsonBody.put("slot", jsonBody1);
//                    jsonBody.put("action", "get_doc_office");
                    requestBody = Utils.buildPostParameters(jsonBody);
                    urlConnection = (HttpURLConnection) Utils.makeRequest("POST", url, JWT, "application/json", requestBody);

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
//                    Log.d("Doctor",response);
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
                        Log.d("response",response);
                        availInfo.setText(jsonObj.getString("Available slots"));
                        totalInfo.setText(jsonObj.getString("Total Slots"));
                        lastAppointmentTime = jsonObj.getString("last_appointment_time");
                        if(jsonObj.getString("Available slots").equals("null")){
                            Toast.makeText(getApplicationContext(),
                                    "No Slots available",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                        Log.d("availableData",availInfo.getText()+" "+totalInfo.getText()+" "+lastAppointmentTime);
                    } catch (final JSONException e) {
                        Log.e("JSON", "Json parsing error: " + e.getMessage());
                        if(e.getMessage().equals("Value <!DOCTYPE of type java.lang.String cannot be converted to JSONObject")){
                            Toast.makeText(getApplicationContext(),
                                    "Doctor not available",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
                spinner.setVisibility(View.GONE);
            }
        };
        async.execute();
    }
    public void doSomething() {
        NotificationManagerCompat my = NotificationManagerCompat.from(this);
        NotificationCompat.Builder myNoti = new NotificationCompat.Builder(this);
        myNoti.setContentText("Appointment Booked Successfully");
        myNoti.setContentTitle("MEDICUS");
        myNoti.setSmallIcon(android.R.drawable.arrow_down_float);
        Intent i1 = new Intent(this, Appointment_calender.class);
        PendingIntent pd = PendingIntent.getActivity(this, 1, i1, 0);
        myNoti.setContentIntent(pd);
        myNoti.setAutoCancel(true);
        my.notify(1, myNoti.build());
        finish();
    }
}
