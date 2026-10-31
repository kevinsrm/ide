plugins {
    id("com.android.application") version "9.4.0" apply false
}

tasks.register<org.gradle.api.tasks.Delete>("clean") {
    delete(layout.buildDirectory)
}
