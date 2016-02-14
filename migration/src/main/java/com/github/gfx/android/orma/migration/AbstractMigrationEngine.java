/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.gfx.android.orma.migration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public abstract class AbstractMigrationEngine implements MigrationEngine {

    protected static boolean extractDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)
                == ApplicationInfo.FLAG_DEBUGGABLE;
    }

    @NonNull
    protected static PackageInfo getPackageInfo(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    protected static int extractLastUpdateTime(@NonNull Context context) {
        long milliseconds = getPackageInfo(context).lastUpdateTime;
        if (milliseconds != 0) {
            return (int) TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        } else {
            return 1; // non-zero integer for robolectric
        }
    }

    @NonNull
    protected static String extractVersionName(@NonNull Context context) {
        String versionCode = getPackageInfo(context).versionName;
        if (versionCode != null) {
            return versionCode;
        } else {
            return "1"; // non-empty string for robolectric
        }
    }

    protected static int extractVersionCode(@NonNull Context context) {
        int versionCode = getPackageInfo(context).versionCode;
        if (versionCode != 0) {
            return versionCode;
        } else {
            return 1; // non-zero integer for robolectric
        }
    }
}
