plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(project(":infrastructure:persistence"))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(libs.spring.boot.starter.test)
}
