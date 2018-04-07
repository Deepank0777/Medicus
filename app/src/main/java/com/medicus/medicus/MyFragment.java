package com.medicus.medicus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kushbhardwaj on 14/02/18.
 */

public class MyFragment extends Fragment {

    View view;
    ImageView iv_image;

    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    public static final String ADD_MESSAGE = "ADD_MESSAGE";
    public static final String IMAGE = "EXTRA_IMAGE";

    public static final MyFragment newInstance(String message, String addition, int int_image)

    {

        MyFragment f = new MyFragment();

        Bundle bdl = new Bundle(2);
        bdl.putString(EXTRA_MESSAGE, message);
        bdl.putString(ADD_MESSAGE, addition);
        bdl.putInt(IMAGE, int_image);

        f.setArguments(bdl);

        return f;

    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String message = getArguments().getString(EXTRA_MESSAGE);
        String addition = getArguments().getString(ADD_MESSAGE);
        int int_image = getArguments().getInt(IMAGE);

        view = inflater.inflate(R.layout.fragment_intro, container, false);
        view.setMinimumWidth(1000);

        TextView messageTextView = (TextView) view.findViewById(R.id.textView);
        TextView messageTextView1 = (TextView) view.findViewById(R.id.textView2);

        ImageView iv_image = (ImageView) view.findViewById(R.id.imageView4);

        iv_image.setImageResource(int_image);
        messageTextView.setText(message);
        messageTextView1.setText(addition);

        return view;

    }

}