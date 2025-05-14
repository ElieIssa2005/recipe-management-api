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

    // Ensure Swagger/OpenAPI dependency is REMOVED
    // implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // Temporarily ensure javax.annotation-api is NOT in compileOnly to avoid the resolution error.
    // If the 'When.MAYBE' Javadoc warning reappears, we can address it differently later if needed.
    // compileOnly("javax.annotation:javax.annotation-api:1.3.2") // Keep this commented out or removed for now

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.11.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure Javadoc task
tasks.withType<Javadoc>().configureEach {
    // 'this' implicitly refers to the Javadoc task instance here

    // Get the main source set for Java files
    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    this.source = sourceSets.getByName("main").allJava
    // Set the classpath for Javadoc. This configuration is standard and should pick up
    // necessary dependencies for Javadoc to understand types used in your code.
    this.classpath = configurations.compileClasspath.get()

    // Set the destination directory directly on the Javadoc task object.
    // This ensures Javadoc output goes to src/main/resources/static/apidocs,
    // which Spring Boot can then serve from the packaged JAR.
    this.setDestinationDir(project.file("${project.projectDir}/src/main/resources/static/apidocs"))
    // For default build directory (requires Dockerfile adjustment if used):
    // this.setDestinationDir(project.file("${project.buildDir}/docs/javadoc"))

    // Configure options for the Javadoc tool
    this.options {
        // 'this' inside this options block refers to JavadocOptions
        // Cast to StandardJavadocDocletOptions to access specific methods/properties
        (this as StandardJavadocDocletOptions).apply {
            // Include members down to 'private' level if they have Javadoc comments
            memberLevel = JavadocMemberLevel.PRIVATE

            // Link to external Javadoc, like the Java SE API, for standard Java types
            links = listOf("https://docs.oracle.com/en/java/javase/17/docs/api/")
            // Add more links if needed:
            // links?.add("https://docs.spring.io/spring-framework/docs/current/javadoc-api/")

            // Other useful options:
            // windowTitle = "${project.name} ${project.version} API Documentation"
            // docTitle = "<h1>${project.name} ${project.version} API</h1>"
            // setAuthor(true)
            // setVersion(true)
        }
    }

    // Optional: Fail the build on Javadoc errors
    // this.isFailOnError = true
    // this.isFailOnWarning = false
}

// Kotlin compilation options
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}