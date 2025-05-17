import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.JavaVersion // Ensure this is imported

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20" // Your project uses Kotlin for Gradle scripts
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.recipe"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // For Javadoc "When.MAYBE" warning, ensure this is available.
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure Javadoc task
tasks.withType<Javadoc>().configureEach {
    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    this.source = sourceSets.getByName("main").allJava

    // Explicitly set the Javadoc classpath.
    // The 'main' source set's compileClasspath already includes 'compileOnly' dependencies.
    // Removed the direct reference to configurations.getByName("compileOnly").
    this.classpath = files(
        sourceSets.getByName("main").compileClasspath,
        sourceSets.getByName("main").output
    )

    this.setDestinationDir(project.file("${project.projectDir}/src/main/resources/static/apidocs"))

    this.options {
        (this as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PRIVATE
            encoding = "UTF-8"
            docEncoding = "UTF-8"
            charset("UTF-8")

            // Ensure the 'links' option is NOT present or is explicitly empty
            // this.links = null // or this.links = emptyList() // if needed

            // Suppress all doclint warnings. This is a broad suppression.
            addStringOption("Xdoclint:all,-missing", "-quiet")
        }
    }
    // Javadoc task fails on errors by default.
    // To prevent failure on warnings (if any remain problematic after Xdoclint):
    // this.failOnError = true // Default
    // (this as org.gradle.api.tasks.AbstractExecTask<*>).ignoreExitValue = true // If you want to ignore all javadoc errors/warnings for the build to pass
}

// Kotlin compilation options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}