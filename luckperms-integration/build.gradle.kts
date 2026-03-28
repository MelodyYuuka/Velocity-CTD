plugins {
    `java-library`
    `maven-publish`
    id("velocity-publish")
}

dependencies {
    implementation(project(":velocity-api"))

    compileOnly("net.luckperms:api:5.5")
}

tasks {
}
