
check:
	./gradlew clean check bintrayUpload

publish:
	./gradlew -PdryRun=false bintrayUpload
