plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    // Spring JDBC (не JPA!)
    implementation(libs.spring.boot.starter.jdbc)

    // Flyway для миграций
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // PostgreSQL драйвер (runtime)
    runtimeOnly(libs.postgresql)

    // Тестирование
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
}
