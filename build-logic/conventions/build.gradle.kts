plugins {
    `kotlin-dsl` // Обязателен для написания плагинов на Kotlin
}

dependencies {
    // Версии должны строго совпадать с gradle/libs.versions.toml
    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.1.1")
}