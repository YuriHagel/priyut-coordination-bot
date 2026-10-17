plugins {
    id("priyut.java-conventions")
    id("org.springframework.boot")
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libsCatalog = catalogs.named("libs")

val springBootVersion = libsCatalog.findVersion("spring-boot")
    .map { it.requiredVersion }
    .orElseThrow { GradleException("Версия 'spring-boot' не найдена в каталоге libs.versions.toml") }

dependencies {
    // Подставляем полученную строку версии
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
}

// Библиотечные модули не должны собираться как исполняемые Spring Boot JAR
tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // Убираем суффикс -plain
}

tasks.named<AbstractArchiveTask>("bootJar") {
    enabled = false
}