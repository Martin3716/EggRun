package com.example.eggrun.classes;

import java.sql.Time;

public class RunSession {
    private final double mDistance;
    private Time mTime;

    public RunSession(double distance){
        mDistance = distance;
    }

    public double getDistance(){
        return mDistance;
    }

    public Time getTime(){
        return mTime;
    }
}
