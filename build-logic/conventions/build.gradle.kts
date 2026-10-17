plugins {
    `kotlin-dsl` // Обязателен для написания плагинов на Kotlin
}

dependencies {
    // Версии должны строго совпадать с gradle/libs.versions.toml
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.1.1")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}