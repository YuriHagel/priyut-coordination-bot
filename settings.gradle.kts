rootProject.name = "priyut-coordination-bot"

includeBuild("build-logic")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // Жесткое правило: запрещаем подпроектам (subprojects) объявлять свои репозитории.
    // Все репозитории должны быть объявлены только здесь. Это ускоряет конфигурацию и предотвращает хаос.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}

include(
    "app",
    "domain",
    "application",
    "infrastructure:persistence",
    "infrastructure:telegram",
    "infrastructure:scheduling",
    "tests:test-fixtures",
    "tests:integration"
)
