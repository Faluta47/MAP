plugins {
    id 'java'
    id 'application'
}

group = 'ro.ubb.map'
version = '1.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation platform('org.junit:junit-bom:5.10.3')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

application {
    mainClass = 'ro.ubb.map.retea.Main'
}

run {
    standardInput = System.in
}

test {
    useJUnitPlatform()
}
