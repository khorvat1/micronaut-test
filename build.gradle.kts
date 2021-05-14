plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("org.jetbrains.kotlin.kapt") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("io.micronaut.application") version "1.5.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.32"
}

version = "0.1"
group = "com.example"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.example.*")
        group("com.example")
        module("micronaut-introspection")
    }
}

dependencies {
    kapt("io.micronaut.openapi:micronaut-openapi")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("javax.annotation:javax.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    compileOnly("org.graalvm.nativeimage:svm")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

allOpen {
    annotation("javax.inject.Singleton")
    annotation("io.micronaut.http.annotation.Controller")
}

application {
    mainClass.set("com.example.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileJava {
        options.compilerArgs.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED ")
        options.compilerArgs.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.awt.im=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.java2d.loops=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.java2d.opengl=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED")
        options.compilerArgs.add("--add-exports=java.desktop/sun.java2d.cmm.lcms=ALL-UNNAMED")
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }


}
