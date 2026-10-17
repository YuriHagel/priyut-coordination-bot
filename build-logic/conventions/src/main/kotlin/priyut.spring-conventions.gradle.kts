plugins {
    id("priyut.java-conventions")
    id("org.springframework.boot")
}


dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
}

// Библиотечные модули не должны собираться как исполняемые Spring Boot JAR
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // Убираем суффикс -plain
}

tasks.named<AbstractArchiveTask>("bootJar") {
    enabled = false
}