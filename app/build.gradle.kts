plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    // Модули проекта
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure:persistence"))
    implementation(project(":infrastructure:telegram"))
    implementation(project(":infrastructure:scheduling"))

    // Web & Metrics
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.spring.boot.starter.aop)

    // Database (JDBC)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.validation)

    // Runtime
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)

    // Tests
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
}

// ПЕРЕОПРЕДЕЛЕНИЕ ДЛЯ ТОЧКИ ВХОДА (APP)
// Отключаем обычный jar
tasks.named<Jar>("jar") {
    enabled = false
}

// Включаем исполняемый fat-jar (используем AbstractArchiveTask, чтобы избежать Unresolved reference)
tasks.named<AbstractArchiveTask>("bootJar") {
    enabled = true
    archiveClassifier.set("") // Убираем суффикс, чтобы файл назывался просто app.jar
}