# Orma Migration Module

`orma-migration` is a library which provides migration for `SQLiteDatabase`.

## MigrationEngine

`MigrationEngine` is an interface to provide migration.

## SchemaDiffMigration

TBD

## ManualStepMigration

TBD

## OrmaMigration

This is a composite class with `ManualStepMigration` and `SchemaDiffMigration`.

## How To Define Migration Steps

[ManualStepMigrationTest.java](src/test/java/com/github/gfx/android/orma/migration/test/ManualStepMigrationTest.java)
is an example.

## See Also

* `SQLite.g4``, is originated from [bkiers/sqlite-parser](https://github.com/bkiers/sqlite-parser)
* [CREATE TABLE - SQLite](https://www.sqlite.org/lang_createtable.html)
