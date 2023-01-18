unit-test:
		(./code/gradlew -p code/android-analytics-library testPhoneDebugUnitTest)
unit-test-coverage:
		(./code/gradlew -p code/android-analytics-library createPhoneDebugUnitTestCoverageReport)

publish-maven-local-jitpack:
		(./code/gradlew -p code/android-analytics-library assemblePhone)
		(./code/gradlew -p code/android-analytics-library publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

build-release:
		(./code/gradlew -p code/android-analytics-library assemblePhoneRelease)

ci-publish-staging: build-release
	(./code/gradlew -p code/android-analytics-library publishReleasePublicationToSonatypeRepository --stacktrace)

ci-publish-main: build-release
	(./code/gradlew -p code/android-analytics-library publishReleasePublicationToSonatypeRepository -Prelease)

integration-test: 
		(./code/gradlew -p code/android-analytics-library connectedPhoneDebugAndroidTest)


assemble-phone:
		(./code/gradlew -p code/android-analytics-library assemblePhone)

java-doc:
		(./code/gradlew -p code/android-analytics-library javadocJar)
