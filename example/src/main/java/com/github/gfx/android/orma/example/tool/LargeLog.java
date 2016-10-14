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

package com.github.gfx.android.orma.example.tool;

import android.util.Log;

import static android.content.ContentValues.TAG;

public class LargeLog {

    // output all the large logs
    public static void e(String tag, String content) {
        if (content.length() > 2000) {
            Log.e(tag, content.substring(0, 2000));
            e(tag, content.substring(2000));
        } else {
            Log.e(tag, content);
        }
    }

    public static void e(String tag, Throwable throwable) {
        e(TAG, Log.getStackTraceString(throwable));
    }
}
