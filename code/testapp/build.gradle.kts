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
import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.application")
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        toggleOffOn("format:off", "format:on")
        target("src/*/java/**/*.java")
        importOrder()
        removeUnusedImports()
        googleJavaFormat(BuildConstants.Versions.GOOGLE_JAVA_FORMAT).aosp().reflowLongStrings()
        endWithNewline()
        formatAnnotations()
        licenseHeader(BuildConstants.ADOBE_LICENSE_HEADER)
    }
}

val mavenCoreVersion: String by project

android {
      namespace = "com.adobe.marketing.mobile.analytics.testapp"

    defaultConfig {
        applicationId = "com.adobe.marketing.mobile.analytics.testapp"
        compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
        targetSdk = BuildConstants.Versions.TARGET_SDK_VERSION
        versionCode = BuildConstants.Versions.VERSION_CODE
        versionName = BuildConstants.Versions.VERSION_NAME
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.RELEASE)  {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.0") 
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")

    implementation(project(":analytics"))

    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
    implementation("com.adobe.marketing.mobile:lifecycle:3.0.0-SNAPSHOT")
    {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
    implementation("com.adobe.marketing.mobile:identity:3.0.0-SNAPSHOT")
    {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
    implementation("com.adobe.marketing.mobile:assurance:2.0.0")
}