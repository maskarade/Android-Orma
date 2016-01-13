# Orma Migration Module

`orma-migration` is a library which provides migration for `SQLiteDatabase`.

This is independent on `orma` module and available for
any Android `SQLiteDatabase` tools.

## MigrationEngine

`MigrationEngine` is an interface to provide migration.

## SchemaDiffMigration

`SchemaDiffMigration` can make SQL statements from two different schemas.

For example, if the following table is defined in a database:

```sql
CREATE TABLE Book (name TEXT NOT NULL, author TEXT NOT NULL)
```

and a new table defined in the code is like this:

```sql
CREATE TABLE Book (name TEXT NOT NULL, author TEXT NOT NULL, published_date DATE)
```

The SchemaDiffMigration generates the following statements:

```sql
CREATE __temp_Book (name TEXT NOT NULL, author TEXT NOT NULL, published_date DATE);
INSERT INTO __temp_Book (name, author) SELECT name, author FROM Book;
DROP TABLE Book;
ALTER TABLE __temp_Book RENAME TO Book;
```

Because [SQLite's ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
is limited, `SchemaDiffMigration` always re-create tables if two tables differs.

## ManualStepMigration

``ManualStepMigration`` provides a way to handle hand-written migration steps.

TBD

## OrmaMigration

This is a composite class with `ManualStepMigration` and `SchemaDiffMigration`.

It invokes `ManualStepMigration` at first, and then invokes `SchemaDiffMigration`.

## How To Define Migration Steps

[ManualStepMigrationTest.java](src/test/java/com/github/gfx/android/orma/migration/test/ManualStepMigrationTest.java)
is an example.

## See Also

* `SQLite.g4` is originated from [bkiers/sqlite-parser](https://github.com/bkiers/sqlite-parser)
* [CREATE TABLE - SQLite](https://www.sqlite.org/lang_createtable.html)
* [SQL::Translator::Diff in Perl](https://metacpan.org/pod/SQL::Translator::Diff)
