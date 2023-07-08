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

System.out.println("Parameters: JavaVersion = " + javaVersion + " - SpongeAPI = " + spongeAPIVersion)

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
}

group = "org.essentialss"
version = "0.1.0"


tasks {
    test {
        useJUnitPlatform()
    }

    register("install") {
        dependsOn("build")
        val coreFile = file("./build/libs/Essentials-S.jar");
        val pathTo = file("${System.getProperty("user.home")}/.m2/repository/org/essentialss/Core/1.0-SNAPSHOT");
        coreFile.copyTo(pathTo, true)
    }

    jar {
        dependsOn(":API:build")
        //dependsOn(":LegacyChatFormatingModule:build");
        archiveFileName.set("Essentials-S.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val collected = configurations.runtimeClasspath.get()
                .filter {
                    if (it.name.startsWith("API-")) {
                        return@filter true
                    }
                    if (it.name.startsWith("DataProperties-")) {
                        return@filter true
                    }
                    return@filter false
                }
                .map {
                    return@map zipTree(it)
                }

        from(collected)
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    api(project(":API"))
    api("com.github.mosemister:DataProperties:master-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

sponge {
    apiVersion(spongeAPIVersion)
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
