package com.example.ivan.little_apple2.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ivan.little_apple2.MainActivity;
import com.example.ivan.little_apple2.R;

/**
 * Created by Ivan on 2016/12/25.
 */

public class ReadmeFragment extends Fragment{
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_readme, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Fragment Readme");

        return view;
    }
}
