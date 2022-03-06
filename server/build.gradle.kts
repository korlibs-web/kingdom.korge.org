import com.soywiz.korge.gradle.*

apply<com.soywiz.korge.gradle.KorgeGradlePlugin>()

dependencies {
    add("commonMainApi", project(":shared"))
}

korge {
    targetJvm()
    jvmMainClassName = "org.korge.kingdom.server.MainKt"
}

tasks {
    //val runJvm by getting(JavaExec::class)

    val runServer by creating(com.soywiz.korge.gradle.targets.jvm.KorgeJavaExec::class) {
        dependsOn(":client:browserReleaseEsbuild")
        //dependsOn(":client:browserDebugEsbuild")
        group = "runServer"
        this.systemProperty("java.awt.headless", "true")
        mainClass.set("org.korge.kingdom.server.MainKt")
    }
}
