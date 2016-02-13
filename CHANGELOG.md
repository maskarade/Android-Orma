The revision history of [Android-Orma](https://github.com/gfx/Android-Orma).

The versioning follows [Semantic Versioning](http://semver.org/):

> Given a version number MAJOR.MINOR.PATCH, increment the:
>
> * MAJOR version when you make incompatible API changes,
> * MINOR version when you add functionality in a backwards-compatible manner, and
> * PATCH version when you make backwards-compatible bug fixes.

## v1.3.0 - 2016/02/13

There are lots of changes in this version:

* https://github.com/gfx/Android-Orma/compare/v1.2.2...v1.3.0

### Bug Fixes

* Removed useless transactions, which might cause dead locks (#150)
* `OrmaConnection#createModel()` did not work for models with non-auto
  primary keys (#159)

### New Features

* Built-in type adapters: `ArrayList<String>` and `HashSet<String>` (#151)
* Added `Selector#isEmpty()` and deprecated `Selector#empty()` (#152)
* `SingleAssociation<T>` is now Gson-serializable (#155, #156)
* `SingleAssociation<T>` is now Parcelable (#160)
* `UPSERT` support, including `Relation#upserter()` (#158)

## v1.2.2 - 2016/02/09

### Bug Fixes

* Fix an NPE when a `@Nullable Boolean field` is `null` (#148)

## v1.2.1 - 2016/02/08

### Bug Fixes

* `@Setter` constructors with `@Nullable` fields did not work (#146, #147)
* Empty `@Setter` constructors should have made compile errors (#145)

## v1.2.0 - 2016/02/03

### Bug Fixes

* Make `@Setter(name)` for constructors work (#139, #140)
* Workaround for Kotlin apt (kapt) issues (#138)

### New Features

* Introduce Static type adapters by `@StaticTypeAdapter` (#131)
  * The best-matched SQLite storage type is adapted
    * e.g. `INTEGER` for `java.util.Date`, `BLOB` for `java.nio.ByteBuffer`
  * Custom binary classes (e.g. `ByteBuffer`, `Bitmap`) are handled correctly
    * v1.0.0 can't use binary objects except for `byte[]`
  * See `README.md` for details

## v1.1.3 - 2016/01/31

### Bug Fixes

- Use `com.tunnelvisionlabs:antlr4:4.5` instead of `org.antlr:antlr4:4.5.1`
  because Android DataBinding 1.1 depends on com.tunnelvisionlabs's runtime which conflicts on org.antlr's runtime (#133)

## v1.1.2 - 2016/01/27

### Bug Fixes

* Follow the `trace` flag in all the logs (#130)
* Use `info` log level for all the migration logs (#130)

## v1.1.1 - 2016/01/25

### Bug Fixes

* For SchemaDiffMigration to use the same versioning logic as OrmaMigration (#129)
* Set application's VERSION_CODE to `OrmaMigration.Bulder#manualStepMigrationVersion()` by default

## v1.1.0 - 2016/01/25

This release includes new features and bug fixes.

### Features

* `OrmaMigration.Builder` (#128)
* `Relation#getOrCreate(int, ModelFactory<T>)` (#127)
* `Selector#empty()` to check a relation is empty or not (#120)
* `DatabaseHandle` interface which `OrmaDatabase` implements (#118)

### Bug Fixes

*  Queries for`SingleAssociation<T>` were broken (#125)
* `OrmaConnection#resetDatabase()` did not work (#123)

## v1.0.1 - 2016/01/16

No code change from v1.0.0.

### Bug Fixes

* Bundles `proguard-rules.pro` in AAR, in order to work with ProGuard
  more easily. Use `-keepattributes Signature` if you set up proguard config
  manually. (#117)

## v1.0.0 - 2016/01/14

* The initial stable version
