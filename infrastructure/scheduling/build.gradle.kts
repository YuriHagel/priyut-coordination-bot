plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    // Spring Context для @Scheduled и @EnableScheduling
//    implementation(libs.spring.context)
//    implementation(libs.spring.boot.autoconfigure)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":tests:test-fixtures"))
}