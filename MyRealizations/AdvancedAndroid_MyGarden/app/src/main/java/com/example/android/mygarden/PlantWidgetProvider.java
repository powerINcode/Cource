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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.transition.Visibility;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

public class PlantWidgetProvider extends AppWidgetProvider {

    // TODO (1): Modify updateAppWidget method to take an image recourse and call
    // setImageViewResource to update the widget’s image
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, long plantId, boolean isNeedToWatared, int imgResId) {

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        RemoteViews views;

        if (width < 300) {
            views = getSimpleWidgetView(context, appWidgetId, plantId, isNeedToWatared, imgResId);
        } else {
            views = getGridWidgetView(context);
        }



        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews getGridWidgetView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        Intent intent = new Intent(context, GridRemoteService.class);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        Intent plantDetailIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, plantDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, pendingIntent);


        views.setEmptyView(R.id.widget_grid_view, R.id.empty_view);



        return views;
    }

    private static RemoteViews getSimpleWidgetView(Context context, int appWidgetId, long plantId, boolean isNeedToWatared,
                                                   int imgResId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        // Construct the RemoteViews object

        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);
        views.setImageViewResource(R.id.widget_plant_image, imgResId);

        if (isNeedToWatared) {
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
            // Add the wateringservice click handler
            Intent wateringIntent = new Intent(context, PlantWateringService.class);
            wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANTS);
            wateringIntent.putExtra(PlantContract.PlantEntry._ID, plantId);
            PendingIntent wateringPendingIntent = PendingIntent.getService(context, 0, wateringIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
        } else {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
        }

        views.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        PlantWateringService.startActionWaterPlantsUpdate(context);
    }

    public static void onPlantsUpdated(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, long plantId, boolean isNeedToWatared, int imgResId) {
        // TODO (2): Move the updateAppWidget loop to a new method called updatePlantWidgets and pass through the image recourse
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, plantId, isNeedToWatared, imgResId);
        }
        // TODO (4): Call startActionUpdatePlantWidgets in onUpdate as well as in AddPlantActivity and PlantDetailActivity (add and delete plants)
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionWaterPlantsUpdate(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
