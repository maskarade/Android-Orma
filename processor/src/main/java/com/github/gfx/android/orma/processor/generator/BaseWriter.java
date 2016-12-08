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
package com.github.gfx.android.orma.processor.generator;

import com.github.gfx.android.orma.processor.OrmaProcessor;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public abstract class BaseWriter {

    protected final ProcessingContext context;

    public BaseWriter(ProcessingContext context) {
        this.context = context;
    }

    public abstract TypeSpec buildTypeSpec();

    public abstract String getPackageName();

    public JavaFile buildJavaFile() {
        return JavaFile.builder(getPackageName(), buildTypeSpec())
                .skipJavaLangImports(true)
                .build();
    }

    /**
     * <p<Mark the class as generated in terms of GitHub Linquist./p>
     * <p>"Code generated by" tells to GitHub that this is generated code.</p>
     * @see <a href="https://github.com/github/linguist/blob/master/lib/linguist/generated.rb">GitHub Linquist</a>
     * @param classBuilder
     */
    public void markAsGenerated(TypeSpec.Builder classBuilder) {
        classBuilder.addJavadoc("<p>Code generated by {@code $L}.</p>\n", OrmaProcessor.class.getCanonicalName());

    }
}
