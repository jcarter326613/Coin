import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.5.30"
    distribution
}

group = "me.jason"
version = "0.0.1"

repositories {
    mavenCentral()
}

tasks.jar {
    isZip64 = true
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    from(configurations.compileOnly.get().map { if (it.isDirectory) it else zipTree(it) })
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.ejml:ejml-all:0.41")

    testImplementation(kotlin("test-junit5"))
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
