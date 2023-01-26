unit-test:
		(./code/gradlew -p code/analytics testPhoneDebugUnitTest)
unit-test-coverage:
		(./code/gradlew -p code/analytics createPhoneDebugUnitTestCoverageReport)

publish-maven-local-jitpack:
		(./code/gradlew -p code/analytics assemblePhone)
		(./code/gradlew -p code/analytics publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

build-release:
		(./code/gradlew -p code/analytics assemblePhoneRelease)

ci-publish-staging: build-release
	(./code/gradlew -p code/analytics publishReleasePublicationToSonatypeRepository --stacktrace)

ci-publish-main: build-release
	(./code/gradlew -p code/analytics publishReleasePublicationToSonatypeRepository -Prelease)

integration-test: 
		(./code/gradlew -p code/analytics connectedPhoneDebugAndroidTest)


assemble-phone:
		(./code/gradlew -p code/analytics assemblePhone)

assemble-app:
		(./code/gradlew -p code/testapp assemble)

java-doc:
		(./code/gradlew -p code/analytics javadocJar)

checkformat:
		(./code/gradlew -p code/analytics spotlessCheck)

format:
		(./code/gradlew -p code/analytics spotlessApply)
