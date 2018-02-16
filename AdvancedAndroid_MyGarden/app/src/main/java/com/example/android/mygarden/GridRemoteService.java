package com.example.android.mygarden;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by powerman23rus on 28.11.17.
 * Enjoy ;)
 */

public class GridRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewFactory(getApplicationContext());
    }
}
