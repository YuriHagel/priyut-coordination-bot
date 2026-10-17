plugins {
    // Базовые настройки Java (toolchain, кодировка, JUnit 5)
    id("priyut.java-conventions")

    // Ключевой плагин: включает source set "testFixtures"
    // и позволяет другим модулям зависеть от него через testFixtures(...)
    `java-test-fixtures`
}

dependencies {
    // 1. Зависимости, необходимые ТОЛЬКО для компиляции самих фикстур
    // (Фикстуры создают доменные объекты и реализуют порты приложения)
    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(project(":application"))

    // 2. Зависимости, которые будут ПЕРЕДАНЫ модулям, использующим эти фикстуры.
    // Используем 'api', чтобы модулям (например, integration-tests) не пришлось
    // подключать эти библиотеки заново вручную.
    testFixturesApi(libs.spring.boot.starter.test)
    testFixturesApi(libs.testcontainers.postgresql)
    testFixturesApi(libs.testcontainers.junit.jupiter)
    testFixturesApi(libs.awaitility)
    testFixturesApi(libs.wiremock.standalone)
}

// Дополнительная настройка кодировки конкретно для исходников фикстур
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}