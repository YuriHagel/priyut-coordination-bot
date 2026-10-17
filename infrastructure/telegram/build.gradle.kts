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

    // Spring Context для DI (@Configuration, @Bean)
//    implementation(libs.spring.context)
//    implementation(libs.spring.boot.autoconfigure)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":tests:test-fixtures"))
}