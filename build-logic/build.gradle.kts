plugins {
    `java-gradle-plugin`
    alias(common.plugins.kotlin.jvm)
    alias(common.plugins.kotlin.samreceiver)
    alias(common.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    "implementation"(gradleKotlinDsl())
    implementation(common.kotlin.serialization.json)
}

samWithReceiver {
    annotation(HasImplicitReceiver::class.qualifiedName!!)
}


gradlePlugin {
    plugins {
        register("common") {
            id = "clover-common"
            implementationClass = "CommonPlugin"
        }
    }
}
