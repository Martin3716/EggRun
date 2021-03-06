package com.example.eggrun.classes.egg;

import androidx.annotation.NonNull;

import com.example.eggrun.R;
import com.example.eggrun.classes.RunSession;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class CommonEgg implements Egg, Serializable {
    private List<RunSession> mRunSessionList;
    private double mDistanceToHatch = 1.0;
    private int mImage = R.drawable.common_egg_image;

    public CommonEgg(){
        mRunSessionList = new ArrayList<>();
    }

    @Override
    public void addRunSession(RunSession runSession){
        mRunSessionList.add(runSession);
        mDistanceToHatch -= runSession.getDistance();
        if (mDistanceToHatch < 0){
            mDistanceToHatch = 0;
        }
    }

    @Override
    public List<RunSession> getRunSessionList(){
        return mRunSessionList;
    }

    @Override
    public double DistanceToHatch(){
        return mDistanceToHatch;
    }

    @Override
    public boolean canHatch(){
        return mDistanceToHatch == 0;
    }

    @Override
    public int getImageId(){
        return mImage;
    }

    @Override
    @NonNull
    public String toString(){
        return "COMMON";
    }
}

