package com.example.eggrun.classes;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.eggrun.R;
import com.example.eggrun.ui.RunSessionActivity;
import com.example.eggrun.ui.RunSessionFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.security.Provider;

public class BackgroundLocationService extends Service {
    private static final String TAG = "BackgroundService";

    //singleton
    private static BackgroundLocationService INSTANCE = null;

    private static final String EXTRA_STARTED_FROM_NOTIFICATIOM = "com.example.eggrun.classes" + ".started_from_notification";
    private RunSessionFragment runSessionFragment;

    private int eggPostion;
    //track time and distance in the service so that when the activities die they don't disapear too
    private Location previousLocation;
    private Location currentLocation;
    private float totalDistance;
    private float[] distance = new float[1];
    private int seconds;


    private static final String CHANNEL_ID = "my_channel";
    private final IBinder mBinder = new LocalBinder();
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;

    public BackgroundLocationService(){

    }

    @Override
    public void onCreate() {
        totalDistance = 0f;
        seconds = 0;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            //this where you get the results of the location requests, gets new location every 4 seconds
            @Override
            public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    onNewLocation(locationResult.getLastLocation());
                calculateDistance();
                //set distances and time for RunSession frag
                if(runSessionFragment != null){
                    runSessionFragment.setDistance(totalDistance/1609);
                }
            }
        };


        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private void runTimer()
    {
        // Creates a new Handler
        final Handler handler
                = new Handler();
        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run()
            {
                runSessionFragment.setSeconds(seconds);
                updateRunFragmentView();
                seconds++;
                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void updateRunFragmentView(){
        if(runSessionFragment != null){
            runSessionFragment.changeDistanceText();
            runSessionFragment.changeTimerText();
        }
    }

    private void onNewLocation(Location lastLocation) {
        previousLocation = currentLocation;
        currentLocation = lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(currentLocation));
        //Update notification content if running as a foreground service
//        if(serviceIsRunningInForeGround(getApplicationContext())){
//            mNotificationManager.notify(NOTI_ID, getNotification());
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATIOM,false);
        if(startedFromNotification){
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    public void removeLocationUpdates() {
        INSTANCE = null;
        try{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            LocationServiceUtil.setRequestingLocationUpdates(this,false);
            stopSelf();
        }catch (SecurityException e){
            LocationServiceUtil.setRequestingLocationUpdates(this,false);
            Log.d(TAG, "Lost location permission. Could not remove updates  " + e);
        }
    }

    private void getLastLocation() {
        try{
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() != null){
                                currentLocation = task.getResult();
                            }else{
                                Log.d(TAG, "Failed to get location");
                            }
                        }
                    });
        }catch (SecurityException e){
            Log.d(TAG, "Lost Location Permission " + e);
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        //request updates for the specified time, here updated every 2 seconds
        locationRequest.setInterval(2000);
        //the minimum allowed time your allowed to get updates, in other words you can't get get updates faster than 0.5 seconds here
        locationRequest.setFastestInterval(500);
        //might change to lower priority to save battery later
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private Notification getNotification() {
        Intent intent = new Intent(this,BackgroundLocationService.class);
        String text = LocationServiceUtil.getLocationText(currentLocation);
        
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATIOM, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent activityPendingIntent = PendingIntent.getActivity(this,0,new Intent(this, RunSessionActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //.addAction(R.drawable.legendary_egg_image, "Launch", activityPendingIntent)
                .addAction(R.drawable.common_egg_image, "Stop Run Session", servicePendingIntent)
                .setContentText("")//text inside the service
                .setContentTitle("Egg Run Session")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.egg_marker)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        //might not need
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private boolean serviceIsRunningInForeGround(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
                if(getClass().getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
        return false;
    }

    public void requestLocationUpdates() {
        LocationServiceUtil.setRequestingLocationUpdates(this,true);
        startService(new Intent(getApplicationContext(), BackgroundLocationService.class));
        try{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }catch (SecurityException e){
            Log.d(TAG, "Lost Location permission. Could not be requested " + e);
        }
        //start timer
        runTimer();
    }

    public void setRunSessionFragment(RunSessionFragment runSessionFragment) {
        this.runSessionFragment = runSessionFragment;
    }

    public void calculateDistance(){
        if(previousLocation != null){
            double startLat = previousLocation.getLatitude();
            double startLng = previousLocation.getLongitude();
            double endLat = currentLocation.getLatitude();
            double endLng = currentLocation.getLongitude();
            Location.distanceBetween(startLat,startLng,endLat,endLng,distance);
            totalDistance += distance[0];
        }
    }

    private BackgroundLocationService instance;

    public class LocalBinder extends Binder {
        public BackgroundLocationService getService(){
            INSTANCE = BackgroundLocationService.this;
            return BackgroundLocationService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(!mChangingConfiguration && LocationServiceUtil.requestingLocationUpdates(this)){
            startForeground(NOTI_ID, getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }

    public static BackgroundLocationService getInstance(){
        return INSTANCE;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getEggPostion() {
        return eggPostion;
    }

    public void setEggPostion(int eggPostion) {
        this.eggPostion = eggPostion;
    }
}
