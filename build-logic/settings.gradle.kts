rootProject.name = "priyut-coordination-bot"

includeBuild("build-logic")

include(
    "app",
    "domain",
    "application",
    "infrastructure:persistence",
    "infrastructure:telegram",
    "infrastructure:scheduling",
    "tests:test-fixtures",
    "tests:integration-tests"
)