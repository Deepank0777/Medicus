package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 * Created by utsav on 30-03-2018.
 */

public class ReminderActivity extends AppCompatActivity {

    Bundle bundle;
    String appointmentData;
    JSONObject jsonObject;
    ParseDate pd;
    TextView docName;
    TextView startTime;
    TextView endTime;
    TextView amPm1;
    TextView amPm2;
    Button refresh;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reminder);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        startTime = findViewById(R.id.starting);
        endTime = findViewById(R.id.ending);
        amPm1 = findViewById(R.id.amopm);
        amPm2 = findViewById(R.id.amopm2);
        refresh = findViewById(R.id.refresh);
        docName = findViewById(R.id.doc_name);

        bundle=getIntent().getExtras();
        if(bundle != null){
            appointmentData = bundle.getString("appointmentData");
            try {
                jsonObject = new JSONObject(appointmentData);
                docName.setText(jsonObject.getString("first_name")+" "+jsonObject.getString("last_name"));
                pd = new ParseDate(jsonObject.getString("probable_start_time"));
                String[] time = pd.getParsedTime().split("_");
                startTime.setText(time[0]);
                amPm1.setText(time[1].toUpperCase());
                pd = new ParseDate(jsonObject.getString("actual_end_time"));
                time = pd.getParsedTime().split("_");
                endTime.setText(time[0]);
                amPm2.setText(time[1].toUpperCase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loaddata();
            }
        });
    }


    private void loaddata() {
//        contact_no = mEmailView.getText().toString();

        SharedPreferences share = getSharedPreferences("PREFS", MODE_PRIVATE);
        final String verify_token = share.getString("token","");



        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> async = new  AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(ReminderActivity.this);
                progressDialog.setTitle("Please Wait...");
                progressDialog.setMessage("Refreshing the List");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                SystemClock.sleep(1500);
                return null;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                progressDialog.dismiss();
            }
        };
        async.execute();
    }
}
