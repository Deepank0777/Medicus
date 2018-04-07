package com.medicus.medicus;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class User_Profile extends android.support.v4.app.Fragment {

    TextView name,address,contact_no,email,blood,age;
    ImageView edit_profile;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.activity_user__profile, container, false);
        name=(TextView)rootView.findViewById(R.id.user_name);
        contact_no=(TextView)rootView.findViewById(R.id.user_contact_no);
        address=(TextView)rootView.findViewById(R.id.user_address);
        email=(TextView)rootView.findViewById(R.id.user_email);
        blood=(TextView)rootView.findViewById(R.id.user_blood_group);
        edit_profile=(ImageView)rootView.findViewById(R.id.edit_profile);
        age=(TextView) rootView.findViewById(R.id.user_dob);
        SharedPreferences preferences = this.getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String name1=preferences.getString("name",null);
        String add1=preferences.getString("address",null);
        String mobile1=preferences.getString("mobile",null);
        String email1=preferences.getString("email",null);
        String blood1=preferences.getString("blood_group",null);
        String age1=preferences.getString("age",null);
        Log.d("555555555555",""+preferences.getString("Reference_token",null));

        name.setText(name1);
        if (mobile1!=null){
            contact_no.setText(add1);
        }
        contact_no.setText(mobile1);
        address.setText(add1);
        email.setText(email1);
        blood.setText(blood1);
        age.setText(age1+"year old");


        edit_profile.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main = new Intent(getActivity(),Selectillness.class);
                User_Profile.this.startActivity(main);
            }
        }));

        return rootView;
    }


}
