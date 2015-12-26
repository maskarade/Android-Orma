# Orma Migration Module

`orma-migration` is a library which provides migration for `SQLiteDatabase`.

This is independent on `orma` module and available for
any Android `SQLiteDatabase` tools.

## MigrationEngine

`MigrationEngine` is an interface to provide migration.

## SchemaDiffMigration

`SchemaDiffMigration` can make SQL statements from two different schemas.

TBD

## ManualStepMigration

``ManualStepMigration`` provides a way to handle hand-written migration steps.

TBD

## OrmaMigration

This is a composite class with `ManualStepMigration` and `SchemaDiffMigration`.

## How To Define Migration Steps

[ManualStepMigrationTest.java](src/test/java/com/github/gfx/android/orma/migration/test/ManualStepMigrationTest.java)
is an example.

## See Also

* `SQLite.g4``, is originated from [bkiers/sqlite-parser](https://github.com/bkiers/sqlite-parser)
* [CREATE TABLE - SQLite](https://www.sqlite.org/lang_createtable.html)
