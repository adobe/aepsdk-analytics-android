unit-test:
		(./code/gradlew -p code/android-analytics-library testPhoneDebugUnitTest)
unit-test-coverage:
		(./code/gradlew -p code/android-analytics-library createPhoneDebugUnitTestCoverageReport)

publish-maven-local-jitpack:
		(./code/gradlew -p code/android-analytics-library assemblePhone)
		(./code/gradlew -p code/android-analytics-library publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

ci-publish-staging: clean build-release
	(./code/gradlew -p code/android-analytics-library publishReleasePublicationToSonatypeRepository --stacktrace)

ci-publish-main: clean build-release
	(./code/gradlew -p code/android-analytics-library publishReleasePublicationToSonatypeRepository -Prelease)