plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.joshuaportero.ajs"
version = "1.1.2-SNAPSHOT"

project.java.sourceCompatibility = JavaVersion.VERSION_17
project.java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.9.1")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.9.1")

    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.7")

    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("com.github.lalyos:jfiglet:0.0.8")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
    shadowJar {
        manifest {
            attributes["Main-Class"] = "me.joshuaportero.ajs.JobScraper"
        }
    }
}
