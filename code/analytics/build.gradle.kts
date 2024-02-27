/*
 * Copyright 2024 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project
val mavenIdentityVersion: String by project

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
    androidTestImplementation("com.adobe.marketing.mobile:identity:$mavenIdentityVersion-SNAPSHOT")
    {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
}