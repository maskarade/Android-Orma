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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    // collections

    @NonNull
    public static <C extends Collection<String>> String serializeStringCollection(@NonNull C collection) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        try {
            jsonWriter.beginArray();
            for (String s : collection) {
                jsonWriter.value(s);
            }
            jsonWriter.endArray();
            jsonWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    @NonNull
    public static <C extends Collection<String>> C deserializeStringCollection(@NonNull String serialized, C collection) {
        StringReader reader = new StringReader(serialized);
        JsonReader jsonReader = new JsonReader(reader);

        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    collection.add(null);
                } else {
                    collection.add(jsonReader.nextString());
                }
            }
            jsonReader.endArray();
            return collection;
        } catch (IOException e) {
            return collection;
        }
    }

    @NonNull
    public static String serializeStringList(@NonNull List<String> source) {
        return serializeStringCollection(source);
    }

    @NonNull
    public static List<String> deserializeStringList(@NonNull String serialized) {
        return deserializeStringCollection(serialized, new ArrayList<String>());
    }

    @NonNull
    public static String serializeStringArrayList(@NonNull ArrayList<String> source) {
        return serializeStringCollection(source);
    }

    @NonNull
    public static ArrayList<String> deserializeStringArrayList(@NonNull String serialized) {
        return deserializeStringCollection(serialized, new ArrayList<String>());
    }

    @NonNull
    public static String serializeStringSet(@NonNull Set<String> source) {
        return serializeStringCollection(source);
    }

    @NonNull
    public static Set<String> deserializeStringSet(@NonNull String serialized) {
        return deserializeStringCollection(serialized, new LinkedHashSet<String>());
    }

    @NonNull
    public static String serializeStringHashSet(@NonNull HashSet<String> source) {
        return serializeStringCollection(source);
    }

    @NonNull
    public static HashSet<String> deserializeStringHashSet(@NonNull String serialized) {
        return deserializeStringCollection(serialized, new HashSet<String>());
    }
}
