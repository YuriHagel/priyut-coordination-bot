plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    // Telegram Bot API (Long Polling)
    implementation(libs.telegram.bots.longpolling)
    implementation(libs.telegram.bots.client)

    implementation(libs.resilience4j.spring.boot4)
    implementation(libs.resilience4j.micrometer)

    implementation(libs.spring.context)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":tests:test-fixtures"))
}