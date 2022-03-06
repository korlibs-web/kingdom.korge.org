import com.soywiz.korge.gradle.*

apply<com.soywiz.korge.gradle.KorgeGradlePlugin>()

dependencies {
    add("commonMainApi", project(":client"))
    add("commonMainApi", project(":server"))
}

korge {
    targetJvm()
    jvmMainClassName = "org.korge.kingdom.MainKt"
}
