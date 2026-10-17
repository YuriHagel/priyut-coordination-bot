plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
    // ... другие плагины
}

dependencies {
    // ... ваши зависимости (web, jdbc, prometheus, modules)
}

// ПЕРЕОПРЕДЕЛЕНИЕ ДЛЯ ТОЧКИ ВХОДА (APP)
// Включаем создание исполняемого fat-jar и отключаем обычный jar
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("") // Убираем суффикс, чтобы файл назывался просто app.jar
}

tasks.withType<Jar>().configureEach {
    if (name == "jar") {
        enabled = false
    }
}

