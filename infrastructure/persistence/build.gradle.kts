plugins {
    `java-library`
}

dependencies {
    api(libs.spring.boot.starter.jdbc)
    api(libs.flyway.core)
    api(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
}
