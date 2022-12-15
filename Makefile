unit-test:
		(./code/gradlew -p code/android-analytics-library testPhoneDebugUnitTest)
unit-test-coverage:
		(./code/gradlew -p code/android-analytics-library createPhoneDebugUnitTestCoverageReport)
