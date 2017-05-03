package com.nol.ivan.little_apple2.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nol.ivan.little_apple2.MainActivity;
import com.nol.ivan.little_apple2.R;


public class TaskFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_task, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Task");



        return view;
    }

}