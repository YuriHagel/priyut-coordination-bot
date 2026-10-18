plugins {
    id("priyut.spring-conventions")
    id("priyut.test-conventions")
}

dependencies {
    implementation(project(":domain"))

    // Spring Context нужен для @Service, @Component
    // Spring TX нужен для @Transactional на use-cases
    implementation(libs.spring.context)
    implementation(libs.spring.tx)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(project(":tests:test-fixtures"))
}