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
package android.example.com.squawker.following;

import android.content.SharedPreferences;
import android.example.com.squawker.R;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Shows the list of instructors you can follow
 */
public class FollowingPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String LOG_TAG = FollowingPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Add visualizer preferences, defined in the XML file in res->xml->preferences_squawker
        addPreferencesFromResource(R.xml.following_squawker);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean isSubscribed = sharedPreferences.getBoolean(key, false);
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        if (isSubscribed) {
            firebaseMessaging.subscribeToTopic(key);
        } else {
            firebaseMessaging.unsubscribeFromTopic(key);
        }
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }
}
