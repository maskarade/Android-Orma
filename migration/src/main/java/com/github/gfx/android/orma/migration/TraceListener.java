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

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.Locale;

public interface TraceListener {

    TraceListener EMPTY = new EmptyTraceListener();

    TraceListener LOGCAT = new LogcatTraceListener();

    void onTrace(@NonNull MigrationEngine engine, @NonNull String format, @NonNull Object[] args);

    class EmptyTraceListener implements TraceListener {

        @Override
        public void onTrace(@NonNull MigrationEngine engine, @NonNull String format, @NonNull Object[] args) {
            // nothing to do
        }
    }

    class LogcatTraceListener implements TraceListener {

        @Override
        public void onTrace(@NonNull MigrationEngine engine, @NonNull String format, @NonNull Object[] args) {
            Log.i(engine.getTag(), String.format(Locale.getDefault(), format, args));
        }
    }
}
