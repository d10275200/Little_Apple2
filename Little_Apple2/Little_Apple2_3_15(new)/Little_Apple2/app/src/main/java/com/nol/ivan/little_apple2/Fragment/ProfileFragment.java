package com.nol.ivan.little_apple2.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nol.ivan.little_apple2.MainActivity;
import com.nol.ivan.little_apple2.R;
import com.nol.ivan.little_apple2.file.SystemParameters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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

    RadioGroup radioGroup;
    RadioButton man;
    RadioButton woman;

    Button SaveProfile;

    Spinner checkmodel;

    String Edit_sex = "";
    int position = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        radioGroup = (RadioGroup) view.findViewById(R.id.radiogroup);
        man = (RadioButton) view.findViewById(R.id.man_radio);
        woman = (RadioButton) view.findViewById(R.id.woman_radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               if(checkedId == man.getId()){
                   Edit_sex = "Man";
                   Log.e("TAG", "I am Man");
               } else if (checkedId == woman.getId()){
                   Log.e("TAG", "I am Woman");
                   Edit_sex = "Woman";
               }
            }
        });

        position = sharedPreferences.getInt("ModelPosition", 1);
        final String[] models = CheckModelInRaw();
        checkmodel = (Spinner) view.findViewById(R.id.sp_model_select);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, models);
        checkmodel.setAdapter(adapter);
        checkmodel.setSelection(position,true);
        checkmodel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String items = checkmodel.getSelectedItem().toString();
                Log.i("Selected item : ", items);
                position = checkmodel.getSelectedItemPosition();
                Log.i("Selected position : ", String.valueOf(position));
                SystemParameters.ModelName = checkmodel.getSelectedItem().toString();
//                checkmodel.setSelection(position,true);
                editor.putInt("ModelPosition", position);
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });


        SaveProfile = (Button) view.findViewById(R.id.Save_Profile);
        SaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Edit_name = String.valueOf(nameEditText.getText());
                String Edit_height = String.valueOf(heightEditText.getText());
                String Edit_weight = String.valueOf(weightEditText.getText());
                String Edit_age = String.valueOf(ageEditText.getText());
                editor.putString("Name", Edit_name);
                editor.putString("Height", Edit_height);
                editor.putString("Weight", Edit_weight);
                editor.putString("Age", Edit_age);
                editor.putString("Sex", Edit_sex);
                editor.commit();

                String ID = sharedPreferences.getString("ID", null);
                Update_User(ID, Edit_name, Edit_height, Edit_weight, Edit_age, Edit_sex);
            }
        });



        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        String Name = sharedPreferences.getString("Name", null);
        String Height = sharedPreferences.getString("Height", null);
        String Weight = sharedPreferences.getString("Weight", null);
        String Age = sharedPreferences.getString("Age", null);
        String Sex = sharedPreferences.getString("Sex", null);

        if("Man".equals(Sex)){
            man.setChecked(true);
        }else if("Woman".equals(Sex)){
            woman.setChecked(true);
        }

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

    private String[] CheckModelInRaw() {
        Field[] fields = R.raw.class.getFields();
        String[] result = new String[fields.length];

        for (int i = 0; i < fields.length; i++)
            result[i] = fields[i].getName();

        return result;
    }

    private String Update_User(final String User_id, final String Name, final String Height, final String Weight, final String Age, final String Sex){
        RequestQueue mQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~Ivan/UpdateLittleAppleUser.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getActivity(), String.valueOf(response), Toast.LENGTH_SHORT).show();
                if(response.equals("success")){
                    Toast.makeText(getActivity(), "Update Success!", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getActivity(), "Update Fail!", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("ID", User_id);
                map.put("NAME", Name);
                map.put("WEIGHT", Weight);
                map.put("HEIGHT", Height);
                map.put("AGE", Age);
                map.put("SEX", Sex);
                return map;
            }
        };
        mQueue.add(stringRequest);

        return "";

    }
}
