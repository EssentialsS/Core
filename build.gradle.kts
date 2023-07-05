import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

var spongeAPIVersion = "8.1.0"
if (project.hasProperty("spongeAPI")) {
    spongeAPIVersion = project.property("spongeAPI").toString();
}
var javaVersion = JavaVersion.VERSION_1_8
if (project.hasProperty("compileVersion")) {
    javaVersion = JavaVersion.toVersion(project.property("compileVersion"))
}

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
}

group = "org"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")

}

dependencies {
    api(project(":API"))
    api("com.github.mosemister:DataProperties:master-SNAPSHOT")
}

sponge {
    apiVersion("8.1.0")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("essentials-s") {
        displayName("essentials-s")
        entrypoint("org.essentialss.EssentialSMain")
        description("A stop shop for all essentials commands")
        links {
            homepage("https://ore.spongepowered.org/MoseMister/Essentials-S")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8"
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
