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

package com.github.gfx.android.orma.processor.tool;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class SynchronizedFiler implements Filer {

    private final Filer parent;

    public SynchronizedFiler(Filer parent) {
        this.parent = parent;
    }

    @Override
    public JavaFileObject createSourceFile(CharSequence name,
            Element... originatingElements)
            throws IOException {
        synchronized (parent) {
            return parent.createSourceFile(name, originatingElements);
        }
    }

    @Override
    public JavaFileObject createClassFile(CharSequence name,
            Element... originatingElements)
            throws IOException {
        synchronized (parent) {
            return parent.createClassFile(name, originatingElements);
        }
    }

    @Override
    public FileObject createResource(JavaFileManager.Location location,
            CharSequence pkg,
            CharSequence relativeName,
            Element... originatingElements)
            throws IOException {
        synchronized (parent) {
            return parent.createResource(location, pkg, relativeName, originatingElements);
        }
    }

    @Override
    public FileObject getResource(JavaFileManager.Location location,
            CharSequence pkg,
            CharSequence relativeName) throws IOException {
        synchronized (parent) {
            return parent.getResource(location, pkg, relativeName);
        }
    }
}
