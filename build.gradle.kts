import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.targets.jvm.*

buildscript {
    val korgePluginVersion: String by project

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
    }
}

tasks {
    // runs the server and a JVM client in a single process
    val runSingle by creating(Task::class) {
        dependsOn(":aether:runJvm")
        group = "run"
    }

    // main tasks: compiles game in Kotlin/JS and starts the websocket server + http server serving the K/JS client
    //             so multiple clients can connect
    val runServer by creating(Task::class) {
        dependsOn(":server:runServer")
        group = "run"
    }

    // deploy
    val deploy by creating(Task::class) {
        val buildServerArtifacts by creating(Copy::class) {
            dependsOn(":client:browserReleaseEsbuild")
            dependsOn(":server:packageJvmFatJar")
            from(File(project(":client").buildDir, "www"))
            from(File(project(":server").buildDir, "libs"))
            into(File(project(":server").buildDir, "app"))
            rename { if (it.contains(".jar")) it else "www/$it" }
            duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
        }

        val deployArtifacts by creating(Exec::class) {
            dependsOn(buildServerArtifacts)
            commandLine("rsync", "-avzhP", File(project(":server").buildDir, "app"), "soywiz:/home/virtual/korge/kingdom.korge.org/")
        }

        val restartServer by creating(Exec::class) {
            commandLine("ssh", "soywiz", "cd /home/virtual/korge/kingdom.korge.org/; docker-compose restart")
        }

        dependsOn(deployArtifacts)
        finalizedBy(restartServer)
    }
}