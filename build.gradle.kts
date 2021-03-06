plugins {
    kotlin("jvm")
}

group = "com.github.quillraven.kecs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

sourceSets {
    val benchmark by creating {
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

configurations {
    val benchmarkImplementation by getting {
        extendsFrom(configurations["implementation"])
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.badlogicgames.gdx:gdx:${project.property("gdxVersion")}")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:${project.property("spekVersion")}")
    testImplementation("org.amshove.kluent:kluent:${project.property("kluentVersion")}")
    testImplementation("com.badlogicgames.ashley:ashley:1.7.3")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:${project.property("spekVersion")}")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:${project.property("kotlinVersion")}")

    configurations["benchmarkImplementation"]("com.badlogicgames.ashley:ashley:1.7.3")
    configurations["benchmarkImplementation"]("net.onedaybeard.artemis:artemis-odb:2.3.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileBenchmarkKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}
