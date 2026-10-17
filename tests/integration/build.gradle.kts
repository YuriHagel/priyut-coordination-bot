plugins {
    java
}

dependencies {
    testImplementation(project(":app"))
    testImplementation(project(":tests:test-fixtures"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.wiremock)
    testImplementation(libs.awaitility)
}

tasks.test {
    useJUnitPlatform()
}