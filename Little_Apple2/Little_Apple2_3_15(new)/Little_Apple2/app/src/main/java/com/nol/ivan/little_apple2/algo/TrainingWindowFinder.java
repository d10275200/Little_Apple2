package com.nol.ivan.little_apple2.algo;

/**
 * Created by Ivan on 2017/2/14.
 */


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/8/6.
 */
public class TrainingWindowFinder {
    private static final String TAG = "TrainingWindowFinder";
    public static final double Threshold = 0.45;
    private static int WindowSize;
    private static final int JumpWindow = 8;


    //Constructor
    public TrainingWindowFinder(int points){
        this.WindowSize = points;
    }

    //Check the rule
    private boolean CheckThreshold(final double[] vals){
        for(int i = 0; i< vals.length; i++){
            if(Math.abs(vals[i]) > Threshold)
                return true;
        }
        return false;
    }


    public final List<Integer> findWindowIndex(final float[] attrs, final float[] vals){
        if(attrs.length != vals.length){
            Log.e(TAG, "Time array length is not equal to vals array.");
            return null;
        }

        int curPos = 0;
        List<Integer> wIndex = new ArrayList<Integer>();
        while(curPos + WindowSize < vals.length ){
            double[] wData = new double[WindowSize];
            for(int i = curPos; i < curPos + WindowSize; i++)
                wData[i-curPos] = vals[i];

            if( CheckThreshold(wData) ){
                wIndex.add(curPos);
                curPos += JumpWindow*WindowSize;
            }else
                curPos += WindowSize;

        }
        return wIndex;
    }
}

