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

package com.github.gfx.android.orma.test.database_package_test;

import com.github.gfx.android.orma.annotation.Database;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.Publisher;

/**
 * @see OrmaDatabaseInAnotherPackage
 */
@Database(
    databaseClassName = "OrmaDatabaseInAnotherPackage",
    includes = {Book.class, Publisher.class}
)
public class TestDatabasePackage {

}
