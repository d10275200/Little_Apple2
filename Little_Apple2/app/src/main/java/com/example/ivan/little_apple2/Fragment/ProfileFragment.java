package com.example.ivan.little_apple2.Fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.opengl.ETC1;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ivan.little_apple2.MainActivity;
import com.example.ivan.little_apple2.R;

/**
 * Created by Ivan on 2016/12/25.
 */


public class ProfileFragment extends Fragment {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText nameEditText;
    EditText heightEditText;
    EditText weightEditText;
    EditText ageEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Fragment Profile");

        String Name = sharedPreferences.getString("Name", null);
        String Height = sharedPreferences.getString("Height", null);
        String Weight = sharedPreferences.getString("Weight", null);
        String Age = sharedPreferences.getString("Age", null);

        nameEditText = (EditText) getView().findViewById(R.id.name_data);
        heightEditText = (EditText) getView().findViewById(R.id.height_data);
        weightEditText = (EditText) getView().findViewById(R.id.weight_data);
        ageEditText = (EditText) getView().findViewById(R.id.age_data);

        if (Name != null && Height != null && Weight != null && Age != null) {
            heightEditText.setText(Height);
            heightEditText.setTextSize(20);
            weightEditText.setText(Weight);
            weightEditText.setTextSize(20);
            nameEditText.setText(Name);
            nameEditText.setTextSize(20);
            ageEditText.setText(Age);
            ageEditText.setTextSize(20);
        } else {
            Log.v("Tag", "Something wrong in profile");
        }

    }
}
