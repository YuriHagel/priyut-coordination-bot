rootProject.name = "build-logic"

dependencyResolutionManagement {
    // Запрещаем подпроектам build-logic объявлять свои репозитории
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}