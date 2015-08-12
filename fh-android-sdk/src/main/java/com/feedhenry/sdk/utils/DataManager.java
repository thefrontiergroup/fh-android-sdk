/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

/**
 * Internal class for managing data persistence.
 */
public class DataManager {
    private static final String LEGACY_PREF_KEY = "init";
    private static final String PREF_FILE_KEY = "fhsdkprivatedata";
    private static final String PREF_KEY_PREFIX = "fhsdk_";
    private static final String MIGRATED_KEY = "legacyMigrated";
    private static final String TRACKID_KEY = "trackId";
    private static final String LOG_TAG = "com.feedhenry.sdk.utils.DataManager";
    private static DataManager mInstance;
    private Context mContext;
    private SharedPreferences mPrefs;

    private DataManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPrefs = this.mContext.getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static synchronized DataManager init(Context context) {
        if (mInstance == null) {
            mInstance = new DataManager(context);
        }
        return mInstance;
    }

    public static synchronized DataManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("DataManager is not initialised");
        }
        return mInstance;
    }

    public void save(String key, String value) {
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.putString(getKey(key), value);
        editor.apply();
    }

    public String read(String key) {
        return this.mPrefs.getString(getKey(key), null);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.remove(getKey(key));
        editor.commit();
    }

    private static String getKey(String key) {
        return PREF_KEY_PREFIX + key;
    }

    /**
     * Move the "init" value from the old pref file to the new pref file.
     * Only do it once.
     */
    public void migrateLegacyData() {
        if (read(MIGRATED_KEY) == null) {
            SharedPreferences legacyPref = this.mContext.getSharedPreferences(LEGACY_PREF_KEY, Context.MODE_PRIVATE);
            if (legacyPref != null) {
                String initValue = legacyPref.getString(LEGACY_PREF_KEY, null);
                if (initValue != null) {
                    if (isFHInitValue(initValue)) {
                        save(LEGACY_PREF_KEY, initValue);
                        FHLog.d(LOG_TAG, "legacy init data has been migrated : " + initValue);
                        // remove old prefs
                        SharedPreferences.Editor editor = legacyPref.edit();
                        editor.remove(LEGACY_PREF_KEY);
                        editor.commit();
                    }
                }
            }
            // at this point, the legacy data has been migrated if it exists.
            // If it does not exist, no need to check again in the future anyway.
            save(MIGRATED_KEY, String.valueOf(true));
        }
    }

    private static boolean isFHInitValue(String value) {
        try {
            return new JSONObject(value).has(TRACKID_KEY);
        } catch (Exception e) {
            return false;
        }
    }
}
