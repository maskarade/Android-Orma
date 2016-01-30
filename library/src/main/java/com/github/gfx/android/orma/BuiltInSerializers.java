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

package com.github.gfx.android.orma;

import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Built-in serializes.
 */
public class BuiltInSerializers {

    @NonNull
    public static String serializeBigDecimal(@NonNull BigDecimal source) {
        return source.toString();
    }

    @NonNull
    public static BigDecimal deserializeBigDecimal(@NonNull String serialized) {
        return new BigDecimal(serialized);
    }

    @NonNull
    public static String serializeBigInteger(@NonNull BigInteger source) {
        return source.toString();
    }

    @NonNull
    public static BigInteger deserializeBigInteger(@NonNull String serialized) {
        return new BigInteger(serialized);
    }

    @NonNull
    public static byte[] serializeByteBuffer(@NonNull ByteBuffer source) {
        return source.array();
    }

    @NonNull
    public static ByteBuffer deserializeByteBuffer(@NonNull byte[] serialized) {
        return ByteBuffer.wrap(serialized);
    }

    @NonNull
    public static String serializeCurrency(@NonNull Currency source) {
        return source.getCurrencyCode();
    }

    @NonNull
    public static Currency deserializeCurrency(@NonNull String serialized) {
        return Currency.getInstance(serialized);
    }


    public static long serializeDate(@NonNull java.util.Date date) {
        return date.getTime();
    }

    @NonNull
    public static java.util.Date deserializeDate(long timeMillis) {
        return new java.util.Date(timeMillis);
    }

    @NonNull
    public static String serializeSqlDate(@NonNull java.sql.Date source) {
        return source.toString();
    }

    @NonNull
    public static java.sql.Date deserializeSqlDate(@NonNull String serialized) {
        return java.sql.Date.valueOf(serialized);
    }

    @NonNull
    public static String serializeSqlTime(@NonNull java.sql.Time source) {
        return source.toString();
    }

    @NonNull
    public static java.sql.Time deserializeSqlTime(@NonNull String serialized) {
        return java.sql.Time.valueOf(serialized);
    }

    @NonNull
    public static String serializeSqlTimestamp(@NonNull java.sql.Timestamp source) {
        return source.toString();
    }

    @NonNull
    public static java.sql.Timestamp deserializeSqlTimestamp(@NonNull String serialized) {
        return java.sql.Timestamp.valueOf(serialized);
    }

    @NonNull
    public static String serializeStringList(@NonNull List<String> source) {
        JSONArray array = new JSONArray();
        for (String s : source) {
            array.put(s);
        }
        return array.toString();
    }

    @NonNull
    public static List<String> deserializeStringList(@NonNull String serialized) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(serialized);
            List<String> list = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (JSONException e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public static String serializeStringSet(@NonNull Set<String> source) {
        JSONArray array = new JSONArray();
        for (String s : source) {
            array.put(s);
        }
        return array.toString();
    }

    @NonNull
    public static Set<String> deserializeStringSet(@NonNull String serialized) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(serialized);
            Set<String> set = new HashSet<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                set.add(jsonArray.getString(i));
            }
            return set;
        } catch (JSONException e) {
            return new HashSet<>();
        }
    }

    @NonNull
    public static String serializeUri(@NonNull Uri source) {
        return source.toString();
    }

    @NonNull
    public static Uri deserializeUri(@NonNull String serialized) {
        return Uri.parse(serialized);
    }

    @NonNull
    public static String serializeUUID(@NonNull UUID source) {
        return source.toString();
    }

    @NonNull
    public static UUID deserializeUUID(@NonNull String serialized) {
        return UUID.fromString(serialized);
    }

}
