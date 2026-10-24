plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    // Spring Context для @Scheduled и @EnableScheduling
    implementation(libs.spring.context)

    implementation(libs.slf4j.api)

    implementation("io.micrometer:micrometer-core")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":tests:test-fixtures")))
    testImplementation(libs.awaitility)
}