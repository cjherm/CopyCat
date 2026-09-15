import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    application
    id("org.jetbrains.compose") version "1.6.11"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "me.ak_indiana"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

javafx {
    version = "21.0.2"
    modules = listOf("javafx.graphics", "javafx.swing")
}

dependencies {
    implementation(compose.desktop.currentOs)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
        attributes["Implementation-Version"] = project.version
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}