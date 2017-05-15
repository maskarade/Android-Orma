
check:
	./gradlew clean check bintrayUpload

publish: check
	./gradlew releng
	./gradlew -PdryRun=false annotations:bintrayUpload
	./gradlew -PdryRun=false processor:bintrayUpload
	./gradlew -PdryRun=false core:bintrayUpload
	./gradlew -PdryRun=false migration:bintrayUpload
	./gradlew -PdryRun=false library:bintrayUpload
	./gradlew -PdryRun=false encryption:bintrayUpload
