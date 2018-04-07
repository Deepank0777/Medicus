package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ReScheduleActivity extends AppCompatActivity {
    Button reschedule;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_schedule);
        reschedule = findViewById(R.id.reschedule);
        cancel = findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ReScheduleActivity.this,
                        "You are updated into rescheduling Queue",
                        Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(ReScheduleActivity.this, HomeScreenActivity.class);
                ReScheduleActivity.this.startActivity(intent);
                ReScheduleActivity.this.finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRequest();
            }
        });
    }

    private void postRequest() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new AsyncTask<Void, Void, String>() {

            ArrayList<String> arr = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected String doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                //for Layout search page
                Toast.makeText(ReScheduleActivity.this,
                        "Appointment Canceled",
                        Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(ReScheduleActivity.this, HomeScreenActivity.class);
                ReScheduleActivity.this.startActivity(intent);
                ReScheduleActivity.this.finish();
            }
        };
        async.execute();
    }
}