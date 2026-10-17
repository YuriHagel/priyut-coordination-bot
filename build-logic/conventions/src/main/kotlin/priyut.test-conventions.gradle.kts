plugins {
    id("priyut.java-conventions")
    jacoco
}

tasks.withType<Test>().configureEach {
    systemProperty("file.encoding", "UTF-8")
    // Фиксируем UTC, чтобы тесты с датами (напоминания за 24/2 часа) не падали из-за часовых поясов
    systemProperty("user.timezone", "UTC")

    testLogging {
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
        )
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<JacocoReport>().configureEach {
    reports {
        xml.required.set(true) // Нужно для SonarQube/CI
        html.required.set(true)
    }
}

tasks.named("test") {
    finalizedBy("jacocoTestReport")
}

tasks.named("check") {
    dependsOn("jacocoTestReport")
}