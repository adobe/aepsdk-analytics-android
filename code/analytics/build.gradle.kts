plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.analytics"
    enableSpotless = true
    enableCheckStyle = true
    enableDokkaDoc = true

    publishing {
        gitRepoName = "aepsdk-analytics-android"
        addCoreDependency(mavenCoreVersion)
    }
}

dependencies {
    // Stop using SNAPSHOT after Core release.
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")

    // testImplementation dependencies provided by aep-library:
    // MOCKITO_CORE, MOCKITO_INLINE, MOCKITO_KOTLIN, JUNIT
    // testImplementation 'androidx.test:core-ktx:1.5.0'

    // androidTestImplementation dependencies provided by aep-library:
    // JUNIT
    androidTestImplementation("com.adobe.marketing.mobile:identity:3.0.0-SNAPSHOT")
    {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
    //androidTestUtil("androidx.test:$Versions.ANDROIDX_TEST_ORCHESTRATOR")
}