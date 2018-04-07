package com.medicus.medicus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DoctorProfile extends AppCompatActivity {
    Toolbar toolbar;
    Bundle bundle;
    ImageView docImage;
    Button bookAppointment;
    TextView docDesignation;
    RatingBar docRating;
    String docId;
    ArrayList<String> ids = new ArrayList<>();
    String specialization = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);
        bundle = getIntent().getExtras();
        String docName = bundle.getString("docName");
        String designation = bundle.getString("docSpecial");
//        String image = bundle.getString("docSpecial");
        toolbar =  findViewById(R.id.toolbar);
        docId = bundle.getString("docId");
        toolbar.setTitle(docName);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        bookAppointment=(Button)findViewById(R.id.bookAppointment);
        mViewPager = findViewById(R.id.doc_content);
        tabLayout.setupWithViewPager(mViewPager);

        SharedPreferences share = getSharedPreferences("PREFS",MODE_PRIVATE);
        try {
            JSONArray jsonArray_special = new JSONArray(share.getString("AllSpecialization",""));
            for(int i = 0; i < jsonArray_special.length(); i++){
                JSONObject jObj = jsonArray_special.getJSONObject(i);
                if(jObj.getString("id").equals(designation)) {
                    specialization = jObj.getString("specialization_name");
                    Log.d("Specialization", specialization);
                    if (specialization != null) {
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        docDesignation = findViewById(R.id.doc_designation);
        docRating = findViewById(R.id.ratingBar);
        docRating.setRating(4);
        docDesignation.setText(specialization);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        bookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loaddata();
                Intent intent = new Intent(DoctorProfile.this,Appointment_calender.class);
                intent.putExtra("doctor_id",docId);
                intent.putExtra("office_id",docId);
                DoctorProfile.this.startActivity(intent);
                DoctorProfile.this.finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.notification) {
            Toast.makeText(getApplicationContext(),"Notification",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

     class SectionsPagerAdapter extends FragmentPagerAdapter {
         SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return new GeneralDocInfo();
                case 1:
                    return new ClinicDocInfo();
                case 2:
                    return new ReviewDocInfo();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title =null;
            switch (position){
                case 0:
                    title = "General";
                    break;
                case 1:
                    title = "Location";
                    break;
                case 2:
                    title = "Review";
                    break;
            }
            return title;
        }
    }
}
