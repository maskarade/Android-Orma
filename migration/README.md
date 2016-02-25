# Orma Migration Module

`orma-migration` is a library which provides migration
for `SQLiteDatabase`.

This is independent from `orma` module and available for
any Android `SQLiteDatabase` tools.

## MigrationEngine

`MigrationEngine` is an interface to provide migration. There are three migration engines provided:

* `SchemaDiffMigration`
* `ManualStepMigration`
* `OrmaMigration`

The last one, `OrmaMigration` is just a composite of the other two.

Typically you set up them with `OrmaDatabase.Builder`.

## SchemaDiffMigration

`SchemaDiffMigration` can make SQL statements from two different schemas. In other words, you don't need to write the migration code.

For example, given there is a table:

```sql
CREATE TABLE Book (name TEXT NOT NULL, author TEXT NOT NULL)
```

and there is another table:

```sql
CREATE TABLE Book (name TEXT NOT NULL, author TEXT NOT NULL, published_date DATE)
```

Then, `SchemaDiffMigration` generates the following statements:

```sql
CREATE __temp_Book (name TEXT NOT NULL, author TEXT NOT NULL, published_date DATE);
INSERT INTO __temp_Book (name, author) SELECT name, author FROM Book;
DROP TABLE Book;
ALTER TABLE __temp_Book RENAME TO Book;
```

Because [SQLite's ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
is limited, `SchemaDiffMigration` always re-creates tables if two tables differs.

### Schema Version Control

`SchemaDiffMigration` uses a string key, or `schemaHash`, to invoke migrations.

The typical key is `OrmaDatabase.SCHEMA_HASH`.

Internally, `SchemaDiffMigration` saves all the migration steps to a table [orma_schema_diff_migration_steps](https://github.com/gfx/Android-Orma/blob/master/migration/src/main/java/com/github/gfx/android/orma/migration/SchemaDiffMigration.java#L49). The `schema_hash` of the latest row of the table is the schema hash for `SchemaDiffMigration`.

## ManualStepMigration

`SchemaDiffMigration` can't handle **renaming**. If you want rename tables
or columns, you have to define migration steps with `ManualStepMigration`.

`ManualStepMigration` provides a way to handle hand-written migration steps,
typically used via `OrmaMigration`.

[ManualStepMigrationTest.java](src/test/java/com/github/gfx/android/orma/migration/test/ManualStepMigrationTest.java)
is an example.

### Schema Version Control

`ManualStepMigration` uses host application's `VERSION_CODE` as schema versions.

This "schema version" likes [SQLiteOpenHelper's schema version](http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html#SQLiteOpenHelper(android.content.Context, java.lang.String, android.database.sqlite.SQLiteDatabase.CursorFactory, int)), although `ManualStepMigration` does not depend on `SQLiteOpenHelper`.

Internally, `ManualStepMigration` use a table to manage schema versions: [orma_migration_steps](https://github.com/gfx/Android-Orma/blob/master/migration/src/main/java/com/github/gfx/android/orma/migration/ManualStepMigration.java#L35). The `version` of the latest row of the table is the schema version for `ManualStepMigration`.

## OrmaMigration

This is a composite class with `ManualStepMigration` and `SchemaDiffMigration`.

It invokes `ManualStepMigration` at first, and then invokes `SchemaDiffMigration`.

### How To Define Migration Steps

* Use `OrmaMigration` which has both `ManualStepMigration` and `SchemaDiffMigration` functionalities.
* `ManualStepMigration` writes steps in `ManualStepMigration.MIGRATION_STEPS_TABLE`
* `SchemaDiffMigration` writes steps in `SchemaDiffMigration.MIGRATION_STEPS_TABLE`

Here is an example to use `OrmaMigration`:

```java
int VERSION_2;
int VERSION_3;

OrmaMigration migration = OrmaMigration.builder(context)
    .schemaHashForSchemaDiffMigration(OrmaDatabase.SCHEMA_HASH)
    // register up() / down() steps
    .step(VERSION_2, new ManualStepMigration.Step() {
        @Override
        public void up(@NonNull ManualStepMigration.Helper helper) {
            helper.execSQL("... upgrading ...");
        }

        @Override
        public void down(@NonNull ManualStepMigration.Helper helper) {
            helper.execSQL("... downgrading ...");
        }
    })
    // register change(), which is used both in upgrade and downgrade
    .step(VERSION_3, new ManualStepMigration.ChangeStep() {
        @Override
        public void change(@NonNull ManualStepMigration.Helper helper) {
            Log.(TAG, helper.upgrade ? "upgrade" : "downgrade");
            helper.execSQL("DROP TABLE foo");
            helper.execSQL("DROP TABLE bar");
        }
    })
    .build();

// pass migration to OrmaDatabase.Builder#migrationEngine()
```

You can see migration logs in debug build, which are disabled in release build.

You can also have a look at databases in devices with [Stetho](https://github.com/facebook/stetho), which is really useful for debugging.

## FAQ

### Does Orma migration engine use `SQLiteOpenHelper` migration mechanism?

No. As of Orma v2.1.0, `SQLiteOpenHelper` is no longer used, whereas old versions of Orma use `SQLiteOpenHelper` to trigger migrations.

In other words, Orma no longer use [PRAGMA user_version](https://www.sqlite.org/pragma.html#pragma_schema_version).

### Why does `SchemaDiffMigration` crash when I add a column?

If the column is not annotated by `@Nullable`, it is declared as `NOT NULL`.

You have to add `@Nullable` to it or set `DEFAULT` to it with `@Column(defaultExpr = "...")`.

### When should I set `OrmaDatabase.Builder#versionForManualStepMigration()`?

The option is provided to test `ManualStepMigration`.

The default value, application's `BuildConfig.VERSION_CODE` is good for almost all the cases.

## See Also

* `SQLite.g4` is originated from [bkiers/sqlite-parser](https://github.com/bkiers/sqlite-parser)
* [CREATE TABLE - SQLite](https://www.sqlite.org/lang_createtable.html)
* [SQL::Translator::Diff in Perl](https://metacpan.org/pod/SQL::Translator::Diff)
