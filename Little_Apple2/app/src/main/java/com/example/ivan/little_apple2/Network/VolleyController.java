package com.example.ivan.little_apple2.Network;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.auth0.android.result.Credentials;

/**
 * Created by Ivan on 2016/12/9.
 */

public class VolleyController extends Application {



    public static final String TAG = VolleyController.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static VolleyController mInstance;
    private Credentials mUserCredentials;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized VolleyController getInstance() {
        if(mInstance==null)
        {
            mInstance=new VolleyController();

        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        (getRequestQueue()).add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public Credentials getUserCredentials() {
        return mUserCredentials;
    }

    public void setUserCredentials(Credentials userCredentials) {
        this.mUserCredentials = userCredentials;
    }
    public void deleteUserCredentials(){
        this.mUserCredentials = null;
    }
}