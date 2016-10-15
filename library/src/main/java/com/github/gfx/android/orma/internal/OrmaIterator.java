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

package com.github.gfx.android.orma.internal;

import com.github.gfx.android.orma.Selector;

import android.database.Cursor;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OrmaIterator<Model> implements Iterator<Model> {

    static final int batchSize = 1000;

    final Selector<Model, ?> selector;

    final int totalCount;

    int totalPos = 0;

    int offset = 0;

    Cursor cursor;

    int cursorPos = 0;

    public OrmaIterator(Selector<Model, ?> selector) {
        this.selector = selector;
        this.totalCount = selector.count();
        fill();
    }

    void finish() {
        cursor.close();
    }

    void fill() {
        if (cursor != null) {
            cursor.close();
        }
        cursor = selector
                .clone()
                .limit(batchSize)
                .offset(offset)
                .execute();

        offset += batchSize;
        cursorPos = 0;
    }

    @Override
    public boolean hasNext() {
        return totalPos < totalCount;
    }

    @Override
    public Model next() {
        if (totalPos >= totalCount) {
            throw new NoSuchElementException("OrmaIterator#next()");
        }

        if (cursor.isLast()) {
            fill();
        }

        cursor.moveToPosition(cursorPos);
        Model model = selector.newModelFromCursor(cursor);

        totalPos++;
        cursorPos++;

        if (!hasNext()) {
            finish();
        }

        return model;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator#remove()");
    }

}
