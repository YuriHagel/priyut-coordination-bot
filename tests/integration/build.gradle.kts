plugins {
    id("priyut.test-conventions")
}

dependencies {
    // 1. Подключаем модуль 'app', чтобы тестировать полностью собранный Spring-контекст
    // со всеми конфигурациями, миграциями Flyway и компонентами.
    testImplementation(project(":app"))

    // 2. Подключаем тестовые фикстуры (моки, билдеры данных, конфиги Testcontainers).
    // ВАЖНО: используем специальный синтаксис testFixtures(...), который мы настроили ранее.
    testImplementation(testFixtures(project(":tests:test-fixtures")))

    // 3. Базовые библиотеки для тестирования (JUnit 5, AssertJ, Mockito)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.wiremock.standalone)
    testImplementation(libs.awaitility)
}

// Дополнительная настройка задачи тестов специально для этого модуля
tasks.withType<Test>().configureEach {
    // Принудительно активируем тестовый профиль Spring для интеграционных тестов
    systemProperty("spring.profiles.active", "test")

    // Увеличиваем таймаут для тестов с Testcontainers, если они долго поднимаются
    systemProperty("junit.jupiter.execution.timeout.testable.method.default", "30s")
}