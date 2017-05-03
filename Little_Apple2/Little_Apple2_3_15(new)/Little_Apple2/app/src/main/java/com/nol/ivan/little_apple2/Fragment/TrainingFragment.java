package com.nol.ivan.little_apple2.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nol.ivan.little_apple2.MainActivity;
import com.nol.ivan.little_apple2.R;
import com.nol.ivan.little_apple2.TypeFragment;

/**
 * Created by Ivan on 2016/12/25.
 */

public class TrainingFragment extends Fragment{

    private TypeFragment curFragment = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_training, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Fragment Training");



        return view;
    }

}
