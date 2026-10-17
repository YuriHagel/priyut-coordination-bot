rootProject.name = "build-logic"

include("conventions")

dependencyResolutionManagement {
    // Запрещаем подпроектам build-logic объявлять свои репозитории
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}