plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.iesra"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    implementation("com.h2database:h2:2.2.224")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}