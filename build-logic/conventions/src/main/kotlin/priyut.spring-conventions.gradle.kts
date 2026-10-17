plugins {
    id("priyut.java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Библиотечные модули не должны собираться как исполняемые Spring Boot JAR
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // Убираем суффикс -plain
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}