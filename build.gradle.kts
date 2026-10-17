import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    base
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

description = "Telegram bot for coordinating car volunteers of animal shelter Priyut/Priyutino"
group = "ru.priyut.coordination"
version = providers.gradleProperty("projectVersion")
    .orElse("0.1.0-SNAPSHOT")
    .get()

val javaVersion: Int = providers.gradleProperty("javaVersion")
    .map { it.trim().toInt() }
    .getOrElse(25)

val jacocoVersion: String? = providers.gradleProperty("jacocoVersion").orNull

allprojects {
    group = rootProject.group
    version = rootProject.version
}

subprojects {
    val currentProject = this

    plugins.withId("java") {
        currentProject.extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(
                    JavaLanguageVersion.of(javaVersion)
                )
            }
        }

        currentProject.tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-parameters")
            options.release.set(javaVersion)
        }

        currentProject.tasks.withType<Javadoc>().configureEach {
            options.encoding = "UTF-8"
        }

        currentProject.tasks.withType<Test>().configureEach {
            useJUnitPlatform()

            systemProperty("file.encoding", "UTF-8")

            testLogging {
                events = setOf(
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.FAILED
                )
                showExceptions = true
                exceptionFormat = TestExceptionFormat.FULL
            }

            reports.junitXml.required.set(true)
            reports.html.required.set(true)

            maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        }

        currentProject.pluginManager.apply("jacoco")

        currentProject.extensions.configure<JacocoPluginExtension> {
            if (jacocoVersion != null) {
                toolVersion = jacocoVersion
            }
        }

        currentProject.tasks.withType<JacocoReport>().configureEach {
            reports.xml.required.set(true)
            reports.html.required.set(true)
        }

        currentProject.tasks.named("test") {
            finalizedBy("jacocoTestReport")
        }

        currentProject.tasks.named("check") {
            dependsOn("jacocoTestReport")
        }
    }

    plugins.withId("org.springframework.boot") {
        currentProject.pluginManager.apply("io.spring.dependency-management")
    }
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = providers.gradleProperty("gradleVersion")
        .getOrElse("9.0.0")

    distributionType = Wrapper.DistributionType.ALL
}