# Orma for Android [![Circle CI](https://circleci.com/gh/gfx/Android-Orma/tree/master.svg?style=svg)](https://circleci.com/gh/gfx/Android-Orma/tree/master)

This is an **alpha** software and the interface will change until released.

**DO NOT USE THIS LIBRARY IN PRODUCTION**.

# Install

```groovy
dependencies {
    apt 'com.github.android.orma:orma-processor:0.0.1'
    provided 'com.github.android.orma:orma-annotations:0.0.1'
    compile 'com.github.android.orma:orma:0.0.1'
}
```

# Release Engineering

```
./gradlew bumpMajor # or bumpMinor / bumpPatch
./gradlew check bintrayUpload -PdryRun=true
./gradlew annotations:bintrayUpload processor:bintrayUpload library:bintrayUpload
```


# Author

FUJI Goro (gfx).

# License

The MIT License.
