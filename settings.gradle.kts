plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "MacWhisper-multiplatform"
include("server:identity:api")
include("server:identity:impl")
include("server:recordings:impl")
include("server:recordings:api")
include("server:summarization:api")
include("server:transcription:impl")
include("server:summarization:impl")
include("server:transcription:api")
include("server:app")
include("server:shared-kernel")