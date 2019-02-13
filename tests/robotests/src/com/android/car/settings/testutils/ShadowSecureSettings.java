/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.settings.testutils;

import android.content.ContentResolver;
import android.provider.Settings;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import java.util.Map;
import java.util.WeakHashMap;

@Implements(Settings.Secure.class)
public class ShadowSecureSettings {

    private static final Map<ContentResolver, Table<Integer, String, Object>> sUserDataMap =
            new WeakHashMap<>();

    @Implementation
    protected static int getInt(ContentResolver cr, String name, int def) {
        return getIntForUser(cr, name, def, cr.getUserId());
    }

    @Implementation
    protected static int getIntForUser(ContentResolver resolver, String name, int def,
            int userHandle) {
        final Table<Integer, String, Object> userTable = getUserTable(resolver);
        synchronized (userTable) {
            final Object object = userTable.get(userHandle, name);
            return object instanceof Integer ? (Integer) object : def;
        }
    }

    @Implementation
    protected static boolean putIntForUser(ContentResolver resolver, String name, int value,
            int userHandle) {
        final Table<Integer, String, Object> userTable = getUserTable(resolver);
        synchronized (userTable) {
            userTable.put(userHandle, name, value);
            return true;
        }
    }

    @Implementation
    protected static String getString(ContentResolver resolver, String name) {
        return getStringForUser(resolver, name, resolver.getUserId());
    }

    @Implementation
    protected static String getStringForUser(ContentResolver resolver, String name,
            int userHandle) {
        final Table<Integer, String, Object> userTable = getUserTable(resolver);
        synchronized (userTable) {
            final Object object = userTable.get(userHandle, name);
            return object instanceof String ? (String) object : null;
        }
    }

    @Implementation
    protected static boolean putString(ContentResolver resolver, String name, String value) {
        return putStringForUser(resolver, name, value, resolver.getUserId());
    }

    @Implementation
    protected static boolean putStringForUser(ContentResolver resolver, String name, String value,
            int userHandle) {
        final Table<Integer, String, Object> userTable = getUserTable(resolver);
        synchronized (userTable) {
            userTable.put(userHandle, name, value);
            return true;
        }
    }

    /**
     * Clears the UserDataMap for Robotests.
     */
    @Resetter
    public static void reset() {
        synchronized (sUserDataMap) {
            sUserDataMap.clear();
        }
    }

    private static Table<Integer, String, Object> getUserTable(ContentResolver contentResolver) {
        synchronized (sUserDataMap) {
            Table<Integer, String, Object> table = sUserDataMap.get(contentResolver);
            if (table == null) {
                table = HashBasedTable.create();
                sUserDataMap.put(contentResolver, table);
            }
            return table;
        }
    }
}
