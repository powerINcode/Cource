package com.example.android.shushme;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.shushme.provider.PlaceContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Constants
    public static final String TAG = MainActivity.class.getSimpleName();

    private final int PERMISSION_FINE_LOCATION_REQUEST_CODE = 1000;
    private final int PLACE_PICKER_REQUEST_CODE = 1001;
    private final String PREFERENCE_GEOFENCING_AVAILABLE = "PREFERENCE_GEOFENCING_AVAILABLE";

    // Member variables
    private CheckBox mPermissionCheckbox;
    private PlaceListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;
    private CheckBox mRingerPermissionCheckbox;

    @Override
    protected void onResume() {
        super.onResume();

        mPermissionCheckbox.setChecked(isLocationPermissionGranted());
        mRingerPermissionCheckbox.setChecked(isRingerPermissionGranted());

        if (isRingerPermissionGranted()) {
            mRingerPermissionCheckbox.setEnabled(false);
        }

    }

    /**
     * Called when the activity is starting
     *
     * @param savedInstanceState The Bundle that contains the data supplied in onSaveInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.places_list_recycler_view);
        mPermissionCheckbox = (CheckBox) findViewById(R.id.cb_permission);
        mRingerPermissionCheckbox = (CheckBox) findViewById(R.id.cb_permission_ringer);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PlaceListAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Switch switcher = (Switch) findViewById(R.id.enable_switch);
        switcher.setChecked(pref.getBoolean(PREFERENCE_GEOFENCING_AVAILABLE, false));
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(PREFERENCE_GEOFENCING_AVAILABLE, isChecked);
                editor.apply();

                if (isChecked) {
                    mGeofencing.registerAllGeofencings();
                } else {
                    mGeofencing.unregisterAllGeofences();
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google API Location Service is connected successfully");
        mGeofencing = new Geofencing(MainActivity.this, null, mClient);
        refreshPlacesData();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspend");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.getErrorMessage());

    }

    public void onChangePermissionClick(View view) {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_FINE_LOCATION_REQUEST_CODE);
        }else {
            mPermissionCheckbox.setChecked(true);
        }
    }

    public void onAddNewLocationClick(View view) {
        if (isLocationPermissionGranted()) {
            try {
                Intent placePickerIntent = new PlacePicker.IntentBuilder()
                        .build(this);

                startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_FINE_LOCATION_REQUEST_CODE) {
            mPermissionCheckbox.setChecked(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Place place = PlacePicker.getPlace(this, data);
                Toast.makeText(this, place.getAddress(), Toast.LENGTH_LONG).show();

                ContentValues cv = new ContentValues();
                cv.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, place.getId());
                getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, cv);

                refreshPlacesData();
            }
        }
    }


    private void refreshPlacesData() {
        Cursor placesCursor = getContentResolver().query(PlaceContract.PlaceEntry.CONTENT_URI, null, null, null, null);

        if (placesCursor == null || placesCursor.getCount() == 0) return;

        ArrayList<String> placeIds = new ArrayList<>();
        while (placesCursor.moveToNext()) {
            placeIds.add(placesCursor.getString(placesCursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));
        }

        Places.GeoDataApi.getPlaceById(mClient, placeIds.toArray(new String[placeIds.size()]))
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        mAdapter.swapPlaces(places);
                        mGeofencing.updateGeofancing(places);
                    }
                });

    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isRingerPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        } else {
            return true;
        }
    }


    public void onRingerChangePermissionClick(View view) {
        if (!isRingerPermissionGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }
}
