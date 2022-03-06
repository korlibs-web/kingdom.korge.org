import com.soywiz.korge.gradle.*

apply<com.soywiz.korge.gradle.KorgeGradlePlugin>()

korge {
    targetJvm()
    targetJs()
    serializationJson()
}
