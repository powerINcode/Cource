package com.example.android.mygarden;

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

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PlantWateringService extends IntentService {

    public static final String ACTION_WATER_PLANTS = "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_WATER_PLANTS_UPDATE = "com.example.android.mygarden.action.ACTION_WATER_PLANTS_UPDATE";
    // TODO (3): Create a new action ACTION_UPDATE_PLANT_WIDGETS to handle updating widget UI and
    // implement handleActionUpdatePlantWidgets to query the plant closest to dying and call
    // updatePlantWidgets to refresh widgets


    public PlantWateringService() {
        super("PlantWateringService");
    }

    /**
     * Starts this service to perform WaterPlants action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    public static void startActionWaterPlantsUpdate(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS_UPDATE);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WATER_PLANTS.equals(action)) {
                handleActionWaterPlants(intent.getLongExtra(PlantContract.PlantEntry._ID, 0));
            } else if (ACTION_WATER_PLANTS_UPDATE.equals(action)) {
                handleActionWaterPlantsUpdate();
            }
        }
    }

    private void handleActionWaterPlantsUpdate() {
        Cursor cursor = getContentResolver().query(PlantContract.PlantEntry.CONTENT_URI, null, null, null, PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        if (cursor != null) {
            cursor.moveToNext();
            long now = System.currentTimeMillis();
            long plantId = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry._ID));
            long createdAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
            long wataredAt = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
            int type = cursor.getInt(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));

            int plantImgRes = PlantUtils.getPlantImageRes(this, now - createdAt, now - wataredAt, type);

            AppWidgetManager appWidget = AppWidgetManager.getInstance(this);
            int[] widgetIds = appWidget.getAppWidgetIds(new ComponentName(this, PlantWidgetProvider.class));

            PlantWidgetProvider.onPlantsUpdated(this, appWidget, widgetIds, plantId, (now - wataredAt) > PlantUtils.MIN_AGE_BETWEEN_WATER, plantImgRes);

            appWidget.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_grid_view);
            cursor.close();
        }


    }

    /**
     * Handle action WaterPlant in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlants(long id) {
        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).appendPath(String.valueOf(id)).build();
        ContentValues cv = new ContentValues();
        cv.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, System.currentTimeMillis());
        // Update only plants that are still alive
        getContentResolver().update(
                PLANTS_URI,
                cv,
                null,
                null);

        startActionWaterPlantsUpdate(this);
    }
}
