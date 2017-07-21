package com.example.manas_world.locationsilence;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.security.PrivilegedAction;

/**
 * Created by Manas_world on 28-06-2017.
 */

public class GeofenceService extends IntentService {

    public static final String TAG = "GeofenceService";

    private static final int mId = 1;

    private AudioManager mAudioManager;

    public GeofenceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if(event.hasError()){

        }
        else{
            int transition = event.getGeofenceTransition();
            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){

                Log.d(TAG, "GEOFENCE ENTERRED");

                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.setRingerMode(0);
                NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setContentText("Your Phone is put on Silent")
                        .setContentTitle("Location Silencer");
                Intent notification = new Intent(this, Locations_SelectedActivity.class);
                /**TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(LoginActivity.class);
                stackBuilder.addNextIntent(notification);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(mId, mBuilder.build());**/

                PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notification, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mId, mBuilder.build());
            }
            else if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){

                Log.d(TAG, "GEOFENCE EXITED");
                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
    }
}
