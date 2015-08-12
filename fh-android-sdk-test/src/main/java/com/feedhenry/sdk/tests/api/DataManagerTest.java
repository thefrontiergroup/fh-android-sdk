/**
 * Copyright (c) 2015 FeedHenry Ltd, All Rights Reserved.
 *
 * Please refer to your contract with FeedHenry for the software license agreement.
 * If you do not have a contract, you do not have a license to use this software.
 */
package com.feedhenry.sdk.tests.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import com.feedhenry.sdk.utils.DataManager;
import org.json.JSONException;
import org.json.JSONObject;

public class DataManagerTest extends AndroidTestCase {

    private static final String TRACKID = "faketrackid";

    private void removeAll() {
        SharedPreferences prefs = getContext().getSharedPreferences("fhsdkprivatedata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    public void setUp() {
        DataManager.init(getContext());
        removeAll();
    }

    public void tearDown() {
        removeAll();
    }

    public void testMigrateData() throws JSONException {
        DataManager dm = DataManager.getInstance();
        String initData = dm.read("init");
        assertNull(initData);
        createLegacyInitData();
        dm.migrateLegacyData();
        initData = dm.read("init");
        assertNotNull(initData);
        JSONObject initObj = new JSONObject(initData);
        assertTrue(initObj.has("trackId"));
        assertEquals(TRACKID, initObj.getString("trackId"));
    }

    public void testDataManager() {
        DataManager dm = DataManager.getInstance();
        String testKey = "testkey";
        String testValue = "testValue";
        String saved = dm.read(testKey);
        assertNull(saved);
        dm.save(testKey, testValue);
        saved = dm.read(testKey);
        assertEquals(saved, testValue);
        dm.remove(testKey);
        saved = dm.read(testKey);
        assertNull(saved);
    }

    private void createLegacyInitData() throws JSONException {
        SharedPreferences prefs = getContext().getSharedPreferences("init", Context.MODE_PRIVATE);
        JSONObject initObj = new JSONObject().put("trackId", TRACKID);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("init", initObj.toString());
        edit.commit();
    }
}
