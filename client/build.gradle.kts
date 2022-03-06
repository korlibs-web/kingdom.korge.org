import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.targets.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.*

apply<com.soywiz.korge.gradle.KorgeGradlePlugin>()

dependencies {
    add("commonMainApi", project(":shared"))
}

korge {
    targetJvm()
    targetJs()
    kotlin.targets.filterIsInstance<KotlinJsTargetDsl>().forEach { target ->
        target.compilations.all {
            kotlinOptions {
                //println("SET SOURCEMAP TO FALSE")
                sourceMap = false
            }
        }
    }
    jvmMainClassName = "org.korge.kingdom.client.MainKt"
}

