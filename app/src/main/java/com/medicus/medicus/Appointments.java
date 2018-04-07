package com.medicus.medicus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Appointments extends android.support.v4.app.Fragment {
    ArrayList<String> mName = new ArrayList<>();
    ArrayList<String> appointLoc = new ArrayList<>();
    ArrayList<String> appointTime = new ArrayList<>();
    ArrayList<String> mimage = new ArrayList<>();
    ListView list;
    SharedPreferences share;
    private String verify_token = null;
    JSONObject jObj;
    JSONArray jsonArray_special;
    String JWT;
    Button refresh;

    String appointmentResponse;
    View view;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_appointments, null);
        list = view.findViewById(R.id.appointments_list);
        refresh = view.findViewById(R.id.refresh);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),
                        ReminderActivity.class);
                try {
                    jObj = jsonArray_special.getJSONObject(position);
                    intent.putExtra("appointmentData",jObj.toString());
                    intent.putExtra("fragToOpen","reminder");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getActivity().startActivity(intent);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                list.setAdapter(null);
                postRequest();
            }
        });
        //v1/appointment/get parameter=> actor:
        return view;
    }

    // TODO: get a better way to solve the problem
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        share = activity.getSharedPreferences("PREFS", 0);
        verify_token = share.getString("token","");
        postRequest();
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        verify_token = share.getString("token","");
//        list = view.findViewById(R.id.appointments_list);
//        postRequest();
//    }


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
                    appointmentResponse = response;
//                    Log.d("Doc",doc_response);
                    jsonArray_special = new JSONArray(appointmentResponse);
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
                if(jsonArray_special!=null) {
                    try {

                        for (int i = 0; i < jsonArray_special.length(); i++) {
                            jObj = jsonArray_special.getJSONObject(i);
                            mName.add(jObj.getString("first_name") + " " + jObj.getString("last_name"));
                            appointLoc.add(jObj.getString("street_address"));
                            appointTime.add(jObj.getString("probable_start_time"));
                            mimage.add(jObj.getString("profile_image_url"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Adapter1 adapter = new Adapter1(getContext(),
                            mName.toArray(new String[mName.size()]),
                            appointLoc.toArray(new String[appointLoc.size()]),
                            appointTime.toArray(new String[appointTime.size()]),
                            mimage.toArray(new String[mimage.size()]));
                    list.setAdapter(adapter);
                }
                progressDialog.dismiss();
            }
        };
        async.execute();
    }
}

class Adapter1 extends ArrayAdapter<String> {
    Context context;
    String[] images;
    String[] doc_name;
    String[] appoint_loc;
    String[] appoint_time;

    Adapter1(Context c,String[] name,String[] loc, String[] time,String[] imgs){
        super(c,R.layout.single_row2,R.id.appointment_name,name);
        this.context=c;
        this.images=imgs;
        this.doc_name = name;
        this.appoint_loc=loc;
        this.appoint_time=time;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.single_row2,parent,false);

        ImageView listImage=(ImageView) row.findViewById(R.id.doc_image);
        TextView docName=(TextView)row.findViewById(R.id.appointment_name);
        TextView appointLoc=(TextView)row.findViewById(R.id.appointment_location);
        TextView appointDate=(TextView)row.findViewById(R.id.appointment_date);
        TextView appointTime=(TextView)row.findViewById(R.id.appointment_time);

        docName.setText(doc_name[position]);
        appointLoc.setText("Address: "+appoint_loc[position]);
        ParseDate pd = new ParseDate(appoint_time[position]);

        appointTime.setText("Time: "+pd.getParsedTime());
        appointDate.setText("Date: "+pd.getParsedDate());
        if(position<images.length) {
            getImage(listImage, images[position], position);
        } else{
            getImage(listImage, images[position-1], position);
        }
        notifyDataSetChanged();
        return row;

    }
    private void getImage(final ImageView imageView,final String imageUrl,final int position) {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Bitmap> async = new  AsyncTask<Void, Void, Bitmap>() {
            Bitmap imageBitmap;
            @Override
            protected Bitmap doInBackground(Void... params) {
                // CASE 2: For JSONObject parameter
                try {
                    URL url2 = new URL(imageUrl);
                    imageBitmap = BitmapFactory.decodeStream(url2.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return imageBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap response) {
                super.onPostExecute(response);
                imageView.setImageBitmap(response);
            }
        };
        async.execute();
    }
}

