plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("androidx.navigation.safeargs.kotlin")
    defaults.`android-module`
    defaults.`setup-jacoco`
}

dependencies {
    api(res("corext"))

    implementation(Deps.arrowCoreData)
    implementation(Deps.coroutinesCore)

    implementation(AndroidDeps.timber)

    AndroidKaptDeps.core.forEach(::kapt)
}