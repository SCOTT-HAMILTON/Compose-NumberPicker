plugins {
    id("com.android.library")
    id("common-config")
}

android {
    defaultConfig {
    }
}
dependencies {
    implementations(Libs.AndroidX.main)
    implementations(Libs.Compose.main)
    implementation(Libs.material)
}
