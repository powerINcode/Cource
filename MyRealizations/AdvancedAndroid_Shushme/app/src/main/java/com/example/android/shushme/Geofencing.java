package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by powerman23rus on 25.11.17.
 * Enjoy ;)
 */

public class Geofencing implements OnSuccessListener<Void>, OnFailureListener {
    private final long EXPIRATION_TIME = TimeUnit.HOURS.toMillis(24);
    private final long GEOFENCING_RADIUS = 50;

    private Context mContext;
    private PlaceBuffer mPlaces;
    private ArrayList<Geofence> mGeofences;
    private GoogleApiClient mClient;
    private PendingIntent mGeofencingPendingIntent;

    public Geofencing(Context context, PlaceBuffer places, GoogleApiClient client) {
        mContext = context;
        mPlaces = places;
        mClient = client;
    }

    public void updateGeofancing(PlaceBuffer places) {
        mPlaces = places;
        mGeofences = new ArrayList<>();

        if (mPlaces == null || mPlaces.getCount() == 0) {
            return;
        }

        for(Place place : mPlaces) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setExpirationDuration(EXPIRATION_TIME)
                    .setCircularRegion(place.getLatLng().latitude, place.getLatLng().longitude, GEOFENCING_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofences.add(geofence);
        }
    }

    public void registerAllGeofencings() {
        if (mClient == null || !mClient.isConnected() || mGeofences == null || mGeofences.size() == 0) {
            return;
        }

        try {
            LocationServices.getGeofencingClient(mContext)
                    .addGeofences(getGeofencingRequest(), getGeofencingPendingIntent())
                    .addOnSuccessListener(this)
                    .addOnFailureListener(this);
        } catch (SecurityException e) {
            Log.e("Geofencing", e.getLocalizedMessage());
        }
    }

    public void unregisterAllGeofences() {
        if (mClient != null && mClient.isConnected()) {
            LocationServices.getGeofencingClient(mContext)
                    .removeGeofences(getGeofencingPendingIntent())
                    .addOnSuccessListener(this);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        if (mGeofences == null) {
            mGeofences = new ArrayList<>();
        }

        return new GeofencingRequest.Builder()
                .addGeofences(mGeofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    private PendingIntent getGeofencingPendingIntent() {
        if (mGeofencingPendingIntent != null) {
            return mGeofencingPendingIntent;
        }

        Intent intent = new Intent(mContext, GeofencingBroadcastReciever.class);
        mGeofencingPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencingPendingIntent;
    }

    @Override
    public void onSuccess(Void aVoid) {
        Log.d("Geofencing", "Successfully add/remove geofences");
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.d("Geofencing", e.getLocalizedMessage());
    }
}
