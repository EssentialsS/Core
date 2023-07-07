var javaVersion = JavaVersion.VERSION_1_8
if (project.hasProperty("compileVersion")) {
    javaVersion = JavaVersion.toVersion(project.property("compileVersion"))
}

plugins {
    id("java-library")
}

group = "org.essentialss"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.EssentialsS:Core:master-SNAPSHOT")
    implementation("org.spongepowered:spongeapi:8.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}