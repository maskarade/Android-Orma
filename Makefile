
dry-run:
	./gradlew check bintrayUpload -DdryRun=true


publish:
	./gradlew check bintrayUpload -DdryRun=false
