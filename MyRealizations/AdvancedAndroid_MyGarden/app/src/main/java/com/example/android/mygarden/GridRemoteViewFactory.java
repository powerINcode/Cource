package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.PlantDetailActivity;
import com.example.android.mygarden.utils.PlantUtils;

/**
 * Created by powerman23rus on 28.11.17.
 * Enjoy ;)
 */

class GridRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    Context mContext;
    Cursor mCursor;

    GridRemoteViewFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(PlantContract.PlantEntry.CONTENT_URI,
                null, null, null, PlantContract.PlantEntry.COLUMN_CREATION_TIME);

    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        mCursor.moveToPosition(position);

        long now = System.currentTimeMillis();
        long plantId = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry._ID));
        long createdAt = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
        long wataredAt = mCursor.getLong(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
        int type = mCursor.getInt(mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));
        int plantImgRes = PlantUtils.getPlantImageRes(mContext, now - createdAt, now - wataredAt, type);

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.plant_widget);
        rv.setImageViewResource(R.id.widget_plant_image, plantImgRes);
        rv.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));
        rv.setViewVisibility(R.id.widget_water_button, View.GONE);

        Bundle extras = new Bundle();
        extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        Intent fillingIntent = new Intent();
        fillingIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_plant_image, fillingIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
