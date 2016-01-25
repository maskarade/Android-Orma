The revision history of [Android-Orma](https://github.com/gfx/Android-Orma).

The versioning follows [Semantic Versioning](http://semver.org/):

> Given a version number MAJOR.MINOR.PATCH, increment the:
>
> * MAJOR version when you make incompatible API changes,
> * MINOR version when you add functionality in a backwards-compatible manner, and
> * PATCH version when you make backwards-compatible bug fixes.

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
