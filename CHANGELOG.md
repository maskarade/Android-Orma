The revision history of Orma.

The versioning follows [Semantic Versioning](http://semver.org/):

> Given a version number MAJOR.MINOR.PATCH, increment the:
>
> * MAJOR version when you make incompatible API changes,
> * MINOR version when you add functionality in a backwards-compatible manner, and
> * PATCH version when you make backwards-compatible bug fixes.

## NEXT

* Add `DatabaseHandle` interface which `OrmaDatabase` implements.

## v1.0.1 - 2016/01/16

* No code change from v1.0.0
* Bundles `proguard-rules.pro` in AAR, in order to work with ProGuard
  more easily. Use `-keepattributes Signature` if you set up proguard config
  manually.

## v1.0.0 - 2016/01/14

* The initial stable version
